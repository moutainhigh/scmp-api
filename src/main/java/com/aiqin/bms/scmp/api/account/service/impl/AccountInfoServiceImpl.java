package com.aiqin.bms.scmp.api.account.service.impl;

import com.aiqin.bms.scmp.api.account.dao.AccountDao;
import com.aiqin.bms.scmp.api.account.domain.Account;
import com.aiqin.bms.scmp.api.account.domain.Util.CodeUtil;
import com.aiqin.bms.scmp.api.account.domain.request.AccountRequest;
import com.aiqin.bms.scmp.api.account.domain.request.PersonRequest;
import com.aiqin.bms.scmp.api.account.domain.response.AccountResponse;
import com.aiqin.bms.scmp.api.account.properties.ContorlUrlProperties;
import com.aiqin.bms.scmp.api.account.service.AccountInfoService;
import com.aiqin.bms.scmp.api.base.PageResData;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.supplier.domain.response.account.Role;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import com.aiqin.ground.util.http.HttpClient;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * ━━━━━━神兽出没━━━━━━
 * 　　┏┓　　　┏┓+ +
 * 　┏┛┻━━━┛┻┓ + +
 * 　┃　　　　　　　┃
 * 　┃　　　━　　　┃ ++ + + +
 * ████━████ ┃+
 * 　┃　　　　　　　┃ +
 * 　┃　　　┻　　　┃
 * 　┃　　　　　　　┃
 * 　┗━┓　　　┏━┛
 * 　　　┃　　　┃                  神兽保佑, 永无BUG!
 * 　　　┃　　　┃
 * 　　　┃　　　┃     Code is far away from bug with the animal protecting
 * 　　　┃　 　　┗━━━┓
 * 　　　┃ 　　　　　　　┣┓
 * 　　　┃ 　　　　　　　┏┛
 * 　　　┗┓┓┏━┳┓┏┛
 * 　　　　┃┫┫　┃┫┫
 * 　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 * <p>
 * <p>
 * 思维方式*热情*能力
 */
@Service
public class AccountInfoServiceImpl implements AccountInfoService {

    private static Logger LOGGER = LoggerFactory.getLogger(AccountInfoServiceImpl.class);

    @Resource
    private AccountDao accountDao;
    @Resource
    private ContorlUrlProperties contorlUrlProperties;

    @Override
    public HttpResponse<PageResData<Account>> accountList(AccountRequest request) {
        List<Account> list = accountDao.list(request);
        Integer count = accountDao.listCount(request);
        LOGGER.info("查询供应商账号列表:{}", count);
        return HttpResponse.successGenerics(new PageResData<>(count, list));
    }
    private PersonRequest handlePerson(Account request){
        PersonRequest personRequest = new PersonRequest();
        personRequest.setPersonId(request.getUsername());

        return personRequest;
    }
    @Override
    public HttpResponse addAccount(AccountRequest request) {
        if(CollectionUtils.isEmpty(request.getRoleIds())){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        Account account = new Account();
        BeanUtils.copyProperties(request,account);
        Account response = accountDao.selectLastOne();
        String username ="";
        if(response==null){
            username = CodeUtil.getCode(CodeUtil.SUPPLIER_PREFIX,"");
        }else{
            username = CodeUtil.getCode(CodeUtil.SUPPLIER_PREFIX,response.getUsername());
        }
        account.setUsername(username);
        account.setPersonId(username);
        //处理调用的参数
        PersonRequest personRequest = handlePerson(account);
        //增加person
        HttpClient client = HttpClient.post(contorlUrlProperties.getRoleList()).json(personRequest);
        HttpResponse httpResponse = client.action().result(HttpResponse.class);
        if(httpResponse.getCode().equals(MessageId.SUCCESS_CODE)){
            // todo account表数据
            // TODO 创建部门
            // todo 关联角色
            // 增加供应链关联表

//        synchronized (this){
            account.setCompanyCode("00");
            account.setCompanyName("公司");
            account.setDepartmentCode("001");
            account.setDepartmentName("部门");
            account.setRoleIds(request.getRoleIds().stream().collect(Collectors.joining(",")));
            Integer count  = accountDao.insert(account);
            LOGGER.info("添加供应商关联表:{}",count);
//        }
        }

        return HttpResponse.success();
    }

    @Override
    public HttpResponse<List<Role>> selectRole(String personId, String ticket) {
        try {
            HttpClient client = HttpClient.get(contorlUrlProperties.getRoleList() + "?system_code=smps-system&page_size=1000")
                    .addParameter("ticket", ticket)
                    .addParameter("ticket_person_id", personId);
            HttpResponse response = client.action().result(HttpResponse.class);
            if (response.getCode().equals(MessageId.SUCCESS_CODE)) {
                PageResData resData = JsonUtil.fromJson(JsonUtil.toJson(response.getData()), PageResData.class);
                if (resData != null && CollectionUtils.isNotEmpty(resData.getDataList())) {
                    List<Role> roleList = JsonUtil.fromJson(JsonUtil.toJson(resData.getDataList()), new TypeReference<List<Role>>() {
                    });
                    return HttpResponse.successGenerics(roleList);
                }
                return HttpResponse.successGenerics(new ArrayList<>());
            } else {
                LOGGER.error("调用主控查询异常", response.getMessage());
                throw new GroundRuntimeException("调用主控查询异常");
            }
        } catch (Exception e) {
            LOGGER.error("查询角色异常:{}", e);
            return HttpResponse.failureGenerics(ResultCode.SELECT_ROLE_ERROR, null);
        }
    }

    @Override
    public HttpResponse updateAccount(Account account) {
        Account response = accountDao.selectOne(account);
        if (response == null) {
            HttpResponse.failureGenerics(ResultCode.NO_HAVE_ACCOUNT, null);
        }
        // todo 修改person
        // todo 修改account
        // todo 修改角色
        // 修改供应链关联表
        Integer count = accountDao.updateByPrimaryKeySelective(account);
        LOGGER.info("修改供应商关联表:{}",count);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse<AccountResponse> accountInfo(String username) {
        Account request = new Account();
        request.setUsername(username);
        Account account = accountDao.selectOne(request);
        if (account == null) {
            HttpResponse.failureGenerics(ResultCode.NO_HAVE_ACCOUNT, null);
        }
        AccountResponse response = new AccountResponse();
        BeanUtils.copyProperties(account,response);
        if(StringUtils.isNotEmpty(account.getRoleIds())){
            response.setRoleIds(Arrays.asList(account.getRoleIds().split(",")));
        }
        return HttpResponse.successGenerics(response);
    }
}

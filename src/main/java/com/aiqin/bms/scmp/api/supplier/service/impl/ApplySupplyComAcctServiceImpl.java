package com.aiqin.bms.scmp.api.supplier.service.impl;

import com.aiqin.bms.scmp.api.base.*;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.*;
import com.aiqin.bms.scmp.api.config.AuthenticationInterceptor;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.dao.supplier.ApplySupplyCompanyAcctDao;
import com.aiqin.bms.scmp.api.supplier.dao.supplier.SupplyCompanyAccountDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.ApplySupplyCompanyAccount;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.EncodingRule;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.SupplyCompanyAccount;
import com.aiqin.bms.scmp.api.supplier.domain.request.OperationLogBean;
import com.aiqin.bms.scmp.api.supplier.domain.request.apply.QueryApplyReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.dto.ApplySupplyComAcctDTO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.dto.ApplySupplyCompanyAcctReqDTO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.dto.SupplyCompanyAccountDTO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.vo.ApplySupplyCompanyAcctReqVO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.vo.QueryApplySupplierComAcctReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.vo.QuerySupplierComAcctReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.LogData;
import com.aiqin.bms.scmp.api.supplier.domain.response.apply.ApplyListRespVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.apply.DetailRequestRespVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.supplier.*;
import com.aiqin.bms.scmp.api.supplier.mapper.ApplySupplyCompanyAccountMapper;
import com.aiqin.bms.scmp.api.supplier.mapper.SupplyCompanyAccountMapper;
import com.aiqin.bms.scmp.api.supplier.service.ApplySupplyComAcctService;
import com.aiqin.bms.scmp.api.supplier.service.EncodingRuleService;
import com.aiqin.bms.scmp.api.supplier.service.OperationLogService;
import com.aiqin.bms.scmp.api.supplier.service.SupplierCommonService;
import com.aiqin.bms.scmp.api.util.*;
import com.aiqin.bms.scmp.api.workflow.annotation.WorkFlowAnnotation;
import com.aiqin.bms.scmp.api.workflow.enumerate.WorkFlow;
import com.aiqin.bms.scmp.api.workflow.helper.WorkFlowHelper;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowCallbackVO;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowVO;
import com.aiqin.bms.scmp.api.workflow.vo.response.WorkFlowRespVO;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @功能说明:
 * @author: wangxu
 * @date: 2018/12/3 0003 16:45
 */
@Slf4j
@Service("1")
@WorkFlowAnnotation(WorkFlow.APPLY_COMPANY_ACC)
public class ApplySupplyComAcctServiceImpl extends BaseServiceImpl implements ApplySupplyComAcctService, WorkFlowHelper {
    @Autowired
    private ApplySupplyCompanyAccountMapper applySupplyCompanyAccountMapper;
    @Autowired
    private ApplySupplyCompanyAcctDao applySupplyCompanyAcctDao;
    @Autowired
    private SupplyCompanyAccountDao supplyCompanyAccountDao;
    @Autowired
    private SupplyCompanyAccountMapper supplyCompanyAccountMapper;
    @Autowired
    private EncodingRuleDao encodingRuleDao;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private SupplierCommonService supplierCommonService;
    @Autowired
    private EncodingRuleService encodingRuleService;
    @Autowired
    private WorkFlowBaseUrl workFlowBaseUrl;
    @Autowired
    private CodeUtils codeUtils;


    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    public Long saveApply(ApplySupplyCompanyAcctReqVO applySupplyCompanyAcctReqVO) {
        log.info("ApplySupplyComAcctServiceImpl-saveApply-传入参数是：[{}]", JSON.toJSONString(applySupplyCompanyAcctReqVO));
        Long resultNum;
        try {
            //复制对象
            ApplySupplyCompanyAcctReqDTO applySupplyCompanyAcctReqDTO = new ApplySupplyCompanyAcctReqDTO();
            BeanCopyUtils.copy(applySupplyCompanyAcctReqVO, applySupplyCompanyAcctReqDTO);
            applySupplyCompanyAcctReqDTO.setFormNo("GYSZH" + IdSequenceUtils.getInstance().nextId());
            resultNum = insertApplyAndLog(applySupplyCompanyAcctReqDTO);
            //调用审批接口
            workFlow(applySupplyCompanyAcctReqDTO);
        } catch (Exception ex) {
            throw new GroundRuntimeException(ex.getMessage());
        }
        return resultNum;
    }

    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    public Long insideSaveApply(ApplySupplyComAcctDTO applySupplyComAcctDTO) {
        ApplySupplyCompanyAcctReqDTO applySupplyCompanyAcctReqDTO = new ApplySupplyCompanyAcctReqDTO();
        BeanCopyUtils.copy(applySupplyComAcctDTO, applySupplyCompanyAcctReqDTO);
        Long resultNum = insertApplyAndLog(applySupplyCompanyAcctReqDTO);
        return resultNum;
    }

    @Transactional(rollbackFor = GroundRuntimeException.class)
    public Long insertApplyAndLog(ApplySupplyCompanyAcctReqDTO applySupplyCompanyAcctReqDTO) {
        log.info("ApplySupplyComAcctServiceImpl-insertApplyAndLog-传入参数是：[{}]", JSON.toJSONString(applySupplyCompanyAcctReqDTO));
        //供货单位账户申请编码
        String rule = encodingRuleService.getNumberingType(EncodingRuleType.APPLY_SUPPLY_COM_ACCT_CODE);
        applySupplyCompanyAcctReqDTO.setApplyCode(rule);
        applySupplyCompanyAcctReqDTO.setApplyType((byte) 1);
        applySupplyCompanyAcctReqDTO.setApplyStatus((byte) 0);
        ((ApplySupplyComAcctService) AopContext.currentProxy()).insert(applySupplyCompanyAcctReqDTO);
        //encodingRuleService.updateNumberValue(rule.getNumberingValue(), rule.getId());
        String content = ApplyStatus.PENDING.getContent().replace(Global.CREATE_BY, applySupplyCompanyAcctReqDTO.getCreateBy()).replace(Global.APPLY_TYPE, "新增");
        return supplierCommonService.getInstance(rule+ "", HandleTypeCoce.PENDING.getStatus(), ObjectTypeCode.APPLY_SUPPLY_COMPANY_ACCOUNT.getStatus(), content, null, HandleTypeCoce.PENDING.getName());

    }

    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @Save
    public Long insert(ApplySupplyCompanyAcctReqDTO applySupplyCompanyAcctReqDTO) {
        log.info("ApplySupplyComAcctServiceImpl-insert-传入参数是：[{}]", JSON.toJSONString(applySupplyCompanyAcctReqDTO));
        Long resultNum;
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        if (null != authToken) {
            applySupplyCompanyAcctReqDTO.setCompanyCode(authToken.getCompanyCode());
            applySupplyCompanyAcctReqDTO.setCompanyName(authToken.getCompanyName());
        }
        if (Objects.isNull(applySupplyCompanyAcctReqDTO.getFormNo())) {
            if (null == applySupplyCompanyAcctReqDTO.getApplyShow()) {
                applySupplyCompanyAcctReqDTO.setApplyShow((byte) 1);
            }
            resultNum = applySupplyCompanyAcctDao.insertApply(applySupplyCompanyAcctReqDTO);
        } else {
            if (null == applySupplyCompanyAcctReqDTO.getApplyShow()) {
                applySupplyCompanyAcctReqDTO.setApplyShow((byte) 0);
            }
            resultNum = applySupplyCompanyAcctDao.insertApplyOut(applySupplyCompanyAcctReqDTO);
        }
        if (resultNum > 0) {
            return resultNum;
        } else {
            throw new GroundRuntimeException("新增申请供应商账户失败");
        }
    }

    @Override
//    @Async("myTaskAsyncPool")
    @Transactional(rollbackFor = Exception.class)
    public void workFlow(ApplySupplyCompanyAcctReqDTO applySupplyCompanyAcctReqDTO) {
        log.info("ApplySupplyComAcctServiceImpl-workFlow-传入参数是：[{}]", JSON.toJSONString(applySupplyCompanyAcctReqDTO));
        try {
            WorkFlowVO workFlowVO = new WorkFlowVO();
            workFlowVO.setPositionCode(applySupplyCompanyAcctReqDTO.getPositionCode());
            workFlowVO.setFormUrl(workFlowBaseUrl.applySupplierAccountUrl + "?applyType=" + applySupplyCompanyAcctReqDTO.getApplyType() + "&applyCode=" + applySupplyCompanyAcctReqDTO.getApplyCode() + "&id=" + applySupplyCompanyAcctReqDTO.getId() + "&itemCode=3" + "&" + workFlowBaseUrl.authority);
            workFlowVO.setHost(workFlowBaseUrl.supplierHost);
            workFlowVO.setFormNo(applySupplyCompanyAcctReqDTO.getFormNo());
            workFlowVO.setUpdateUrl(workFlowBaseUrl.callBackBaseUrl + WorkFlow.APPLY_COMPANY_ACC.getNum());
            int i1 = applySupplyCompanyAcctReqDTO.getApplyType().intValue();
            String info = "账户信息";
            String applyTypeTitle = "修改";
            if (StatusTypeCode.ADD_APPLY.getStatus().equals(applySupplyCompanyAcctReqDTO.getApplyType())) {
                applyTypeTitle = "新增";
            }
            workFlowVO.setTitle(applyTypeTitle + applySupplyCompanyAcctReqDTO.getAccountName() + info);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("auditPersonId", applySupplyCompanyAcctReqDTO.getDirectSupervisorCode());
            workFlowVO.setVariables(jsonObject.toString());
            WorkFlowRespVO workFlowRespVO = callWorkFlowApi(workFlowVO, WorkFlow.APPLY_COMPANY_ACC);
            if (workFlowRespVO.getSuccess()) {
                ApplySupplyCompanyAccount applySupplyCompanyAccount = new ApplySupplyCompanyAccount();
                applySupplyCompanyAccount.setId(applySupplyCompanyAcctReqDTO.getId());
                //状态变为审核中
                applySupplyCompanyAccount.setApplyStatus((byte) 1);
                int i = applySupplyCompanyAccountMapper.updateByPrimaryKeySelective(applySupplyCompanyAccount);
                if (i <= 0) {
                    throw new GroundRuntimeException("审核状态修改失败");
                }

                String content = ApplyStatus.PENDING.getContent().replace(Global.CREATE_BY, applySupplyCompanyAcctReqDTO.getUpdateBy()).replace(Global.APPLY_TYPE, applyTypeTitle);
                //存日志
                supplierCommonService.getInstance(applySupplyCompanyAcctReqDTO.getApplyCode(), HandleTypeCoce.APPROVAL.getStatus(), ObjectTypeCode.APPLY_SUPPLY_COMPANY_ACCOUNT.getStatus(), content, null, HandleTypeCoce.APPROVAL.getName());
            } else {
                //存调用失败的日志
                String msg = workFlowRespVO.getMsg();
                log.info(msg);
                throw new GroundRuntimeException(msg);
            }
        } catch (Exception e) {
            //存失败日志
            throw new GroundRuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ApplySupplyCompanyAcctReqVO applySupplyCompanyAcctReq) {
        log.info("ApplySupplyComAcctServiceImpl-update-传入参数是：[{}]", JSON.toJSONString(applySupplyCompanyAcctReq));
        if (Objects.isNull(applySupplyCompanyAcctReq.getId())) {
            throw new GroundRuntimeException("查询条件异常");
        }
        //首先通过编码查找到原始数据
        SupplyCompanyAccount supplyCompanyAccount = supplyCompanyAccountMapper.selectByPrimaryKey(applySupplyCompanyAcctReq.getId());
        if (Objects.isNull(supplyCompanyAccount)) {
            log.error("查询供应单位数据失败，传入id是:{}", applySupplyCompanyAcctReq.getId());
            throw new GroundRuntimeException("修改供应单位账户失败");
        }
        if (Objects.equals(supplyCompanyAccount.getApplyStatus(), 1)) {
            throw new GroundRuntimeException("审批中的数据,不允许修改");
        }
        //首先判断是否是删除操作
        if (Objects.equals(applySupplyCompanyAcctReq.getDelFlag().intValue(), 1)) {
            //是，执行逻辑删除
            ApplySupplyCompanyAcctReqDTO s = new ApplySupplyCompanyAcctReqDTO();
            s.setDelFlag((byte) 1);
            s.setId(supplyCompanyAccount.getId());
            return ((ApplySupplyComAcctService) AopContext.currentProxy()).deleteSupplyData(s);
        }
        //修改数据
        ApplySupplyCompanyAcctReqDTO s = new ApplySupplyCompanyAcctReqDTO();
        BeanCopyUtils.copy(applySupplyCompanyAcctReq, s);
//        s.setId(account.getId());
        s.setId(null);
        s.setApplyStatus((byte) 0);
//        s.setApplyCode(supplyCompanyAccount.getApplyCompanyAccountCode());
        s.setApplyType((byte) 2);
        s.setFormNo("GYSZH" + IdSequenceUtils.getInstance().nextId());
        String code = encodingRuleService.getNumberingType(EncodingRuleType.APPLY_SUPPLY_COM_ACCT_CODE);
        s.setApplyCode(code);
        ((ApplySupplyComAcctService) AopContext.currentProxy()).insert(s);
       // encodingRuleService.updateNumberValue(rule.getNumberingValue(), rule.getId());
//        ((ApplySupplyComAcctService) AopContext.currentProxy()).updateApplyData(s);
        //存日志
        String content = ApplyStatus.PENDING.getContent().replace(Global.CREATE_BY, s.getUpdateBy()).replace(Global.APPLY_TYPE, "修改");
        supplierCommonService.getInstance(s.getApplyCode(), HandleTypeCoce.PENDING.getStatus(), ObjectTypeCode.APPLY_SUPPLY_COMPANY_ACCOUNT.getStatus(), content, null, HandleTypeCoce.PENDING.getName());
        //申请表状态更新完成后，再更新正式表
        supplyCompanyAccount.setApplyStatus((byte) 1);
        SupplyCompanyAccountDTO s1 = new SupplyCompanyAccountDTO();
        s1.setId(supplyCompanyAccount.getId());
        s1.setApplyStatus((byte) 1);
        s1.setDelFlag((byte) 0);
        s1.setApplyCompanyAccountCode(s.getApplyCode());
        int i = ((ApplySupplyComAcctService) AopContext.currentProxy()).updateSupplyStatus(s1);
        workFlow(s);
        return i;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Update
    public int updateApplyData(ApplySupplyCompanyAcctReqDTO s) {
        log.info("ApplySupplyComAcctServiceImpl-updateApplyData-传入参数是：[{}]", JSON.toJSONString(s));
        int temp;
        try {
            ApplySupplyCompanyAccount t = new ApplySupplyCompanyAccount();
            BeanCopyUtils.copy(s, t);
            t.setSupplyCompanyCode(s.getApplySupplyCompanyCode());
            t.setSupplyCompanyName(s.getApplySupplyCompanyName());
            ApplySupplyCompanyAccount applySupplyCompanyAccount = applySupplyCompanyAccountMapper.selectByPrimaryKey(s.getId());
            temp = applySupplyCompanyAccountMapper.updateByPrimaryKeySelective(t);
            if (temp <= 0) {
                throw new GroundRuntimeException("修改供应单位账户失败");
            }
            //调用审批
        } catch (Exception e) {
            log.error("修改供应单位账户失败,传入的对象是：{}", JSONObject.toJSONString(s));
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("修改供应单位账户失败");
        }
        return temp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSupplyStatus(SupplyCompanyAccountDTO s) {
        log.info("ApplySupplyComAcctServiceImpl-updateSupplyStatus-传入参数是：[{}]", JSON.toJSONString(s));
        int temp;
        try {
            SupplyCompanyAccount t = new SupplyCompanyAccount();
            BeanUtils.copyProperties(s, t);
            temp = supplyCompanyAccountMapper.updateByPrimaryKeySelective(t);
            if (temp <= 0) {
                throw new GroundRuntimeException("修改供应单位账户失败");
            }
        } catch (Exception e) {
            log.error("修改供应单位账户失败,传入的对象是：{}", JSONObject.toJSONString(s));
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("修改供应单位账户失败");
        }
        return temp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Update
    public int updateSupplyData(SupplyCompanyAccountDTO s) {
        log.info("ApplySupplyComAcctServiceImpl-updateSupplyData-传入参数是：[{}]", JSON.toJSONString(s));
        int temp;
        try {
            SupplyCompanyAccount t = new SupplyCompanyAccount();
            BeanUtils.copyProperties(s, t);
            temp = supplyCompanyAccountMapper.updateByPrimaryKey(t);
            ApplySupplyCompanyAccount applySupplyCompanyAccount = applySupplyCompanyAccountMapper.selectByPrimaryKey(s.getId());
            if (temp <= 0) {
                throw new GroundRuntimeException("修改供应单位账户失败");
            }
        } catch (Exception e) {
            log.error("修改供应单位账户失败,传入的对象是：{}", JSONObject.toJSONString(s));
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("修改供应单位账户失败");
        }
        return temp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Update
    public int deleteSupplyData(ApplySupplyCompanyAcctReqDTO s) {
        log.info("ApplySupplyComAcctServiceImpl-deleteSupplyData-传入参数是：[{}]", JSON.toJSONString(s));
        int temp;
        try {
            SupplyCompanyAccount t = new SupplyCompanyAccount();
            BeanUtils.copyProperties(s, t);
            ApplySupplyCompanyAccount applySupplyCompanyAccount = applySupplyCompanyAccountMapper.selectByPrimaryKey(s.getId());
            temp = supplyCompanyAccountMapper.updateByPrimaryKeySelective(t);
            if (temp <= 0) {
                throw new GroundRuntimeException("删除供应单位账户失败");
            }
            //存日志
            supplierCommonService.getInstance(applySupplyCompanyAccount.getApplyCompanyAccountCode(), HandleTypeCoce.DELETE.getStatus(), ObjectTypeCode.SUPPLY_COMPANY_ACCOUNT.getStatus(), HandleTypeCoce.APPLY_DELETE_SUPPLY_COMPANY_ACCOUNT.getName(), null, HandleTypeCoce.DELETE.getName());
        } catch (Exception e) {
            log.error("删除供应单位账户失败,传入的对象是：{}", JSONObject.toJSONString(s));
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("删除供应单位账户失败");
        }
        return temp;
    }

    @Override
    public BasePage<QueryApplySupplierComAcctRespVo> selectApplyListByQueryVO(QueryApplySupplierComAcctReqVo vo) {
        log.info("ApplySupplyComAcctServiceImpl-selectApplyListByQueryVO-传入参数是：[{}]", JSON.toJSONString(vo));
        try {
            AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
            if (null != authToken) {
                vo.setCompanyCode(authToken.getCompanyCode());
                vo.setApplyBy(authToken.getPersonName());
            }
            PageHelper.startPage(vo.getPageNo(), vo.getPageSize());
            List<QueryApplySupplierComAcctRespVo> userDetails = applySupplyCompanyAcctDao.selectListByQueryVO(vo);
            return PageUtil.getPageList(vo.getPageNo(), userDetails);
        } catch (Exception ex) {
            log.error("申请供货单位账户列表查询异常！");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    @Override
    public BasePage<QuerySupplierComAcctRespVo> selectSupplyListByQueryVO(QuerySupplierComAcctReqVo vo) {
        log.info("ApplySupplyComAcctServiceImpl-selectSupplyListByQueryVO-传入参数是：[{}]", JSON.toJSONString(vo));
        try {
            AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
            if (null != authToken) {
                vo.setCompanyCode(authToken.getCompanyCode());
//                vo.setApplyBy(authToken.getPersonName());
                vo.setPersonId(authToken.getPersonId());
            }
            PageHelper.startPage(vo.getPageNo(), vo.getPageSize());
            List<QuerySupplierComAcctRespVo> userDetails = supplyCompanyAccountDao.selectListByQueryVO(vo);
            return PageUtil.getPageList(vo.getPageNo(), userDetails);
        } catch (Exception ex) {
            log.error("供货单位账户列表查询异常！");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     * 根据供应商名称查询账户信息
     *
     * @param supplierCode
     * @return
     */
    @Override
    public List<QuerySupplierComAcctRespVo> selectSupplierComAcctBySupplierCode(String supplierCode) {
        if (StringUtils.isBlank(supplierCode)) {
            throw new BizException(ResultCode.SUPPLIER_CODE_EMPTY);
        }
        QuerySupplierComAcctReqVo vo = new QuerySupplierComAcctReqVo();
        vo.setSupplyCompanyCode(supplierCode);
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        if (null != authToken) {
            vo.setCompanyCode(authToken.getCompanyCode());
        }
        return supplyCompanyAccountDao.selectListByQueryVO(vo);
    }

    @Override
    public ApplySupplyComAcctInfo2RespVO getApplySupplyCompanyAccountById(Long id) {
        log.info("ApplySupplyComAcctServiceImpl-getApplySupplyCompanyAccountById-传入参数是：[{}]", id);
        if (Objects.isNull(id)) {
            throw new GroundRuntimeException("id不能为空!");
        }
        ApplySupplyCompanyAccount account = applySupplyCompanyAccountMapper.selectByPrimaryKey(id);
        if (Objects.isNull(account)) {
            log.error("通过id查询申请供货单位账户详情失败，传入的id是：{}", id);
            throw new GroundRuntimeException("数据异常!");
        }
        try {
            ApplySupplyComAcctInfo2RespVO t = new ApplySupplyComAcctInfo2RespVO();
            BeanUtils.copyProperties(account, t);
            t.setApplySupplyCompanyName(account.getSupplyCompanyName());
            if (Objects.equals(1, account.getApplyType().intValue())) {
                //新增修改
                t.setApplyBy(account.getCreateBy());
                t.setApplyDate(account.getCreateTime());
            } else if (Objects.equals(2, account.getApplyType().intValue())) {
                t.setApplyBy(account.getUpdateBy());
                t.setApplyDate(account.getUpdateTime());
            }
            t.setModelType("账户");
            t.setModelTypeCode("3");
            t.setStatus(account.getApplyStatus().toString());
            t.setEnable(StatusTypeCode.DEL_FLAG.getStatus().equals(account.getDelFlag()) ? StatusTypeCode.DIS_ABLE.getName() : StatusTypeCode.EN_ABLE.getName());
            //封装日志数据
            OperationLogBean operationLogBean = new OperationLogBean(account.getApplyCompanyAccountCode(), null, ObjectTypeCode.APPLY_SUPPLY_COMPANY_ACCOUNT.getStatus(), null, null);
            List<LogData> log = operationLogService.selectListByVO(operationLogBean);
            t.setLogData(log);
            return t;
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("数据异常!");
        }
    }

    @Override
    public SupplyComAcctMainRespVO getSupplyCompanyAccountById(Long id) {
        log.info("ApplySupplyComAcctServiceImpl-getSupplyCompanyAccountById-传入参数是：[{}]", id);
        if (Objects.isNull(id)) {
            throw new GroundRuntimeException("id不能为空!");
        }
        SupplyCompanyAccount s = supplyCompanyAccountMapper.selectByPrimaryKey(id);
        if (Objects.isNull(s)) {
            log.error("通过id查询供货单位账户详情失败，传入的id是：{}", id);
            throw new GroundRuntimeException("数据异常!");
        }
        SupplyComAcctMainRespVO m = new SupplyComAcctMainRespVO();
        try {
            SupplyComAcctInfoRespVO t = new SupplyComAcctInfoRespVO();
            BeanUtils.copyProperties(s, t);
            m.setNewSupplyAccount(t);
            //封装日志数据
            List<LogData> log = operationLogService.selectListByVO(new OperationLogBean(s.getSupplyCompanyAccountCode(), null, ObjectTypeCode.SUPPLY_COMPANY_ACCOUNT.getStatus(), null, null));
            m.setLogData(log);
            //过滤  拿到修改的数据
            List<LogData> collect = log.stream().filter(o -> o.getHandleType().equals(HandleTypeCoce.UPDATE_SUPPLY_COMPANY_ACCOUNT.getStatus())).collect(Collectors.toList());
            if (CollectionUtils.isEmptyCollection(collect)) {
                return m;
            } else {
                LogData logData = collect.get(collect.size() - 1);
                String content = logData.getContent();
                //转回JSON
                SupplyComAcctInfoRespVO t1 = new SupplyComAcctInfoRespVO();
                SupplyCompanyAccount s1 = JSON.parseObject(content, SupplyCompanyAccount.class);
                BeanUtils.copyProperties(s1, t1);
                m.setOldSupplyAccount(t1);
                return m;
            }
        } catch (Exception e) {
            log.error("通过id查询供货单位账户详情失败，传入id={}", id);
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("数据异常!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelById(Long id) {
        log.info("ApplySupplyComAcctServiceImpl-cancelById-传入参数是：[{}]", id);
        if (Objects.isNull(id)) {
            throw new GroundRuntimeException("id不能为空!");
        }
        int temp;
        //首先查出id
        ApplySupplyCompanyAccount account = applySupplyCompanyAccountMapper.selectByPrimaryKey(id);
        if (Objects.isNull(account)) {
            log.error("通过id查询申请供货单位账户详情失败，传入的id是：{}", id);
            throw new GroundRuntimeException("数据异常!");
        }
        //拿到操作类型
        Byte applyType = account.getApplyType();
        //判断申请状态
        if (Objects.equals(account.getApplyStatus().intValue(), 1) || Objects.equals(account.getApplyStatus().intValue(), 0)) {
            //已经推送的
            if (Objects.equals(account.getApplyStatus().intValue(), 1)) {
                //TODO 调用接口推送取消的操作 成功后进行下一步操作
                WorkFlowVO w = new WorkFlowVO();
                w.setFormNo(account.getFormNo());
                WorkFlowRespVO workFlowRespVO = cancelWorkFlow(w);
                if (!workFlowRespVO.getSuccess()) {
                    throw new GroundRuntimeException("撤销失败!");
                }
            }
//            ApplySupplyCompanyAcctReqDTO s = new ApplySupplyCompanyAcctReqDTO();
//            s.setId(account.getId());
//            //设置状态
//            s.setApplyStatus((byte) 4);
//            temp = ((ApplySupplyComAcctService) AopContext.currentProxy()).cancelApplyData(s);
//
//            if (applyType.intValue() == 2) {
//                //修改操作 需要先把正式表的状态改了
//                SupplyCompanyAccountDTO s1 = new SupplyCompanyAccountDTO();
//                s.setApplyCode(account.getApplyCompanyAccountCode());
//                s.setApplyStatus((byte) 0);
//                temp = ((ApplySupplyComAcctService) AopContext.currentProxy()).cancelSupplyData(s1);
//            }
        } else {
            throw new GroundRuntimeException("该审核状态不允许取消!");
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Update
    public int cancelApplyData(ApplySupplyCompanyAcctReqDTO s) {
        log.info("ApplySupplyComAcctServiceImpl-cancelApplyData-传入参数是：[{}]", JSON.toJSONString(s));
        int temp;
        try {
            ApplySupplyCompanyAccount t = new ApplySupplyCompanyAccount();
            BeanUtils.copyProperties(s, t);
            temp = applySupplyCompanyAccountMapper.updateByPrimaryKeySelective(t);
            if (temp <= 0) {
                throw new GroundRuntimeException("取消申请供应单位账户失败");
            }
        } catch (Exception e) {
            log.error("取消申请供应单位账户失败,传入的对象是：{}", JSONObject.toJSONString(s));
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("取消申请供应单位账户失败");
        }
        return temp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Update
    public int cancelSupplyData(SupplyCompanyAccountDTO s1) {
        log.info("ApplySupplyComAcctServiceImpl-cancelSupplyData-传入参数是：[{}]", JSON.toJSONString(s1));
        int temp;
        try {
            SupplyCompanyAccount t1 = new SupplyCompanyAccount();
            BeanUtils.copyProperties(s1, t1);
            SupplyCompanyAccount entity = new SupplyCompanyAccount();
            entity.setApplyCompanyAccountCode(s1.getApplyCompanyAccountCode());
            temp = supplyCompanyAccountDao.updateByEntitySelective(t1, entity);
            if (temp <= 0) {
                throw new GroundRuntimeException("取消供应单位账户失败");
            }
        } catch (Exception e) {
            log.error("取消供应单位账户失败,传入的对象是：{}", JSONObject.toJSONString(s1));
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("取消供应单位账户失败");
        }
        return temp;
    }

    @Override
    public List<SupplyCompanyAccount> getSupplyCompanyAccountByCompanyCode(List<String> codes) {
        log.info("ApplySupplyComAcctServiceImpl-getSupplyCompanyAccountByCompanyCode-传入参数是：[{}]", JSON.toJSONString(codes));
        return supplyCompanyAccountMapper.selectByCode(codes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insideWorkFlowCallback(ApplySupplyCompanyAccount applySupplyCompanyAccount, WorkFlowCallbackVO vo) {
        if (Objects.isNull(applySupplyCompanyAccount)) {
            return HandlingExceptionCode.FLOW_CALL_BACK_FALSE;
        }
        if (applySupplyCompanyAccount.getApplyStatus().equals(ApplyStatus.APPROVAL.getNumber())) {
            applySupplyCompanyAccount.setApplyStatus(vo.getApplyStatus());
            if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
                SupplyCompanyAccount oldSupplyComAcct = supplyCompanyAccountDao.getSupplyCompanyAccount(applySupplyCompanyAccount.getApplyCompanyAccountCode());
                //通过插入正式数据
                SupplyCompanyAccount supplyCompanyAccount = new SupplyCompanyAccount();
                BeanCopyUtils.copy(applySupplyCompanyAccount, supplyCompanyAccount);
                supplyCompanyAccount.setAuditorBy(vo.getApprovalUserName());
                supplyCompanyAccount.setAuditorTime(new Date());
                supplyCompanyAccount.setApplyCompanyAccountCode(applySupplyCompanyAccount.getApplyCompanyAccountCode());
                if (null != oldSupplyComAcct) {
                    supplyCompanyAccount.setId(oldSupplyComAcct.getId());
                    supplyCompanyAccountMapper.updateByPrimaryKey(supplyCompanyAccount);
                } else {
                    //Long numberValue = encodingRuleDao.getNumberValue(8L);
                    String redisCode = codeUtils.getRedisCode("SUPPLY_COM_ACCT_CODE");
                    supplyCompanyAccount.setId(null);
                    supplyCompanyAccount.setSupplyCompanyAccountCode(redisCode);
                    log.info("供货单位账户保存正式数据成功");
                    //更新编码
                    //encodingRuleDao.updateNumberValue(numberValue, 8L);
                    supplyCompanyAccountMapper.insert(supplyCompanyAccount);
                }
            } else if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL_FAILED.getNumber())) {
                //驳回, 设置状态
                applySupplyCompanyAccount.setApplyStatus(vo.getApplyStatus());
                SupplyCompanyAccount oldSupplyComAcct = supplyCompanyAccountDao.getSupplyCompanyAccount(applySupplyCompanyAccount.getApplyCompanyAccountCode());
                if (null != oldSupplyComAcct) {
                    SupplyCompanyAccount account = new SupplyCompanyAccount();
                    account.setId(oldSupplyComAcct.getId());
                    account.setApplyStatus(ApplyStatus.APPROVAL_SUCCESS.getNumber());
                    supplyCompanyAccountMapper.updateByPrimaryKeySelective(account);
                }
            } else if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL.getNumber())) {
                //传入的是审批中，继续该流程
                return HandlingExceptionCode.FLOW_CALL_BACK_SUCCESS;
            } else if (vo.getApplyStatus().equals(ApplyStatus.REVOKED.getNumber())) {
                applySupplyCompanyAccount.setApplyStatus(vo.getApplyStatus());
                SupplyCompanyAccount oldSupplyComAcct = supplyCompanyAccountDao.getSupplyCompanyAccount(applySupplyCompanyAccount.getApplyCompanyAccountCode());
                if (null != oldSupplyComAcct) {
                    SupplyCompanyAccount account = new SupplyCompanyAccount();
                    account.setId(oldSupplyComAcct.getId());
                    account.setApplyStatus(ApplyStatus.APPROVAL_SUCCESS.getNumber());
                    supplyCompanyAccountMapper.updateByPrimaryKeySelective(account);
                }
            } else {
                return HandlingExceptionCode.FLOW_CALL_BACK_FALSE;
            }
        } else {
            return HandlingExceptionCode.FLOW_CALL_BACK_FALSE;
        }
        applySupplyCompanyAccount.setAuditorBy(vo.getAuditorBy());
        applySupplyCompanyAccount.setAuditorTime(vo.getAuditorTime());
        applySupplyCompanyAccountMapper.updateByPrimaryKey(applySupplyCompanyAccount);
        return HandlingExceptionCode.FLOW_CALL_BACK_SUCCESS;
    }

    @Override
    public List<ApplyListRespVo> queryApplyList(QueryApplyReqVo querySupplierReqVO) {
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        if (null != authToken) {
            querySupplierReqVO.setCompanyCode(authToken.getCompanyCode());
            //querySupplierReqVO.setApplyBy(authToken.getPersonName());
        }
        return applySupplyCompanyAcctDao.queryApplyList(querySupplierReqVO);
    }

    /**
     * 功能描述: 返回详情接口请求参数
     *
     * @param formNo
     * @return
     * @auther knight.xie
     * @date 2019/8/9 14:41
     */
    @Override
    public DetailRequestRespVo getInfoByForm(String formNo) {
        ApplySupplyCompanyAccount account = applySupplyCompanyAccountMapper.selectByFormNO(formNo);
        if (null == account) {
            throw new BizException(ResultCode.OBJECT_EMPTY_BY_FORMNO);
        }
        DetailRequestRespVo detailRequestRespVo = new DetailRequestRespVo();
        detailRequestRespVo.setApplyCode(account.getApplyCompanyAccountCode());
        detailRequestRespVo.setId(account.getId());
        detailRequestRespVo.setItemCode("3");
        return detailRequestRespVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String workFlowCallback(WorkFlowCallbackVO vo1) {
        log.info("ApplySupplyComAcctServiceImpl-workFlowCallback-传入参数是：[{}]", JSON.toJSONString(vo1));
        //通过编码查询实体
        WorkFlowCallbackVO vo = updateSupStatus(vo1);
        ApplySupplyCompanyAccount account = applySupplyCompanyAccountMapper.selectByFormNO(vo.getFormNo());
        if (Objects.isNull(account)) {
            log.info("该条申请数据不存在！formNo是：{}", vo.getFormNo());
            return "false";
        }
        HandleTypeCoce typeCoce = null;
        if (!account.getApplyStatus().equals(ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
            account.setApplyStatus(vo.getApplyStatus());
            if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
                typeCoce = HandleTypeCoce.APPROVAL_SUCCESS;
                //新增
                if (account.getApplyType().intValue() == 1) {
                    SupplyCompanyAccount s = new SupplyCompanyAccount();
                    //通过插入正式数据
                    BeanCopyUtils.copy(account, s);
                    //EncodingRule code = encodingRuleDao.getNumberingType("SUPPLY_COM_ACCT_CODE");
                    String code = codeUtils.getRedisCode("SUPPLY_COM_ACCT_CODE");
                    s.setId(null);
                    s.setSupplyCompanyAccountCode(code);
                    account.setSupplyCompanyAccountCode(code);
                    s.setApplyCompanyAccountCode(account.getApplyCompanyAccountCode());
                    s.setAuditorBy(vo.getApprovalUserName());
                    s.setAuditorTime(new Date());
                    supplyCompanyAccountMapper.insert(s);
                    account.setSupplyCompanyAccountCode(code);
                    log.info("供货单位账户保存正式数据成功");
                    //更新编码
                    //encodingRuleDao.updateNumberValue(code.getNumberingValue(), code.getId());
                    supplierCommonService.getInstance(s.getSupplyCompanyAccountCode(), HandleTypeCoce.ADD.getStatus(), ObjectTypeCode.SUPPLY_COMPANY_ACCOUNT.getStatus(), HandleTypeCoce.ADD_SUPPLY_COMPANY_ACCOUNT.getName(), null, HandleTypeCoce.ADD.getName(), account.getUpdateBy());
                } else if (account.getApplyType().intValue() == 2) {
                    //修改
                    SupplyCompanyAccount s2 = supplyCompanyAccountMapper.selectByApplyCode(account.getApplyCompanyAccountCode());
                    s2.setApplyStatus((byte) 0);
                    SupplyCompanyAccount s1 = new SupplyCompanyAccount();
                    BeanCopyUtils.copy(account, s1);
                    s1.setSupplyCompanyCode(s2.getSupplyCompanyCode());
                    s1.setId(s2.getId());
                    s1.setAuditorBy(vo.getApprovalUserName());
                    s1.setAuditorTime(new Date());
                    supplyCompanyAccountMapper.updateByPrimaryKeySelective(s1);
                    supplierCommonService.getInstance(s2.getSupplyCompanyAccountCode(), HandleTypeCoce.UPDATE.getStatus(), ObjectTypeCode.SUPPLY_COMPANY_ACCOUNT.getStatus(), HandleTypeCoce.UPDATE_SUPPLY_COMPANY_ACCOUNT.getName(), null, HandleTypeCoce.UPDATE.getName(), account.getUpdateBy());
                } else {
                    return "false";
                }
            } else if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL_FAILED.getNumber())) {
                typeCoce = HandleTypeCoce.APPROVAL_FAILED;
                //驳回, 设置状态
                //修改驳回后，数据还原
                SupplyCompanyAccount oldSupplyComAcct = supplyCompanyAccountDao.getSupplyCompanyAccount(account.getApplyCompanyAccountCode());
                if (null != oldSupplyComAcct) {
                    SupplyCompanyAccount account1 = new SupplyCompanyAccount();
                    account1.setId(oldSupplyComAcct.getId());
                    account1.setApplyStatus(ApplyStatus.APPROVAL_SUCCESS.getNumber());
                    supplyCompanyAccountMapper.updateByPrimaryKeySelective(account1);
                }
            } else if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL.getNumber())) {
                typeCoce = HandleTypeCoce.APPROVAL;
                //传入的是审批中，继续该流程
            } else if (vo.getApplyStatus().equals(ApplyStatus.REVOKED.getNumber())) {
                //传入的撤销
                typeCoce = HandleTypeCoce.REVOKED;
                SupplyCompanyAccount oldSupplyComAcct = supplyCompanyAccountDao.getSupplyCompanyAccount(account.getApplyCompanyAccountCode());
                if (null != oldSupplyComAcct) {
                    SupplyCompanyAccount account1 = new SupplyCompanyAccount();
                    account1.setId(oldSupplyComAcct.getId());
                    account1.setApplyStatus(ApplyStatus.APPROVAL_SUCCESS.getNumber());
                    supplyCompanyAccountMapper.updateByPrimaryKeySelective(account1);
                }
            } else {
                log.info("传入的审核状态不存在！状态是：{}", vo.getApplyStatus());
                return "false";
            }
        } else {
            log.info("该条申请数据已审核！formNo是：{}", vo.getFormNo());
            return "false";
        }
        account.setAuditorBy(vo.getApprovalUserName());
        account.setAuditorTime(new Date());
        applySupplyCompanyAccountMapper.updateByPrimaryKey(account);
        //判断审核状态，存日志信息
        ApplyStatus applyStatus = ApplyStatus.getApplyStatusByNumber(account.getApplyStatus());
        if (Objects.equals(ApplyStatus.APPROVAL, applyStatus)) {
            applyStatus = ApplyStatus.APPROVAL_SUCCESS;
        }
        String content = applyStatus.getContent().replace(Global.CREATE_BY, account.getUpdateBy()).replace(Global.AUDITOR_BY, vo.getApprovalUserName());
        supplierCommonService.getInstance(account.getApplyCompanyAccountCode(), typeCoce.getStatus(), ObjectTypeCode.APPLY_SUPPLY_COMPANY_ACCOUNT.getStatus(), content, null, typeCoce.getName(), vo.getApprovalUserName());
        return "success";
    }

}

package com.aiqin.bms.scmp.api.supplier.service.impl;

import com.aiqin.bms.scmp.api.base.AreaBasic;
import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.base.EncodingRuleType;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.common.*;
import com.aiqin.bms.scmp.api.config.AuthenticationInterceptor;
import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.dao.logisticscenter.LogisticsCenterAreaDao;
import com.aiqin.bms.scmp.api.supplier.dao.logisticscenter.LogisticsCenterDao;
import com.aiqin.bms.scmp.api.supplier.domain.LogisticsCenterEnum;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.EncodingRule;
import com.aiqin.bms.scmp.api.supplier.domain.request.logisticscenter.dto.LogisticsCenterAreaDTO;
import com.aiqin.bms.scmp.api.supplier.domain.request.logisticscenter.dto.LogisticsCenterDTO;
import com.aiqin.bms.scmp.api.supplier.domain.request.logisticscenter.vo.LogisticsCenterReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.logisticscenter.vo.QueryLogisticsCenterReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.logisticscenter.vo.UpdateLogisticsCenterReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.logisticscenter.LogisticsCenterAreaResVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.logisticscenter.LogisticsCenterResVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.logisticscenter.QueryLogisticsCenterResVo;
import com.aiqin.bms.scmp.api.supplier.service.LogisticsCenterService;
import com.aiqin.bms.scmp.api.util.*;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.aiqin.mgs.control.component.service.AreaBasicService;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 描述:
 *
 * @Author: Kt.w
 * @Date: 2018/12/25
 * @Version 1.0
 * @since 1.0
 */
@Service
public class LogisticsCenterServiceImpl implements LogisticsCenterService {

    @Autowired
    private LogisticsCenterDao logisticsCenterDao;

    @Autowired
    private EncodingRuleDao encodingRuleDao;

    @Autowired
    private LogisticsCenterAreaDao logisticsCenterAreaDao;
    @Resource
    private AreaBasicService areaBasicService;
    @Autowired
    private AreaBasicInfoServiceImpl areaBasicInfoService;

    @Resource
    private CodeUtils codeUtils;

    /**
     *  列表展示以及搜索
     * @param vo
     * @return
     */
    @Override
    public BasePage<QueryLogisticsCenterResVo> getLogisticsCenterList(QueryLogisticsCenterReqVo vo) {
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        if(null != authToken){
            vo.setCompanyCode(authToken.getCompanyCode());
        }
        PageHelper.startPage(vo.getPageNo(), vo.getPageSize());
        List<LogisticsCenterDTO> logisticsCenterDTOS = logisticsCenterDao.getLogisticsCenterList(vo);
        // 获取分页参数
        BasePage<QueryLogisticsCenterResVo> basePage = PageUtil.getPageList(vo.getPageNo(),logisticsCenterDTOS);
        try {
            basePage.setDataList(BeanCopyUtils.copyList(logisticsCenterDTOS,QueryLogisticsCenterResVo.class));
            return basePage;
        }catch (Exception e){

        }
        throw new GroundRuntimeException("分页查询失败");
    }

    /**
     * 请求保存转化实体
     * @param logisticsCenterReqVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    public HttpResponse<Integer> saveLogisticsCenter(LogisticsCenterReqVo logisticsCenterReqVo) {

        // 验证名字是否重复
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        String companyCode = "";
        if(null != authToken){
            companyCode = authToken.getCompanyCode();
        }
        String logisticsCenterName = logisticsCenterReqVo.getLogisticsCenterName();
        Integer integer = logisticsCenterDao.checkName(logisticsCenterName,null,companyCode);
        if(integer>0){
            throw new GroundRuntimeException("仓库名称不能重复");
        }
       LogisticsCenterDTO  logisticsCenterDTO = new LogisticsCenterDTO();
       BeanCopyUtils.copy(logisticsCenterReqVo,logisticsCenterDTO);
        //设置库房编码
        //EncodingRule encodingRule = encodingRuleDao.getNumberingType(EncodingRuleType.LOGISTICS_CENTER_CODE);
        String redisCode = codeUtils.getRedisCode(EncodingRuleType.LOGISTICS_CENTER_CODE);
        logisticsCenterDTO.setLogisticsCenterCode(redisCode);
        // 更新数据库编码尺度
        //encodingRuleDao.updateNumberValue(encodingRule.getNumberingValue(),  encodingRule.getId());
        //设置采购组主体的删除状态，启用禁用状态
        logisticsCenterDTO.setDelFlag(Byte.parseByte("0"));
        logisticsCenterDTO.setEnable(Byte.parseByte("0"));
        //保存物流中心主体
        int k = ((LogisticsCenterService) AopContext.currentProxy()).insertSelective(logisticsCenterDTO);
        if (k>0){
            try {
                if(CollectionUtils.isNotEmptyCollection(logisticsCenterReqVo.getLogisticsCenterAreaReqVos())){
                    List<LogisticsCenterAreaDTO> list = BeanCopyUtils.copyList(logisticsCenterReqVo.getLogisticsCenterAreaReqVos(),LogisticsCenterAreaDTO.class);
                    //设置关联编码
                    list.stream().forEach(purchase -> purchase.setLogisticsCenterCode(String.valueOf(logisticsCenterDTO.getLogisticsCenterCode())));
                    //设置启用禁用状态
                    list.stream().forEach(purchase -> purchase.setEnable(Byte.parseByte("0")));
                    // 设置逻辑删除状态
                    list.stream().forEach(purchase -> purchase.setDelFlag(Byte.parseByte("0")));
                    //批量插入
                    int kp = ((LogisticsCenterService)AopContext.currentProxy()).saveList(list);
                    if(kp>0){
                        return HttpResponse.success(kp);
                    }else {
                        throw new GroundRuntimeException("仓库服务范围新增失败");
                    }
                }else{
                    return HttpResponse.success(k);
                }
            }catch (Exception e){
                throw new GroundRuntimeException("仓库服务范围新增失败");
            }

        }else {
            throw new GroundRuntimeException("物流中心新增失败");
        }

    }

    /**
     * 通过id返回物流中心实体
     * @param id
     * @return
     */
    @Override
    public LogisticsCenterResVo getLogisticsCenter(Long id) {
        LogisticsCenterResVo logisticsCenterResVo = new LogisticsCenterResVo();

        try {
            LogisticsCenterDTO logisticsCenterDTO = logisticsCenterDao.selectByPrimaryKey(id);
            BeanCopyUtils.copy(logisticsCenterDTO,logisticsCenterResVo);
          // 根据编码去查询服务范围
           List< LogisticsCenterAreaDTO> logisticsCenterAreaDTOS =logisticsCenterAreaDao.disEnableByLogisticsCenterCode(logisticsCenterResVo.getLogisticsCenterCode());
           logisticsCenterResVo.setLogisticsCenterAreaResVos(BeanCopyUtils.copyList(logisticsCenterAreaDTOS, LogisticsCenterAreaResVo.class));
           return logisticsCenterResVo;

        }catch (Exception e){
            throw new GroundRuntimeException("查询失败");
        }
    }

    /**
     * 请求修改转化实体
     * @param updateLogisticsCenterReqVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    public HttpResponse<Integer> updateLogisticsCenter(UpdateLogisticsCenterReqVo updateLogisticsCenterReqVo) {

        //验证名字是否冲突
        String logisticsCenterName = updateLogisticsCenterReqVo.getLogisticsCenterName();
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        String companyCode = "";
        if(null != authToken){
            companyCode = authToken.getCompanyCode();
        }
        Integer integer = logisticsCenterDao.checkName(logisticsCenterName,updateLogisticsCenterReqVo.getLogisticsCenterCode(),companyCode);
        if(integer>0){
            throw new GroundRuntimeException("仓库名称不能重复");
        }
        LogisticsCenterDTO  logisticsCenterDTO = new LogisticsCenterDTO();
        BeanCopyUtils.copy(updateLogisticsCenterReqVo,logisticsCenterDTO);
        // 更新物流中心主体
        int k  = ((LogisticsCenterService)AopContext.currentProxy()).updateByPrimaryKeySelective(logisticsCenterDTO);
        if(k>0){
            //  服务区范围转化实体
            //先查再删后增
            List<LogisticsCenterAreaDTO> list =  logisticsCenterAreaDao.selectByCode(updateLogisticsCenterReqVo.getLogisticsCenterCode());
            if(CollectionUtils.isNotEmptyCollection(list)){
                int i = logisticsCenterAreaDao.deleteByCode(updateLogisticsCenterReqVo.getLogisticsCenterCode());
                if (i != list.size()) {
                    throw new GroundRuntimeException("修改失败");
                }
            }
            if(CollectionUtils.isNotEmptyCollection(updateLogisticsCenterReqVo.getLogisticsCenterAreaReqVoList())){
                List<LogisticsCenterAreaDTO> list1 = BeanCopyUtils.copyList(updateLogisticsCenterReqVo.getLogisticsCenterAreaReqVoList(),LogisticsCenterAreaDTO.class);
                boolean equals = Objects.equals(updateLogisticsCenterReqVo.getEnable(), 1);
                //设置关联编码
                list1.forEach(o->{
                    o.setLogisticsCenterCode(updateLogisticsCenterReqVo.getLogisticsCenterCode());
                    o.setDelFlag((byte)0);
                    if (equals) {
                        o.setEnable((byte)1);
                    }
                });
                //批量插入
                int kp = ((LogisticsCenterService)AopContext.currentProxy()).saveList(list1);
                if(kp!=list1.size()){
                    throw new GroundRuntimeException("仓库服务范围新增失败");
                }
                return HttpResponse.success(kp);
            }else{
                return HttpResponse.success(k);
            }

        }
        throw new GroundRuntimeException("修改失败");
    }

    /**
     * 保存物流中心主体
     * @param record
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @Save
    public int insertSelective(LogisticsCenterDTO record) {
        try{
            AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
            if(null != authToken){
                record.setCompanyCode(authToken.getCompanyCode());
                record.setCompanyName(authToken.getCompanyName());
            }
            return logisticsCenterDao.insertSelective(record);
        }catch (Exception e){
            throw new GroundRuntimeException("仓库新增失败");
        }
    }


    /**
     * 批量插入物流中心服务范围
     * @param logisticsCenterAreaDTOS
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @SaveList
    public int saveList(List<LogisticsCenterAreaDTO> logisticsCenterAreaDTOS) {
        try{
            return logisticsCenterAreaDao.saveList(logisticsCenterAreaDTOS);
        }catch (Exception e){
            throw new GroundRuntimeException("仓库服务区新增失败");
        }
    }


    /**
     * 更新物流中心主体
     * @param record
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @Update
    public int updateByPrimaryKeySelective(LogisticsCenterDTO record) {
        try {
         return logisticsCenterDao.updateByCodeSelective(record);
        }catch (Exception e){
            throw new GroundRuntimeException("仓库修改失败");
        }
    }

    /**
     * 批量更新物流中心服务范围
     * @param logisticsCenterAreaDTOS
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @UpdateList
    public int updateList(List<LogisticsCenterAreaDTO> logisticsCenterAreaDTOS) {
        try {
            return logisticsCenterAreaDao.updateList(logisticsCenterAreaDTOS);
        }catch (Exception e){
            throw new GroundRuntimeException("仓库服务区修改失败");
        }
    }

    /**
     * 物流中心API
     * @return
     */
    @Override
    public List<LogisticsCenterResVo> getLogisticsCenterAPI(String companyCode) {
        try {
            if(StringUtils.isBlank(companyCode)){
                AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
                if(null != authToken){
                    companyCode = authToken.getCompanyCode();
                }
            }
            List<LogisticsCenterDTO>  logisticsCenterDTOS = logisticsCenterDao.getEnableLogisticsCenterList(companyCode);
            List<LogisticsCenterResVo> logisticsCenterResVos = BeanCopyUtils.copyList(logisticsCenterDTOS,LogisticsCenterResVo.class);
            for (LogisticsCenterResVo vo : logisticsCenterResVos) {
                vo.setLogisticsCenterAreaResVos(BeanCopyUtils.copyList(logisticsCenterAreaDao.enableByLogisticsCenterCode(vo.getLogisticsCenterCode()),LogisticsCenterAreaResVo.class));
            }
            return logisticsCenterResVos;
        } catch (Exception e) {
            throw new GroundRuntimeException("仓库查询失败");
        }
    }


    /**
     * 查询所有省份并且返回全国信息
     * @return
     */
    @Override
    public HttpResponse getProvinceList() {
        try {
            AreaBasic areaBasic = new AreaBasic();
            areaBasic.setArea_id(LogisticsCenterEnum.CHINA.getStatus());
            areaBasic.setArea_name(LogisticsCenterEnum.CHINA.getName());
            HttpResponse result = areaBasicService.areaTypeInfo(3);
            if (result != null && result.getData()!=null){
                List<AreaBasic> areaBasicList = (List<AreaBasic>)result.getData();
                areaBasicList.add(0,areaBasic);
                return HttpResponse.success(areaBasicList);
            }

            throw new BizException(ResultCode.NO_HAVE_INFO_ERROR);
        } catch (GroundRuntimeException e){
            throw new BizException(ResultCode.NO_HAVE_INFO_ERROR);
        }
    }

    /**
     * 查询市
     * @param provinceId
     * @return
     */
    @Override
    public HttpResponse<AreaBasic> getCityList(String provinceId) {
        try {
            AreaBasic areaBasic = new AreaBasic();
            HttpResponse result = areaBasicService.selectAreaList(provinceId);
            // 如果是北京
            if(LogisticsCenterEnum.BEI_JING.getStatus().equals(provinceId)){
                areaBasic.setParent_id(provinceId);
                areaBasic.setArea_id(LogisticsCenterEnum.BEI_JING_TOTAL.getStatus());
                areaBasic.setArea_name(LogisticsCenterEnum.BEI_JING_TOTAL.getName());
            }
            // 如果是天津
            else if (LogisticsCenterEnum.TIAN_JIN.getStatus().equals(provinceId)){
                areaBasic.setParent_id(provinceId);
                areaBasic.setArea_id(LogisticsCenterEnum.TIAN_JIN_TOTAL.getStatus());
                areaBasic.setArea_name(LogisticsCenterEnum.TIAN_JIN_TOTAL.getName());

            }//如果是上海
            else if (LogisticsCenterEnum.SHANG_HAI.getStatus().equals(provinceId)){
                areaBasic.setParent_id(provinceId);
                    areaBasic.setArea_id(LogisticsCenterEnum.SHANG_HAI_TOTAL.getStatus());
                    areaBasic.setArea_name(LogisticsCenterEnum.SHANG_HAI_TOTAL.getName());
            } // 如果是重亲
            else if (LogisticsCenterEnum.CHONG_QING.getStatus().equals(provinceId)){
                areaBasic.setParent_id(provinceId);
                areaBasic.setArea_id(LogisticsCenterEnum.CHONG_QING_TOTAL.getStatus());
                areaBasic.setArea_name(LogisticsCenterEnum.CHONG_QING_TOTAL.getName());
            }//如果是全国
            else if(LogisticsCenterEnum.CHINA.getStatus().equals(provinceId)){
                List<AreaBasic> areaBasicList = new ArrayList<>();
                areaBasic.setParent_id(provinceId);
                areaBasic.setArea_id(LogisticsCenterEnum.CHINA.getStatus());
                areaBasic.setArea_name(LogisticsCenterEnum.CHINA.getName());
                areaBasicList.add(0,areaBasic);
                return HttpResponse.success(areaBasicList);
            }else {
                areaBasic.setParent_id(provinceId);
                areaBasic.setArea_id(LogisticsCenterEnum.PROVINCE.getStatus());
                areaBasic.setArea_name(LogisticsCenterEnum.PROVINCE.getName());

            }
            if (result != null && result.getData()!=null){
                List<AreaBasic> areaBasicList = (List<AreaBasic>)result.getData();
                areaBasicList.add(0,areaBasic);
                return HttpResponse.success(areaBasicList);
            }

            return HttpResponse.failure(ResultCode.NO_HAVE_INFO_ERROR);
        } catch (GroundRuntimeException e) {
            return HttpResponse.failure(ResultCode.NO_HAVE_INFO_ERROR);
        }
    }

    @Override
    public HttpResponse<List<AreaBasic>> getAreaInfo() {
        HttpResponse<List<AreaBasic>> treeList = areaBasicInfoService.getTreeList();
        List<AreaBasic> tempData = treeList.getData();
        String s = JSON.toJSONString(tempData);
        List<AreaBasic> data = JSON.parseArray(s, AreaBasic.class);
        data.forEach(o->{
                AreaBasic area = new AreaBasic();
                if(Objects.nonNull(LogisticsCenterEnum.getAllStatus().get(o.getArea_id()))){
                        area.setParent_id(o.getArea_id());
                        area.setArea_id(LogisticsCenterEnum.getAllStatus().get(o.getArea_id()).getNextStatus());
                        area.setArea_name(LogisticsCenterEnum.getAllStatus().get(o.getArea_id()).getNextName());
                        o.getChildren().add(0,area);
                    }else {
                        area.setParent_id(o.getArea_id());
                        area.setArea_id(LogisticsCenterEnum.PROVINCE.getStatus());
                        area.setArea_name(LogisticsCenterEnum.PROVINCE.getName());
                    if (Objects.isNull(o.getChildren())) {
                        //台湾单独加一个全省
                        List<AreaBasic> areaBasics = Lists.newArrayList();
                        o.setChildren(areaBasics);
                    }
                    o.getChildren().add(0,area);
                }
            });
        //增加全国
        AreaBasic a = new AreaBasic();
        a.setArea_id(LogisticsCenterEnum.CHINA.getStatus());
        a.setArea_name(LogisticsCenterEnum.CHINA.getName());
        a.setParent_id(LogisticsCenterEnum.CHINA.getStatus());
        a.setParent_area_name(LogisticsCenterEnum.CHINA.getName());
        List<AreaBasic> areaBasics = Lists.newArrayList();
        AreaBasic a2 = new AreaBasic();
        //全国下面的全省
        a2.setArea_id(LogisticsCenterEnum.PROVINCE.getStatus());
        a2.setArea_name(LogisticsCenterEnum.PROVINCE.getName());
        a2.setParent_id(LogisticsCenterEnum.PROVINCE.getStatus());
        a2.setParent_area_name(LogisticsCenterEnum.PROVINCE.getName());
        areaBasics.add(a2);
        a.setChildren(areaBasics);
        data.add(0,a);
        return HttpResponse.success(data);
    }

    @Override
    public Map<String,LogisticsCenterDTO> selectByNameList(Set<String> warehouseList, String companyCode) {
        return logisticsCenterDao.selectByCenterNames(warehouseList,companyCode);
    }
}

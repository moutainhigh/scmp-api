package com.aiqin.bms.scmp.api.product.service.impl;

import com.aiqin.bms.scmp.api.api.store.StoreApi;
import com.aiqin.bms.scmp.api.base.*;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.*;
import com.aiqin.bms.scmp.api.config.AuthenticationInterceptor;
import com.aiqin.bms.scmp.api.constant.CommonConstant;
import com.aiqin.bms.scmp.api.product.dao.ProductSkuDao;
import com.aiqin.bms.scmp.api.product.domain.dto.salearea.ApplyProductSkuSaleAreaMainDTO;
import com.aiqin.bms.scmp.api.product.domain.dto.salearea.ProductSkuSaleAreaMainDTO;
import com.aiqin.bms.scmp.api.product.domain.dto.salearea.ProductSkuSaleAreaMainDraftDTO;
import com.aiqin.bms.scmp.api.product.domain.excel.SaleSkuInfoExport;
import com.aiqin.bms.scmp.api.product.domain.excel.SkuSaleAreaImport;
import com.aiqin.bms.scmp.api.product.domain.pojo.*;
import com.aiqin.bms.scmp.api.product.domain.product.apply.ProductApplyInfoRespVO;
import com.aiqin.bms.scmp.api.product.domain.product.apply.ProductSaleAreaApplyVO;
import com.aiqin.bms.scmp.api.product.domain.request.product.apply.QueryProductApplyRespVO;
import com.aiqin.bms.scmp.api.product.domain.request.salearea.*;
import com.aiqin.bms.scmp.api.product.domain.response.product.apply.QueryProductApplyReqVO;
import com.aiqin.bms.scmp.api.product.domain.response.salearea.*;
import com.aiqin.bms.scmp.api.product.mapper.*;
import com.aiqin.bms.scmp.api.product.service.ProductSaleAreaService;
import com.aiqin.bms.scmp.api.product.service.SkuInfoService;
import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.EncodingRule;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.SupplierOperationLog;
import com.aiqin.bms.scmp.api.supplier.domain.request.OperationLogBean;
import com.aiqin.bms.scmp.api.supplier.domain.response.LogData;
import com.aiqin.bms.scmp.api.supplier.domain.response.apply.DetailRequestRespVo;
import com.aiqin.bms.scmp.api.supplier.service.OperationLogService;
import com.aiqin.bms.scmp.api.supplier.service.SupplierCommonService;
import com.aiqin.bms.scmp.api.util.*;
import com.aiqin.bms.scmp.api.util.excel.exception.ExcelException;
import com.aiqin.bms.scmp.api.util.excel.utils.ExcelUtil;
import com.aiqin.bms.scmp.api.workflow.annotation.WorkFlowAnnotation;
import com.aiqin.bms.scmp.api.workflow.enumerate.WorkFlow;
import com.aiqin.bms.scmp.api.workflow.helper.WorkFlowHelper;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowCallbackVO;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowVO;
import com.aiqin.bms.scmp.api.workflow.vo.response.WorkFlowRespVO;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.Project;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.aiqin.mgs.control.component.service.AreaBasicService;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description:
 *
 * @author: NullPointException
 * @date: 2019-05-09
 * @time: 17:07
 */
@Service
@Slf4j
@WorkFlowAnnotation(WorkFlow.APPLY_SALE_AREA)
public class ProductSaleAreaServiceImpl extends BaseServiceImpl implements ProductSaleAreaService, WorkFlowHelper {
    @Autowired
    private ProductSkuSaleAreaDraftMapper productSkuSaleAreaDraftMapper;
    @Autowired
    private ProductSkuDao productSkuDao;
    @Autowired
    private ProductSkuSaleAreaMainDraftMapper productSkuSaleAreaMainDraftMapper;
    @Autowired
    private ProductSkuSaleAreaInfoDraftMapper productSkuSaleAreaInfoDraftMapper;
    @Autowired
    private ApplyProductSkuSaleAreaMapper applyProductSkuSaleAreaMapper;
    @Autowired
    private ApplyProductSkuSaleAreaInfoMapper applyProductSkuSaleAreaInfoMapper;
    @Autowired
    private EncodingRuleDao encodingRuleDao;
    @Autowired
    private WorkFlowBaseUrl workFlowBaseUrl;
    @Autowired
    private ProductSkuSaleAreaMapper productSkuSaleAreaMapper;
    @Autowired
    private ProductSkuSaleAreaInfoMapper productSkuSaleAreaInfoMapper;
    @Autowired
    private ProductSkuSaleAreaChannelDraftMapper productSkuSaleAreaChannelDraftMapper;
    @Autowired
    private ApplyProductSkuSaleAreaMainMapper applyProductSkuSaleAreaMainMapper;
    @Autowired
    private ApplyProductSkuSaleAreaChannelMapper applyProductSkuSaleAreaChannelMapper;
    @Autowired
    private ProductSkuSaleAreaChannelMapper productSkuSaleAreaChannelMapper;
    @Autowired
    private ProductSkuSaleAreaMainMapper productSkuSaleAreaMainMapper;
    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private AreaBasicService areaBasicService;
    @Autowired
    private StoreApi storeApi;
    @Autowired
    private SupplierCommonService supplierCommonService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private CodeUtils codeUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addSaleArea(ProductSkuSaleAreaMainReqVO request) throws Exception {
        AuthToken currentAuthToken = AuthenticationInterceptor.getCurrentAuthToken();
        request.setCompanyCode(currentAuthToken.getCompanyCode());
        request.setCreateBy(currentAuthToken.getPersonName());
        request.setCompanyName(currentAuthToken.getCompanyName());
        request.setCreateTime(new Date());
        if (StringUtils.isNotBlank(request.getOfficialCode())) {
            request.setUpdateBy(currentAuthToken.getPersonName());
            request.setUpdateTime(new Date());
        }
        if (StringUtils.isBlank(request.getCode())) {
            request.setBeDisable(0);
        }
        //数据本身验重
        validateRepeat(request);
//        //验证名称
//        List<ProductSkuSaleAreaMain> draftName = productSkuSaleAreaMainDraftMapper.selectByName(request.getName());
//        if (CollectionUtils.isNotEmpty(draftName)) {
//            throw new BizException(MessageId.create(Project.SUPPLIER_API,98,"名称重复保存失败"));
//        }
        List<ProductSkuSaleAreaMain> applyName = applyProductSkuSaleAreaMainMapper.selectByName(request.getName());
        if (CollectionUtils.isNotEmpty(applyName)) {
            throw new BizException(MessageId.create(Project.SUPPLIER_API, 98, "名称重复保存失败"));
        }
//        //首先申请表中试是否有申请中的数据
//        if(Objects.nonNull(request.getOfficialCode())){
//           ApplyProductSkuSaleAreaMain apply =  applyProductSkuSaleAreaMainMapper.selectByOfficialCode(ApplyStatus.APPROVAL.getNumber().intValue(),request.getOfficialCode());
//           if(Objects.nonNull(apply)){
//               throw new BizException(MessageId.create(Project.SUPPLIER_API,98,"已有发起修改的数据，无法保存！"));
//           }
//        }
//        //判断草稿表中是否有这条数据
//        List<String> skuCodes = request.getSkuList().stream().map(ProductSaleAreaReqVO::getSkuCode).distinct().collect(Collectors.toList());
//        List<ProductSkuSaleAreaDraft> repeat = productSkuSaleAreaDraftMapper.selectBySkuCodes(skuCodes);
//        //key:skuCode+供货渠道编码+供应商编码apper.selec
//        Map<String, ProductSkuSaleAreaDraft> repeatMap = repeat.stream().collect(Collectors.toMap(o -> o.getSkuCode() + o.getCategoriesSupplyChannelsCode() + Optional.ofNullable(o.getDirectDeliverySupplierCode()).orElse(""), Function.identity()));
//        StringBuilder sb = new StringBuilder();
//        request.getSkuList().forEach(o->{
//            ProductSkuSaleAreaDraft draft = repeatMap.get(o.getSkuCode() + o.getCategoriesSupplyChannelsCode() + Optional.ofNullable(o.getDirectDeliverySupplierCode()).orElse(""));
//           if(Objects.nonNull(draft)){
//                sb.append("该条数据下sku为").append(o.getSkuCode()).append("供货渠道类型为").append(o.getCategoriesSupplyChannelsName()).append("的数据已存在于临时表，无法提交！ ");
//           }
//        });
//        if (StringUtils.isNotBlank(sb.toString())) {
//            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, sb.toString()));
//        }
//        //判断申请表中是否有该条数据
//        List<ApplyProductSkuSaleArea> applyRepeat = applyProductSkuSaleAreaMapper.selectBySkuCodes(skuCodes,getUser().getCompanyCode());
//        StringBuilder sb2 = new StringBuilder();
//        //key:skuCode+供货渠道编码+供应商编码
//        Map<String, ApplyProductSkuSaleArea> applyRepeatMap = applyRepeat.stream().collect(Collectors.toMap(o -> o.getSkuCode() + o.getCategoriesSupplyChannelsCode() + Optional.ofNullable(o.getDirectDeliverySupplierCode()).orElse(""), Function.identity()));
//        request.getSkuList().forEach(o->{
//            ApplyProductSkuSaleArea draft = applyRepeatMap.get(o.getSkuCode() + o.getCategoriesSupplyChannelsCode() + Optional.ofNullable(o.getDirectDeliverySupplierCode()).orElse(""));
//            if(Objects.nonNull(draft)){
//                sb2.append("该条数据下sku为").append(o.getSkuCode()).append("供货渠道类型为").append(o.getCategoriesSupplyChannelsName()).append("的数据已存在于申请表，无法提交！ ");
//            }
//        });
//        if (StringUtils.isNotBlank(sb2.toString())) {
//            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, sb2.toString()));
//        }


        //保存数据
        return saveData(request);
    }

    private void validateRepeat(ProductSkuSaleAreaMainReqVO request) {
        //判断重复
        List<ProductSkuSaleAreaInfoReqVO> areaList = request.getAreaList();
        Set<String> objects = Sets.newHashSet();
        areaList.forEach(o -> {
            if (!objects.add(o.getCode())) {
                throw new BizException(MessageId.create(Project.SUPPLIER_API, 98, "数据异常，请检查禁用和启用的销售区域是否有交叉"));
            }
        });
//        List<ProductSaleAreaReqVO> skuList = request.getSkuList();
//        Set<Object> objects1 = Sets.newHashSet();
//        skuList.forEach(o->{
//            if(!objects1.add(o.getSkuCode()+o.getCategoriesSupplyChannelsCode()+o.getDirectDeliverySupplierCode())){
//                throw new BizException(MessageId.create(Project.SUPPLIER_API,98,"数据异常，请检查sku信息是否有重复！"));
//            }
//            //判断是否为空
//            if(Objects.isNull(o.getCategoriesSupplyChannelsCode())){
//                throw new BizException(ResultCode.DATA_NOT_COMPLETE);
//            }
//            if(CommonConstant.SUPPLY_CHANNEL_TYPE_DIRECT_DELIVERY.equals(o.getCategoriesSupplyChannelsCode())){
//                if(Objects.isNull(o.getDirectDeliverySupplierCode())){
//                    throw new BizException(ResultCode.DATA_NOT_COMPLETE);
//                }
//            }
//        });
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveData(ProductSkuSaleAreaMainReqVO request) throws Exception {
        //拼装实体
        ProductSkuSaleAreaMainDraft copy = BeanCopyUtils.copy(request, ProductSkuSaleAreaMainDraft.class);
        //获取编码
        //EncodingRule numberingType = encodingRuleDao.getNumberingType(EncodingRuleType.SALE_AREA_CODE);
        String redisCode = codeUtils.getRedisCode(EncodingRuleType.SALE_AREA_CODE);
        String code = "SAD" +redisCode;
        copy.setCode(code);
        List<ProductSkuSaleAreaInfoDraft> productSkuSaleAreaInfoDrafts = BeanCopyUtils.copyList(request.getAreaList(), ProductSkuSaleAreaInfoDraft.class);
        //设置主表编码
        productSkuSaleAreaInfoDrafts.forEach(o -> {
            o.setMainCode(code);
            o.setCompanyCode(request.getCompanyCode());
            o.setCompanyName(request.getCompanyName());
        });
//        List<ProductSkuSaleAreaDraft> skuSaleAreaDrafts = BeanCopyUtils.copyList(request.getSkuList(), ProductSkuSaleAreaDraft.class);
//        skuSaleAreaDrafts.forEach(
//                o ->{
//                    o.setCode(code);
//                    o.setBeDisable(request.getBeDisable());
//                    o.setCompanyCode(request.getCompanyCode());
//                    o.setCompanyName(request.getCompanyName());
//                    o.setCreateBy(request.getCreateBy());
//                    o.setCreateTime(request.getCreateTime());
//                }
//        );
        List<ProductSkuSaleAreaChannelDraft> channelDrafts = BeanCopyUtils.copyList(request.getChannelList(), ProductSkuSaleAreaChannelDraft.class);
        channelDrafts.forEach(o -> {
            o.setCode(code);
            o.setCompanyCode(request.getCompanyCode());
            o.setCompanyName(request.getCompanyName());
        });
        //保存数据
        saveDraftData(copy, productSkuSaleAreaInfoDrafts, channelDrafts);
        //更新编码
        //encodingRuleDao.updateNumberValue(numberingType.getNumberingValue(), numberingType.getId());

        //新增日志
        SupplierOperationLog supplierOperationLog = new SupplierOperationLog();
        supplierOperationLog.setObjectId(code);
        supplierOperationLog.setHandleType(new Byte(String.valueOf(0)));
        supplierOperationLog.setObjectType(ObjectTypeCode.SALE_AREA.getStatus());
        supplierOperationLog.setHandleName("新增");
        supplierOperationLog.setDelFlag(new Byte(String.valueOf(0)));
        operationLogService.insert(supplierOperationLog);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDraftData(ProductSkuSaleAreaMainDraft copy, List<ProductSkuSaleAreaInfoDraft> productSkuSaleAreaInfoDrafts, List<ProductSkuSaleAreaChannelDraft> channelDrafts) {
       if(!ObjectUtils.equals(null,copy)){
         int insert = productSkuSaleAreaMainDraftMapper.insert(copy);
         if (insert <= 0) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存主表数据失败"));
        }
       }
        //插入sku信息
//        int i = productSkuSaleAreaDraftMapper.insertBatch(skuSaleAreaDrafts);
//        if (i != skuSaleAreaDrafts.size()) {
//            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存sku信息失败！"));
//        }
        //插入区域数据
        int j = productSkuSaleAreaInfoDraftMapper.insertBatch(productSkuSaleAreaInfoDrafts);
        if (j != productSkuSaleAreaInfoDrafts.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存区域信息失败！"));
        }
        //插入渠道数据
        if (CollectionUtils.isNotEmpty(channelDrafts)) {
            int k = productSkuSaleAreaChannelDraftMapper.insertBatch(channelDrafts);
            if (k != channelDrafts.size()) {
                throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存区域信息失败！"));
            }
        }
    }

    @Override
    public BasePage<QueryProductSaleAreaMainRespVO> queryListForOfficial(QueryProductSaleAreaMainReqVO request) {
        request.setCompanyCode(getUser().getCompanyCode());
        List<Long> longs = productSkuSaleAreaMainMapper.selectListByQueryVoCount(request);
        if (CollectionUtils.isEmpty(longs)) {
            return PageUtil.getPageList(request.getPageNo(), Lists.newArrayList());
        }
        List<QueryProductSaleAreaMainRespVO> respVos = productSkuSaleAreaMainMapper.selectListByQueryVo(PageUtil.myPage(longs, request));
        return PageUtil.getPageList(request.getPageNo(), request.getPageSize(), longs.size(), respVos);
    }

    @Override
    public List<QueryProductSaleAreaMainRespVO> queryListForDraft(String companyCode) {
        if (StringUtils.isBlank(companyCode)) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "公司编码不能为空！"));
        }
        return productSkuSaleAreaMainDraftMapper.selectAllDraftData(companyCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addSaleAreaApply(ApplySaleAreaReqVO reqVO) {
        //获取登录人
        AuthToken currentAuthToken = getUser();
        //通过编码查询出数据
        List<ProductSkuSaleAreaMainDraftDTO> dtos = productSkuSaleAreaMainDraftMapper.selectDataByCodes(reqVO.getAreaCodes());
        List<ProductSkuSaleAreaDraft> skuList = Lists.newArrayList();
        List<ProductSkuSaleAreaInfoDraft> areaList = Lists.newArrayList();
        List<ProductSkuSaleAreaChannelDraft> channelList = Lists.newArrayList();
        dtos.forEach(o -> {
            skuList.addAll(o.getSkuList());
            areaList.addAll(o.getAreaList());
            channelList.addAll(o.getChannelList());
        });
        if (CollectionUtils.isEmpty(dtos)) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "获取数据异常，提交失败"));
        }
        //主表数据
        List<ApplyProductSkuSaleAreaMain> temps = null;
        temps = BeanCopyUtils.copyList(dtos, ApplyProductSkuSaleAreaMain.class);
        //附表数据
        List<ApplyProductSkuSaleAreaInfo> areaInfoList = null;
        List<ApplyProductSkuSaleAreaChannel> channelInfoList = null;
        List<ApplyProductSkuSaleArea> skuInfoList = null;
        areaInfoList = BeanCopyUtils.copyList(areaList, ApplyProductSkuSaleAreaInfo.class);
        channelInfoList = BeanCopyUtils.copyList(channelList, ApplyProductSkuSaleAreaChannel.class);
        skuInfoList = BeanCopyUtils.copyList(skuList, ApplyProductSkuSaleArea.class);
        //EncodingRule numberingType = encodingRuleDao.getNumberingType(EncodingRuleType.APPLY_SALE_AREA_CODE);
        String redisCode = codeUtils.getRedisCode(EncodingRuleType.APPLY_SALE_AREA_CODE);
        String code = "SAA" + redisCode;
        String formNo = "SAA" + IdSequenceUtils.getInstance().nextId();
        //封装数据
        temps.forEach(o -> {
            o.setCreateBy(currentAuthToken.getPersonName());
            o.setApplyCode(code);
            o.setCreateTime(new Date());
            o.setFormNo(formNo);
            o.setSelectionEffectiveTime(reqVO.getSelectionEffectiveTime());
            o.setSelectionEffectiveStartTime(reqVO.getSelectionEffectiveStartTime());
            o.setApplyStatus(ApplyStatus.APPROVAL.getNumber().intValue());
            o.setDirectSupervisorCode(reqVO.getDirectSupervisorCode());
            o.setDirectSupervisorName(reqVO.getDirectSupervisorName());
        });
        //保存数据
        saveApplyData(temps, areaInfoList, channelInfoList, skuInfoList);
        //删除草稿表中的数据
        deleteDraftBatchByCodes(dtos, skuList, areaList, channelList);
        //更新编码
        //encodingRuleDao.updateNumberValue(numberingType.getNumberingValue(), numberingType.getId());
        //存日志
        supplierCommonService.getInstance(formNo, HandleTypeCoce.PENDING.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.WAIT_SALE_AREA.getName(), null, HandleTypeCoce.PENDING.getName(), getUser().getPersonName());
        //调用审批的接口
        workFlow(formNo, code, currentAuthToken.getPersonName(), reqVO.getDirectSupervisorCode(), reqVO.getPositionCode());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApplyData(List<ApplyProductSkuSaleAreaMain> temps, List<ApplyProductSkuSaleAreaInfo> areaInfoList, List<ApplyProductSkuSaleAreaChannel> channelInfoList, List<ApplyProductSkuSaleArea> skuInfoList) {
        int i = applyProductSkuSaleAreaMainMapper.insertBatch(temps);
        if (i != temps.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "申请销售区域-主表数据保存异常！"));
        }
        //保存附表数据
        int i2 = applyProductSkuSaleAreaMapper.insertBatch(skuInfoList);
        if (i2 != skuInfoList.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "申请销售区域-sku数据保存异常！"));
        }
        int i3 = applyProductSkuSaleAreaInfoMapper.insertBatch(areaInfoList);
        if (i3 != areaInfoList.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "申请销售区域-区域数据保存异常！"));
        }
        int i4 = applyProductSkuSaleAreaChannelMapper.insertBatch(channelInfoList);
        if (i4 != channelInfoList.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "申请销售区域-渠道数据保存异常！"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraftBatchByCodes(List<ProductSkuSaleAreaMainDraftDTO> dto, List<ProductSkuSaleAreaDraft> skuList, List<ProductSkuSaleAreaInfoDraft> areaList, List<ProductSkuSaleAreaChannelDraft> channelList) {
        List<String> codes = dto.stream().map(ProductSkuSaleAreaMainDraftDTO::getCode).collect(Collectors.toList());
        //删除主表
        int i = productSkuSaleAreaMainDraftMapper.deleteDraftBatchByCodes(codes);
        if (i != dto.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "删除销售区域-主表临时数据异常！"));
        }
        //删除附表
        int i2 = productSkuSaleAreaInfoDraftMapper.deleteDraftBatchByCodes(codes);
        if (i2 != areaList.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "删除销售区域-区域临时数据异常！"));
        }
        int i3 = productSkuSaleAreaDraftMapper.deleteDraftBatchByCodes(codes);
        if (i3 != skuList.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "删除销售区域-sku临时数据异常！"));
        }
        int i4 = productSkuSaleAreaChannelDraftMapper.deleteDraftBatchByCodes(codes);
        if (i4 != channelList.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "删除销售区域-渠道临时数据异常！"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void workFlow(String formNo, String applyCode, String userName, String directSupervisorCode, String positionCode) {
        WorkFlowVO workFlowVO = new WorkFlowVO();
        workFlowVO.setPositionCode(positionCode);
        workFlowVO.setFormUrl(workFlowBaseUrl.applySaleArea + "?approvalType=3&code=" + applyCode + "&" + workFlowBaseUrl.authority);
        workFlowVO.setHost(workFlowBaseUrl.supplierHost);
        workFlowVO.setFormNo(formNo);
        workFlowVO.setUpdateUrl(workFlowBaseUrl.callBackBaseUrl + WorkFlow.APPLY_SALE_AREA.getNum());
        workFlowVO.setTitle(userName + "创建销售区域审批");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("auditPersonId", directSupervisorCode);
        workFlowVO.setVariables(jsonObject.toString());
        WorkFlowRespVO workFlowRespVO = callWorkFlowApi(workFlowVO, WorkFlow.APPLY_SALE_AREA);
        //判断是否成功
        if (workFlowRespVO.getSuccess()) {
            //TODO 这里暂时没有任何操作
            supplierCommonService.getInstance(formNo, HandleTypeCoce.APPROVAL.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.UNDER_SALE_AREA.getName(), null, HandleTypeCoce.APPROVAL.getName(), getUser().getPersonName());
        } else {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, workFlowRespVO.getMsg()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String workFlowCallback(WorkFlowCallbackVO vo) {
        WorkFlowCallbackVO newVO = updateSupStatus(vo);
        //审批中，直接返回成功
        if (Objects.equals(newVO.getApplyStatus(), ApplyStatus.APPROVAL.getNumber())) {
            return WorkFlowReturn.SUCCESS;
        }
        //首先通过formNO查找数据
        List<ApplyProductSkuSaleAreaMainDTO> list = applyProductSkuSaleAreaMainMapper.selectByFormNo(newVO.getFormNo());
        if (CollectionUtils.isEmpty(list)) {
            log.error("通过formNo查询数据异常,传入数据:{}", JSON.toJSONString(vo));
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "通过编码查询数据异常！"));

        }
        //判断查出来的是否是在审批中的数据
        if (!list.get(0).getApplyStatus().equals(ApplyStatus.APPROVAL.getNumber().intValue())) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据异常，不是在审批中的数据！"));
        }
        //审批驳回
        if (Objects.equals(newVO.getApplyStatus(), ApplyStatus.APPROVAL_FAILED.getNumber())) {
            approvalRejection(newVO, list);
            supplierCommonService.getInstance(newVO.getFormNo(), HandleTypeCoce.APPROVAL_FAILED.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.NOPASS_SALE_AREA.getName(), null, HandleTypeCoce.APPROVAL_FAILED.getName(), newVO.getApprovalUserName());
            return WorkFlowReturn.SUCCESS;
        }
        //撤销
        if (Objects.equals(newVO.getApplyStatus(), ApplyStatus.REVOKED.getNumber())) {
            supplierCommonService.getInstance(newVO.getFormNo(), HandleTypeCoce.REVOKED.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.RECOBER_SALE_AREA.getName(), null, HandleTypeCoce.REVOKED.getName(), newVO.getApprovalUserName());
            cancel(newVO, list);
            return WorkFlowReturn.SUCCESS;
        }
        //审批通过
        if (Objects.equals(newVO.getApplyStatus(), ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
            //保存正式数据数据
            try {
                saveOfficial(newVO, list);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return WorkFlowReturn.FALSE;
            }
            supplierCommonService.getInstance(newVO.getFormNo(), HandleTypeCoce.APPROVAL_SUCCESS.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.PASS_SALE_AREA.getName(), null, HandleTypeCoce.APPROVAL_SUCCESS.getName(), newVO.getApprovalUserName());
            return WorkFlowReturn.SUCCESS;
        }
        return WorkFlowReturn.FALSE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOfficial(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> list) throws Exception {
        List<ApplyProductSkuSaleAreaMainDTO> addList = Lists.newArrayList();
        List<ApplyProductSkuSaleAreaMainDTO> updateList = Lists.newArrayList();
        //分类
        list.forEach(o -> {
            if (Objects.equals(ApplyType.ADD, o.getApplyType()) && Objects.isNull(o.getOfficialCode())) {
                addList.add(o);
            }
            if (Objects.equals(ApplyType.UPDATE, o.getApplyType()) && Objects.nonNull(o.getOfficialCode())) {
                updateList.add(o);
            }
        });
        if (addList.size() + updateList.size() != list.size()) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据异常！"));

        }
        //保存
        if (CollectionUtils.isNotEmpty(addList)) {
            saveBatchForOfficial(newVO, addList);
        }
        boolean flag = Objects.isNull(list.get(0).getSelectionEffectiveStartTime()) || list.get(0).getSelectionEffectiveStartTime().before(new Date());
        log.info("是否立即生效：[{}]", flag);
        //更新
        if (flag && CollectionUtils.isNotEmpty(updateList)) {
            updateBatchForOfficial(newVO, updateList);
        }
        //如果是空，则立即生效，如果不是空，生效时间小于当前时间也是生效
        //更新申请表数据
        ApplyProductSkuSaleAreaInfoReq info = dealData(newVO);
        info.setBeEffective(flag ? 1 : 0);
        Integer integer = updateApplyInfoByVO(info);
        if (integer != list.size()) {
            log.error("审批通过时数据更新异常：需要更新的数据:[{}],实际更新的数据：[{}]", list.size(), integer);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据更新异常！"));
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchForOfficial(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> updateList) throws Exception {
        //这里的key是不允许重复的，保存说明数据异常
        Map<String, ApplyProductSkuSaleAreaMainDTO> dtoMap = updateList.stream().collect(Collectors.toMap(ApplyProductSkuSaleAreaMainDTO::getOfficialCode, Function.identity()));
        //通过正式编码集合查询数据
        List<ProductSkuSaleAreaMain> main = Lists.newArrayList();
        List<ProductSkuSaleArea> skuList = Lists.newArrayList();
        List<ProductSkuSaleAreaInfo> areaList = Lists.newArrayList();
        List<ProductSkuSaleAreaChannel> channelList = Lists.newArrayList();
//        List<ProductSkuSaleAreaMain> oldMains = Lists.newArrayList();
        List<ProductSkuSaleArea> oldSkuList = Lists.newArrayList();
        List<ProductSkuSaleAreaInfo> oldAreaList = Lists.newArrayList();
        List<ProductSkuSaleAreaChannel> oldChannelList = Lists.newArrayList();
        Set<String> codes = dtoMap.keySet();
        List<ProductSkuSaleAreaMainDTO> skuSaleAreas = productSkuSaleAreaMainMapper.selectListByCodes(codes);
        if (CollectionUtils.isEmpty(skuSaleAreas) || updateList.size() != skuSaleAreas.size()) {
            log.error("集合信息：{}", JSON.toJSONString(skuSaleAreas));
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存销售区域时数据异常！"));
        }
        for (ProductSkuSaleAreaMainDTO oldMain : skuSaleAreas) {
            ApplyProductSkuSaleAreaMainDTO areaDTO = dtoMap.get(oldMain.getCode());
            if (Objects.isNull(areaDTO)) {
                log.error("数据为空");
                throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "更新销售区域正式表数据异常！"));
            }
            ProductSkuSaleAreaMain newMain = BeanCopyUtils.copy(areaDTO, ProductSkuSaleAreaMain.class);
            //设置为生效
//            newMain.setBeEffective(1);
            newMain.setCode(oldMain.getCode());
            supplierCommonService.getInstance(oldMain.getCode(), HandleTypeCoce.UPDATE.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.EDIT_SALE_AREA.getName(), null, HandleTypeCoce.UPDATE.getName(), newVO.getApprovalUserName());
            newMain.setCreateBy(oldMain.getCreateBy());
            newMain.setCreateTime(oldMain.getCreateTime());
            main.add(newMain);
            List<ProductSkuSaleArea> saleAreas = BeanCopyUtils.copyList(areaDTO.getSkuList(), ProductSkuSaleArea.class);
            saleAreas.forEach(o -> o.setCode(oldMain.getCode()));
            skuList.addAll(saleAreas);
            List<ProductSkuSaleAreaChannel> areaChannels = BeanCopyUtils.copyList(areaDTO.getChannelList(), ProductSkuSaleAreaChannel.class);
            areaChannels.forEach(o -> o.setCode(oldMain.getCode()));
            channelList.addAll(areaChannels);
            List<ProductSkuSaleAreaInfo> areaInfos = BeanCopyUtils.copyList(areaDTO.getAreaList(), ProductSkuSaleAreaInfo.class);
            areaInfos.forEach(o -> o.setMainCode(oldMain.getCode()));
            areaList.addAll(areaInfos);
            oldAreaList.addAll(oldMain.getAreaList());
            oldSkuList.addAll(oldMain.getSkuList());
            oldChannelList.addAll(oldMain.getChannelList());
        }
        //删除附表的数据
        deleteOfficialSubByCodes(codes, oldSkuList, oldAreaList, oldChannelList);
        //更新正式表数据
        int i2 = productSkuSaleAreaMainMapper.updateByCode(main);
        if (i2 != main.size()) {
            log.error("更新时，希望更新主表条数:[{}],实际条数：[{}]", main.size(), i2);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
        //保存附表数据
        saveOfficeSubData(skuList, areaList, channelList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOfficialSubByCodes(Set<String> codes, List<ProductSkuSaleArea> oldSkuList, List<ProductSkuSaleAreaInfo> oldAreaList, List<ProductSkuSaleAreaChannel> oldChannelList) {
        int i1 = productSkuSaleAreaMapper.deleteByCodes(codes);
        if (i1 != oldSkuList.size()) {
            log.error("希望删除sku附表条数:[{}],实际条数：[{}]", oldSkuList.size(), i1);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
        int i2 = productSkuSaleAreaChannelMapper.deleteByCodes(codes);
        if (i2 != oldChannelList.size()) {
            log.error("希望删除区域附表条数:[{}],实际条数：[{}]", oldChannelList.size(), i2);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
        int i3 = productSkuSaleAreaInfoMapper.deleteByCodes(codes);
        if (i3 != oldAreaList.size()) {
            log.error("希望删除附表渠道条数:[{}],实际条数：[{}]", oldAreaList.size(), i3);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchForOfficial(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> addList) throws Exception {
        List<ProductSkuSaleAreaMain> main = Lists.newArrayList();
        List<ProductSkuSaleArea> skuList = Lists.newArrayList();
        List<ProductSkuSaleAreaInfo> areaList = Lists.newArrayList();
        List<ProductSkuSaleAreaChannel> channelList = Lists.newArrayList();
        //判断是否生效
        boolean flag = Objects.isNull(addList.get(0).getSelectionEffectiveStartTime()) || addList.get(0).getSelectionEffectiveStartTime().before(new Date());
        //组装数据
        for (ApplyProductSkuSaleAreaMainDTO mainDTO : addList) {
            ProductSkuSaleAreaMain saleAreaMain = BeanCopyUtils.copy(mainDTO, ProductSkuSaleAreaMain.class);
            supplierCommonService.getInstance(mainDTO.getCode(), HandleTypeCoce.ADD.getStatus(), ObjectTypeCode.SALE_AREA.getStatus(), HandleTypeCoce.ADD_SALE_AREA.getName(), null, HandleTypeCoce.ADD.getName(), newVO.getApprovalUserName());
//            saleAreaMain.setBeEffective(flag ?1:0);
            main.add(saleAreaMain);
            skuList.addAll(BeanCopyUtils.copyList(mainDTO.getSkuList(), ProductSkuSaleArea.class));
            areaList.addAll(BeanCopyUtils.copyList(mainDTO.getAreaList(), ProductSkuSaleAreaInfo.class));
            channelList.addAll(BeanCopyUtils.copyList(mainDTO.getChannelList(), ProductSkuSaleAreaChannel.class));
        }
        //批量插入附表数据
        saveOfficeSubData(skuList, areaList, channelList);
        //批量插入主表数据
        int i4 = productSkuSaleAreaMainMapper.insertBatch(main);
        if (i4 != main.size()) {
            log.error("新增时，希望插入正式表条数:[{}],实际插入条数：[{}]", main.size(), i4);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOfficeSubData(List<ProductSkuSaleArea> skuList, List<ProductSkuSaleAreaInfo> areaList, List<ProductSkuSaleAreaChannel> channelList) {
        int i = productSkuSaleAreaMapper.insertBatch(skuList);
        if (i != skuList.size()) {
            log.error("希望插入sku表条数:[{}],实际插入条数：[{}]", skuList.size(), i);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
        //批量插入附表数据
        int i2 = productSkuSaleAreaInfoMapper.insertBatch(areaList);
        if (i2 != areaList.size()) {
            log.error("新增时，希望插入区域附表条数:[{}],实际插入条数：[{}]", areaList.size(), i);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
        //批量插入附表数据
        int i3 = productSkuSaleAreaChannelMapper.insertBatch(channelList);
        if (i3 != channelList.size()) {
            log.error("新增时，希望插入渠道附表条数:[{}],实际插入条数：[{}]", channelList.size(), i);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "保存正式表数据失败！"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> list) {
        //处理数据
        ApplyProductSkuSaleAreaInfoReq req = dealData(newVO);
        //批量更新数据
        Integer integer = updateApplyInfoByVO(req);
        if (integer != list.size()) {
            log.error("审批取消时数据更新异常：需要更新的数据:[{}],实际更新的数据：[{}]", list.size(), integer);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据更新异常！"));
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvalRejection(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> list) {
        //处理数据
        ApplyProductSkuSaleAreaInfoReq req = dealData(newVO);
        //批量更新数据
        Integer integer = updateApplyInfoByVO(req);
        if (integer != list.size()) {
            log.error("审批驳回时数据更新异常：需要更新的数据:[{}],实际更新的数据：[{}]", list.size(), integer);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据更新异常！"));
        }
    }

    @Override
    public List<QueryProductApplyRespVO> queryApplyList(QueryProductApplyReqVO reqVo) {
        PageHelper.startPage(reqVo.getPageNo(), reqVo.getPageSize());
        return applyProductSkuSaleAreaMainMapper.queryApplyList(reqVo);
    }

    @Override
    public ProductApplyInfoRespVO<ProductSaleAreaApplyVO> applyView(String code) {
        List<ProductSaleAreaApplyVO> list = applyProductSkuSaleAreaMainMapper.selectByApplyCode(code);
        if (CollectionUtils.isEmpty(list)) {
            log.error("传入的编码是：[{}]", code);
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据异常，无法查询到该数据"));
        }
        //处理数据
        return dealApplyViewData(list);
    }

    @Override
    public ProductSaleAreaForOfficialMainRespVO officialView(String code) {
        ProductSaleAreaForOfficialMainRespVO vo = productSkuSaleAreaMainMapper.selectDetailByCode(code);
        if (Objects.isNull(vo)) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "数据异常，无法查询到该数据"));
        }
        //日志
        OperationLogBean operationLogBean = new OperationLogBean(code, null, ObjectTypeCode.SALE_AREA.getStatus(), null, null);
        List<LogData> log = operationLogService.selectListByVO(operationLogBean);
        vo.setLogData(log);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDraft(String code) {
        List<String> objects = Lists.newArrayList();
        objects.add(code);
        List<ProductSkuSaleAreaMainDraftDTO> temp = productSkuSaleAreaMainDraftMapper.selectDataByCodes(objects);
        if (CollectionUtils.isEmpty(temp) || temp.size() > 1) {
//            throw new BizException(MessageId.create(Project.PRODUCT_API,98,"对应编码的数据异常!"));
            log.info("deleteDraft---数据是:[{}]", JSONObject.toJSONString(temp));
            return true;
        }
        deleteDraftBatchByCodes(temp, temp.get(0).getSkuList(), temp.get(0).getAreaList(), temp.get(0).getChannelList());
        return true;
    }

    @Override
    public Boolean cancelApply(String code) {
        List<ProductSaleAreaApplyVO> productSaleAreaApplyVOS = applyProductSkuSaleAreaMainMapper.selectByApplyCode(code);
        if (CollectionUtils.isEmpty(productSaleAreaApplyVOS)) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "对应编码的数据异常!"));
        }
        if (productSaleAreaApplyVOS.get(0).getApplyStatus().equals(CommonConstant.CANCEL)) {
            return true;
        }
        if (productSaleAreaApplyVOS.get(0).getApplyStatus().equals(CommonConstant.UNDER_REVIEW)) {
            WorkFlowVO workFlowVO = new WorkFlowVO();
            workFlowVO.setFormNo(productSaleAreaApplyVOS.get(0).getFormNo());
            WorkFlowRespVO workFlowRespVO = cancelWorkFlow(workFlowVO);
            return workFlowRespVO.getSuccess();
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean editView(ProductSkuSaleAreaMainReqVO productSkuSaleAreaMainReqVO) throws Exception {
        AuthToken currentAuthToken = AuthenticationInterceptor.getCurrentAuthToken();
        productSkuSaleAreaMainReqVO.setCompanyCode(currentAuthToken.getCompanyCode());
        productSkuSaleAreaMainReqVO.setCompanyName(currentAuthToken.getCompanyName());
        //对主表进行修改
        ProductSkuSaleAreaMain productSkuSaleAreaMain = new ProductSkuSaleAreaMain();
        BeanUtils.copyProperties(productSkuSaleAreaMainReqVO, productSkuSaleAreaMain);

        int num = productSkuSaleAreaMainMapper.updateByPrimaryKeySelective(productSkuSaleAreaMain);
        if (num < 1) {
            throw new BizException(MessageId.create(Project.PRODUCT_API, 98, "对应编码的数据异常!"));
        }
        //删掉之前关联的表
        productSkuSaleAreaInfoMapper.deleteByCode(productSkuSaleAreaMain.getCode());
        productSkuSaleAreaChannelMapper.deleteByCode(productSkuSaleAreaMain.getCode());
        //重新保存数据
        //拼装实体
        List<ProductSkuSaleAreaChannelDraft> channelDrafts = BeanCopyUtils
                .copyList(productSkuSaleAreaMainReqVO.getChannelList(), ProductSkuSaleAreaChannelDraft.class);
        channelDrafts.forEach(x->{
            x.setCode(productSkuSaleAreaMainReqVO.getCode());
            x.setCompanyCode(productSkuSaleAreaMainReqVO.getCompanyCode());
            x.setCompanyName(productSkuSaleAreaMainReqVO.getCompanyName());
        });

        List<ProductSkuSaleAreaInfoDraft> productSkuSaleAreaInfoDrafts = BeanCopyUtils.copyList(productSkuSaleAreaMainReqVO.getAreaList(), ProductSkuSaleAreaInfoDraft.class);
        productSkuSaleAreaInfoDrafts.forEach(x->{
            x.setMainCode(productSkuSaleAreaMainReqVO.getCode());
            x.setCompanyCode(productSkuSaleAreaMainReqVO.getCompanyCode());
            x.setCompanyName(productSkuSaleAreaMainReqVO.getCompanyName());

        });

        saveDraftData(null, productSkuSaleAreaInfoDrafts, channelDrafts);
        //新增日志
        SupplierOperationLog supplierOperationLog = new SupplierOperationLog();
        supplierOperationLog.setObjectId(productSkuSaleAreaMainReqVO.getCode());
        supplierOperationLog.setHandleType(new Byte(String.valueOf(1)));
        supplierOperationLog.setObjectType(ObjectTypeCode.SALE_AREA.getStatus());
        supplierOperationLog.setHandleName("修改");
        supplierOperationLog.setDelFlag(new Byte(String.valueOf(0)));
        operationLogService.insert(supplierOperationLog);
        return true;
    }


    @Override
    public BasePage<QueryProductSaleAreaForSkuRespVO> skuList(QueryProductSaleAreaForSkuReqVO reqVO) {
        reqVO.setCompanyCode(getUser().getCompanyCode());
        return skuInfoService.selectSkuListForSaleArea(reqVO);
    }

    /**
     * 组装数据
     *
     * @param list
     * @return com.aiqin.mgs.product.api.domain.product.apply.ProductApplyInfoRespVO
     * @author NullPointException
     * @date 2019/5/15
     */
    private ProductApplyInfoRespVO<ProductSaleAreaApplyVO> dealApplyViewData(List<ProductSaleAreaApplyVO> list) {
        ProductApplyInfoRespVO<ProductSaleAreaApplyVO> resp = new ProductApplyInfoRespVO<>();
        //数据相同默认取第一个
        ProductSaleAreaApplyVO applyVO = list.get(0);
        resp.setApplyBy(applyVO.getCreateBy());
        resp.setApplyTime(applyVO.getCreateTime());
        resp.setApplyStatus(applyVO.getApplyStatus());
        resp.setAuditorBy(applyVO.getAuditorBy());
        resp.setAuditorTime(applyVO.getAuditorTime());
        resp.setSelectionEffectiveStartTime(applyVO.getSelectionEffectiveStartTime());
        resp.setSelectionEffectiveTime(applyVO.getSelectionEffectiveTime());
        resp.setCode(applyVO.getCode());
        resp.setFormNo(applyVO.getFormNo());
        Set<String> skuCodes = Sets.newHashSet();
        //统计sku数量
        for (ProductSaleAreaApplyVO productSaleAreaApplyVO : list) {
            List<String> collect = productSaleAreaApplyVO.getSkuList().stream().map(QueryProductSaleAreaRespVO::getSkuCode).distinct().collect(Collectors.toList());
            skuCodes.addAll(collect);
        }
        resp.setSkuNum(skuCodes.size());
        resp.setData(list);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateApplyInfoByVO(ApplyProductSkuSaleAreaInfoReq req) {
        return applyProductSkuSaleAreaMainMapper.updateApplyInfoByVO(req);
    }

    /**
     * 封装数据
     *
     * @param newVO 传回数据
     * @return com.aiqin.mgs.product.api.domain.request.salearea.ApplyProductSkuSaleAreaInfoReq
     * @author NullPointException
     * @date 2019/5/13
     */
    private ApplyProductSkuSaleAreaInfoReq dealData(WorkFlowCallbackVO newVO) {
        ApplyProductSkuSaleAreaInfoReq temp = new ApplyProductSkuSaleAreaInfoReq();
        temp.setApplyStatus(newVO.getApplyStatus().intValue());
        temp.setAuditorBy(newVO.getApprovalUserName());
        temp.setAuditorTime(new Date());
        temp.setFormNo(newVO.getFormNo());
        return temp;
    }

    @Override
    public BasePage<AreaBasic> areaList(QueryAreaReqVO req) {
        HttpResponse response = areaBasicService.selectAreaOneTree();
        boolean flag = Objects.isNull(response) || !MsgStatus.SUCCESS.equals(response.getCode()) || CollectionUtils.isEmpty((List<LinkedHashMap<String, String>>) response.getData());
        if (flag) {
            throw new BizException(MessageId.create(Project.SUPPLIER_API, 98, "获取省列表失败！"));
        }
        List<LinkedHashMap<String, String>> tempList = (List<LinkedHashMap<String, String>>) response.getData();
        String s = JSON.toJSONString(tempList);
        List<AreaBasic> list = JSONObject.parseArray(s, AreaBasic.class);
        Integer tatal = list.size();
        if (StringUtils.isNotBlank(req.getAreaName())) {
            Stream<AreaBasic> areaBasicStream = list.stream().filter(o -> o.getArea_name().contains(req.getAreaName()));
            List<AreaBasic> collect = areaBasicStream.collect(Collectors.toList());
            tatal = collect.size();
            list = collect.stream().skip((req.getPageNo() - 1) * req.getPageSize()).limit(req.getPageSize()).collect(Collectors.toList());
        } else {
            list = list.stream().skip((req.getPageNo() - 1) * req.getPageSize()).limit(req.getPageSize()).collect(Collectors.toList());
        }
        BasePage pageList = PageUtil.getPageList(req.getPageNo(), list);
        pageList.setTotalCount(tatal.longValue());
        pageList.setPageNo(req.getPageNo());
        pageList.setPageSize(req.getPageSize());
        return pageList;
    }

    @Override
    public DetailRequestRespVo getInfoByForm(String formNo) {
        DetailRequestRespVo respVo = new DetailRequestRespVo();
        List<ApplyProductSkuSaleAreaMainDTO> list = applyProductSkuSaleAreaMainMapper.selectByFormNo(formNo);
        if (CollectionUtils.isEmpty(list)) {
            throw new BizException(ResultCode.OBJECT_EMPTY_BY_FORMNO);
        }
        String applyCode = list.get(0).getApplyCode();
        respVo.setApplyCode(applyCode);
        respVo.setItemCode("3");
        return respVo;
    }

    @Override
    public BasePage<StoreInfo> storeList(QueryStoreReqVO req) {
        HttpResponse httpResponse = storeApi.storeList(req);
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) httpResponse.getData();
        String s = JSON.toJSONString(data);
        JSONObject object = JSONObject.parseObject(s);
        Integer totalCount = (Integer) object.get("totalCount");
        Object dataList = object.get("dataList");
        String s1 = JSONObject.toJSONString(dataList);
        List<StoreInfo> storeInfos = JSONObject.parseArray(s1, StoreInfo.class);
        BasePage pageList = PageUtil.getPageList(req.getPageNo(), storeInfos);
        pageList.setPageSize(req.getPageSize());
        pageList.setPageNo(req.getPageNo());
        pageList.setTotalCount(totalCount.longValue());
        return pageList;
    }

    @Override
    public List<ProductSaleAreaFuzzySearchRespVO> fuzzySearch(String name) {
        QueryAreaReqVO areaReqVO = new QueryAreaReqVO();
        areaReqVO.setAreaName(name);
        areaReqVO.setPageSize(5);
        //先查区域
        BasePage<AreaBasic> pageInfo = areaList(areaReqVO);
        List<AreaBasic> list = pageInfo.getDataList();
        List<ProductSaleAreaFuzzySearchRespVO> respVOS = Lists.newArrayList();
        //默认展示5条
        for (AreaBasic areaBasic : list) {
            ProductSaleAreaFuzzySearchRespVO temp = new ProductSaleAreaFuzzySearchRespVO(areaBasic.getArea_id(), areaBasic.getArea_name(), 1);
            respVOS.add(temp);
            if (respVOS.size() == 5) {
                return respVOS;
            }
        }
        //再查门店
        QueryStoreReqVO req = new QueryStoreReqVO();
        req.setCondition(name);
        req.setPageSize(areaReqVO.getPageSize() - respVOS.size());
        BasePage<StoreInfo> pageInfo1 = storeList(req);
        List<StoreInfo> list1 = pageInfo1.getDataList();
        for (StoreInfo o : list1) {
            ProductSaleAreaFuzzySearchRespVO temp = new ProductSaleAreaFuzzySearchRespVO(o.getStoreCode(), o.getStoreName(), 2);
            respVOS.add(temp);
        }
        return respVOS;
    }

    @Override
    public HttpResponse skuAreaSale(String skuCode, String provinceCode, String storeCode) {
        if (StringUtils.isBlank(skuCode) || StringUtils.isBlank(provinceCode) || StringUtils.isBlank(storeCode)) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        // 查询商品是否启用及主单号
        String mainCode = productSkuSaleAreaMapper.selectMainCode(skuCode);
        if (StringUtils.isBlank(mainCode)) {
            return HttpResponse.success(true);
        }
        // 根据区域(禁用)查询
        Integer areaCount = productSkuSaleAreaInfoMapper.selectAreaStatus(mainCode, provinceCode, 0, 1);
        if (areaCount > 0) {
            return HttpResponse.success(false);
        } else {
            // 根据门店(禁用)查询
            Integer storeCount = productSkuSaleAreaInfoMapper.selectAreaStatus(mainCode, storeCode, 0, 2);
            if (storeCount > 0) {
                return HttpResponse.success(false);
            } else {
                // 根据区域(启用)查询
                Integer areaCount1 = productSkuSaleAreaInfoMapper.selectAreaStatus(mainCode, provinceCode, 1, 1);
                if (areaCount1 > 0) {
                    return HttpResponse.success(true);
                } else {
                    // 根据门店(启用)查询
                    Integer storeCount1 = productSkuSaleAreaInfoMapper.selectAreaStatus(mainCode, storeCode, 1, 2);
                    if (storeCount1 <= 0) {
                        return HttpResponse.success(false);
                    }
                }
            }
        }
        return HttpResponse.success(true);
    }

    @Override
    public QueryProductSaleAreaSkuRespVO skuDetail(QueryProductDetailReqVO reqVO) {
        //获取登录人
        AuthToken currentAuthToken = getUser();
        String personId = currentAuthToken.getPersonId();
        reqVO.setPersonId(personId);
        QueryProductSaleAreaSkuRespVO queryProductSaleAreaSkuRespVO = productSkuSaleAreaMapper.skuDetail(reqVO);
        List<ProductSkuInfo> page = productSkuSaleAreaMapper.getSkuList(reqVO);
        queryProductSaleAreaSkuRespVO.setSkuList(page);
        return queryProductSaleAreaSkuRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean skuEdit(QueryProductDetailReqVO reqVO) {
        //获取登录人
        AuthToken currentAuthToken = getUser();
        String personId = currentAuthToken.getPersonId();
        reqVO.setPersonId(personId);
        //先删除
        productSkuSaleAreaMapper.deleteByCode(reqVO);
        //增加
        reqVO.getSkuList().stream().forEach(x->x.setCode(reqVO.getCode()));
        if(CollectionUtils.isNotEmpty(reqVO.getSkuList())) {
            productSkuSaleAreaMapper.insertBatch(reqVO.getSkuList());
        }
        return true;
    }


    @Override
    public BasePage<QueryProductSaleAreaSkuRespVO> officialSkuList(QueryProductSaleAreaReqVO reqVO) {
        //获取登录人
        AuthToken currentAuthToken = getUser();
        String personId = currentAuthToken.getPersonId();
        reqVO.setCompanyCode(getUser().getCompanyCode());
        reqVO.setPersonId(personId);
        List<Long> longs = productSkuSaleAreaMapper.officialSkuListCount(reqVO);
        if (CollectionUtils.isEmpty(longs)) {
            return PageUtil.getPageList(reqVO.getPageNo(), Lists.newArrayList());
        }
        List<QueryProductSaleAreaSkuRespVO> list = productSkuSaleAreaMapper.officialSkuList(PageUtil.myPage(longs, reqVO), personId);
        return PageUtil.getPageList(reqVO.getPageNo(), reqVO.getPageSize(), longs.size(), list);
    }

    @Override
    public BasePage<AreaBasic> officialSkuList2(QueryProductSaleAreaReqVO2 reqVO) {
        //获取登录人
        AuthToken currentAuthToken = getUser();
        String personId = currentAuthToken.getPersonId();
        reqVO.setCompanyCode(getUser().getCompanyCode());
        reqVO.setPersonId(personId);
        List<Long> longs = productSkuSaleAreaMapper.officialSkuListCount2(reqVO);
        if (CollectionUtils.isEmpty(longs)) {
            return PageUtil.getPageList(reqVO.getPageNo(), Lists.newArrayList());
        }

        List<QueryProductSaleAreaSkuRespVO> list = productSkuSaleAreaMapper.officialSkuList2(PageUtil.myPage(longs, reqVO), personId);
        return PageUtil.getPageList(reqVO.getPageNo(), reqVO.getPageSize(), longs.size(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SkuSaleAreaImport>  importData(MultipartFile file) {
        List<SkuSaleAreaImport> skuConfigImports=Lists.newArrayList();
        //获取登录人
        AuthToken currentAuthToken = getUser();
        try {
            skuConfigImports = ExcelUtil.readExcel(file, SkuSaleAreaImport.class, 1, 2);

            //供货渠道类别
            skuConfigImports = skuConfigImports.stream().distinct().collect(Collectors.toList());
            for (int i =0;i<skuConfigImports.size();i++) {
                SkuSaleAreaImport skuSaleAreaImport=skuConfigImports.get(i);
                if (StringUtils.isEmpty(skuSaleAreaImport.getSkuCode())||StringUtils.isEmpty(skuSaleAreaImport.getSaleName())||StringUtils.isEmpty(skuSaleAreaImport.getCategoriesSupplyChannelsName())) {
                    throw new BizException("第"+i+2+"有必填项为空");
                }
                ProductSkuInfo productSkuInfo = new ProductSkuInfo();
                BeanUtils.copyProperties(skuSaleAreaImport, productSkuInfo);

                if (productSkuDao.selectByNameAndcode(productSkuInfo) < 1) {
                    throw new BizException("第"+i+2+"或者错误商品code" + productSkuInfo.getSkuCode());
                }
                ProductSkuSaleAreaMain productSkuSaleAreaMain= productSkuSaleAreaMainMapper.selectByName(skuSaleAreaImport.getSaleName());
                if (ObjectUtils.equals(null,productSkuSaleAreaMain)) {
                    throw new BizException("第"+i+2+"错误销售区域名称"+skuSaleAreaImport.getSaleName());
                }else {
                    skuSaleAreaImport.setSaleCode(productSkuSaleAreaMain.getCode());
                }

                if (!skuSaleAreaImport.getCategoriesSupplyChannelsName().equals("配送") && !skuSaleAreaImport.getCategoriesSupplyChannelsName().equals("直送")) {
                    throw new BizException("第"+i+2+"错误配送方法" + skuSaleAreaImport.getCategoriesSupplyChannelsName());
                }

                if (skuSaleAreaImport.getCategoriesSupplyChannelsName().equals("配送") ) {
                    skuSaleAreaImport.setCategoriesSupplyChannelsCode(2);
                }else if (skuSaleAreaImport.getCategoriesSupplyChannelsName().equals("直送") ) {
                    skuSaleAreaImport.setCategoriesSupplyChannelsCode(3);
                }

                if (skuSaleAreaImport.getCategoriesSupplyChannelsName().equals("直送") && StringUtils.isEmpty(skuSaleAreaImport.getDirectDeliverySupplierName())) {
                    throw new BizException("第"+i+2+"直送供应商的名称。如果供货渠道类别为自送，那么必填");

                }
                skuSaleAreaImport.setCompanyCode(currentAuthToken.getCompanyCode());
                skuSaleAreaImport.setCompanyName(currentAuthToken.getCompanyName());
                skuSaleAreaImport.setCreateBy(currentAuthToken.getPersonName());
            }


        } catch (ExcelException e) {
            e.printStackTrace();
            return null;
        }
        return skuConfigImports;
    }

    @Override
    public Boolean importDataConfirm(List<SkuSaleAreaImport> skuSaleAreaImports) {

        //获取登录人
        AuthToken currentAuthToken = getUser();
        for (SkuSaleAreaImport skuSaleAreaImport:
        skuSaleAreaImports ) {
            skuSaleAreaImport.setCompanyCode(currentAuthToken.getCompanyCode());
            skuSaleAreaImport.setCompanyName(currentAuthToken.getCompanyName());
            skuSaleAreaImport.setCreateBy(currentAuthToken.getPersonName());

            productSkuSaleAreaMapper.insertImports(skuSaleAreaImport);
        }
        return null;
    }



    @Override
    public HttpResponse exportSkuBysaleCode(HttpServletResponse resp, String saleCode) {
        QueryProductDetailReqVO reqVO=new QueryProductDetailReqVO();
        reqVO.setCode(saleCode);
        List<ProductSkuInfo> skuList = productSkuSaleAreaMapper.getSkuList(reqVO);
        Integer length = skuList.size();
        if(length > 50000){
            return HttpResponse.failure(MessageId.create(Project.SCMP_API, 500, "最大导出量为5W条数据,筛选的数据量已超过本限制,请根据采购组/品类/时间过滤"));
        }
        if(length == 0){
            return HttpResponse.failure(MessageId.create(Project.SCMP_API, 500, "没有找到需要导出的数据"));
        }
        List<SaleSkuInfoExport> list=BeanCopyUtils.copyList(skuList,SaleSkuInfoExport.class);
        try {
            ProductSaleAreaForOfficialMainRespVO productSaleAreaForOfficialMainRespVO= productSkuSaleAreaMainMapper.selectDetailByCode(saleCode);

            String timeStr1= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            ExcelUtil.writeExcel(resp, list, productSaleAreaForOfficialMainRespVO.getName()+timeStr1, "商品导出模板", ExcelTypeEnum.XLSX, SaleSkuInfoExport.class);
        } catch (ExcelException e) {
            e.printStackTrace();
            return HttpResponse.success("导出失败");

        }
        return HttpResponse.success();

    }


}

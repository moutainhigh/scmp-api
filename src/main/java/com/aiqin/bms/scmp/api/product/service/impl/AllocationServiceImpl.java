package com.aiqin.bms.scmp.api.product.service.impl;

import com.aiqin.bms.scmp.api.abutment.domain.request.dl.BatchRequest;
import com.aiqin.bms.scmp.api.abutment.domain.request.dl.ProductRequest;
import com.aiqin.bms.scmp.api.abutment.domain.request.dl.StockChangeRequest;
import com.aiqin.bms.scmp.api.abutment.service.DlAbutmentService;
import com.aiqin.bms.scmp.api.base.*;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.*;
import com.aiqin.bms.scmp.api.config.AuthenticationInterceptor;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.dao.ProductSkuPicturesDao;
import com.aiqin.bms.scmp.api.product.dao.ProductSkuSalesInfoDao;
import com.aiqin.bms.scmp.api.product.dao.StockBatchDao;
import com.aiqin.bms.scmp.api.product.domain.EnumReqVo;
import com.aiqin.bms.scmp.api.product.domain.converter.AllocationResVo2OutboundReqVoConverter;
import com.aiqin.bms.scmp.api.product.domain.converter.allocation.AllocationOrderToInboundConverter;
import com.aiqin.bms.scmp.api.product.domain.converter.allocation.AllocationOrderToOutboundConverter;
import com.aiqin.bms.scmp.api.product.domain.dto.allocation.AllocationDTO;
import com.aiqin.bms.scmp.api.product.domain.pojo.Allocation;
import com.aiqin.bms.scmp.api.product.domain.pojo.AllocationProduct;
import com.aiqin.bms.scmp.api.product.domain.pojo.AllocationProductBatch;
import com.aiqin.bms.scmp.api.product.domain.pojo.StockBatch;
import com.aiqin.bms.scmp.api.product.domain.request.QueryStockSkuReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.WarehouseConfigReq;
import com.aiqin.bms.scmp.api.product.domain.request.allocation.*;
import com.aiqin.bms.scmp.api.product.domain.request.inbound.InboundReqSave;
import com.aiqin.bms.scmp.api.product.domain.request.outbound.OutboundReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.stock.ChangeStockRequest;
import com.aiqin.bms.scmp.api.product.domain.request.stock.StockBatchInfoRequest;
import com.aiqin.bms.scmp.api.product.domain.request.stock.StockInfoRequest;
import com.aiqin.bms.scmp.api.product.domain.response.QueryStockSkuRespVo;
import com.aiqin.bms.scmp.api.product.domain.response.WarehouseConfigResp;
import com.aiqin.bms.scmp.api.product.domain.response.allocation.*;
import com.aiqin.bms.scmp.api.product.domain.response.sku.PurchaseSaleStockRespVo;
import com.aiqin.bms.scmp.api.product.mapper.AllocationMapper;
import com.aiqin.bms.scmp.api.product.mapper.AllocationProductBatchMapper;
import com.aiqin.bms.scmp.api.product.mapper.AllocationProductMapper;
import com.aiqin.bms.scmp.api.product.mapper.ProductSkuBatchMapper;
import com.aiqin.bms.scmp.api.product.service.AllocationService;
import com.aiqin.bms.scmp.api.product.service.InboundService;
import com.aiqin.bms.scmp.api.product.service.OutboundService;
import com.aiqin.bms.scmp.api.product.service.StockService;
import com.aiqin.bms.scmp.api.purchase.domain.request.order.BatchWmsInfo;
import com.aiqin.bms.scmp.api.purchase.domain.request.wms.CancelSource;
import com.aiqin.bms.scmp.api.purchase.service.WmsCancelService;
import com.aiqin.bms.scmp.api.purchase.service.impl.GoodsRejectServiceImpl;
import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.dao.warehouse.WarehouseDao;
import com.aiqin.bms.scmp.api.supplier.domain.request.OperationLogVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.warehouse.dto.WarehouseDTO;
import com.aiqin.bms.scmp.api.supplier.domain.response.LogData;
import com.aiqin.bms.scmp.api.supplier.domain.response.allocation.AllocationItemRespVo;
import com.aiqin.bms.scmp.api.supplier.service.OperationLogService;
import com.aiqin.bms.scmp.api.supplier.service.SupplierCommonService;
import com.aiqin.bms.scmp.api.supplier.service.WarehouseService;
import com.aiqin.bms.scmp.api.util.*;
import com.aiqin.bms.scmp.api.workflow.annotation.WorkFlowAnnotation;
import com.aiqin.bms.scmp.api.workflow.enumerate.WorkFlow;
import com.aiqin.bms.scmp.api.workflow.helper.WorkFlowHelper;
import com.aiqin.bms.scmp.api.workflow.service.WorkFlowService;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowCallbackVO;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowVO;
import com.aiqin.bms.scmp.api.workflow.vo.response.WorkFlowRespVO;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import com.aiqin.ground.util.http.HttpClient;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @Author: Kt.w
 * @Date: 2019/1/9
 * @Version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@WorkFlowAnnotation(WorkFlow.APPLY_ALLOCATTION)
public class AllocationServiceImpl extends BaseServiceImpl implements AllocationService, WorkFlowHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllocationServiceImpl.class);

    @Autowired
    private AllocationMapper allocationMapper;
    @Autowired
    private AllocationProductMapper allocationProductMapper;
    @Autowired
    private AllocationProductBatchMapper allocationProductBatchMapper;
    @Autowired
    private EncodingRuleDao encodingRuleDao;
    @Autowired
    private SupplierCommonService supplierCommonService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private WorkFlowBaseUrl workFlowBaseUrl;
    @Autowired
    private StockService stockService;
    @Autowired
    private WarehouseService supplierApiService;
    @Autowired
    private ProductSkuPicturesDao productSkuPicturesDao;
    @Autowired
    private OutboundService outboundService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private StockBatchDao stockBatchDao;
    @Autowired
    private UrlConfig urlConfig;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private ProductSkuSalesInfoDao productSkuSalesInfoDao;
    @Autowired
    private InboundService inboundService;
    @Autowired
    private WmsCancelService wmsCancelService;
    @Autowired
    private ProductSkuBatchMapper productSkuBatchMapper;
    @Autowired
    private DlAbutmentService dlService;

    @Resource
    private CodeUtils codeUtils;

    @Override
    public BasePage<QueryAllocationResVo> getList(QueryAllocationReqVo vo) {
        AuthToken authToken = getUser();
        vo.setCompanyCode(authToken.getCompanyCode());
        PageHelper.startPage(vo.getPageNo(), vo.getPageSize());

        List<QueryAllocationResVo> list = allocationMapper.getList(vo);
        BasePage<QueryAllocationResVo> basePage = PageUtil.getPageList(vo.getPageNo(), list);
        return basePage;

    }

    /**
     * 转化保存实体
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AllocationReqVo vo) {
        log.info("新增调拨移库损溢单保存的参数：[{}]", JsonUtil.toJson(vo));
        Allocation allocation = new Allocation();
        BeanCopyUtils.copy(vo, allocation);
        allocation.setPatternType(1);
        allocation.setPatternName("我方发起");
        AllocationTypeEnum allocationTypeEnum = AllocationTypeEnum.getAllocationTypeEnumByType(vo.getAllocationType());
//        // 获取编码
//        EncodingRule encodingRule = encodingRuleDao.getNumberingType(EncodingRuleType.ALLOCATION_CODE);
//        // 更新编码库
//        encodingRuleDao.updateNumberValue(encodingRule.getNumberingValue(),encodingRule.getId());
        String redisCode = codeUtils.getRedisCode(EncodingRuleType.ALLOCATION_CODE);
        allocation.setAllocationCode(redisCode);
        String content = "";
        // type 审批人和申请人是一人 直接调审批回调接口
        Integer type;
        if (Objects.equals(vo.getAllocationType(), AllocationTypeEnum.ALLOCATION.getType())) {
            content = HandleTypeCoce.ADD_ALLOCATION.getName();
            type = 8;
        } else if (Objects.equals(vo.getAllocationType(), AllocationTypeEnum.MOVE.getType())) {
            content = HandleTypeCoce.ADD_MOVEMENT.getName();
            type = 13;
        } else {
            content = HandleTypeCoce.ADD_SCRAP.getName();
            type = 15;
        }
        //保存日志
        supplierCommonService.getInstance(allocation.getAllocationCode() + "", HandleTypeCoce.ADD.getStatus(), ObjectTypeCode.ALLOCATION.getStatus(), content, null, HandleTypeCoce.ADD.getName());
        //设置状态(未审核)
        allocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_TOCHECK.getStatus());
        allocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_TOCHECK.getName());
        //获取FormNo
        String form = allocationTypeEnum.getGenerateCode() + IdSequenceUtils.getInstance().nextId();
        allocation.setFormNo(form);
        LOGGER.info("保存调拨单主表数据参数{}", JsonUtil.toJson(allocation));
        Long k = ((AllocationService) AopContext.currentProxy()).insertSelective(allocation);
        if (k <= 0) {
            throw new GroundRuntimeException("调拨单保存失败");
        }
        //转化调拨单sku列表
        List<AllocationProductBatch> list = BeanCopyUtils.copyList(vo.getList(), AllocationProductBatch.class);
        list.stream().forEach(allocationProduct -> {
            allocationProduct.setTaxAmount(null != allocationProduct.getTaxAmount() ? allocationProduct.getTaxAmount() : new BigDecimal(0));
            allocationProduct.setTax(null != allocationProduct.getTax() ? allocationProduct.getTax() : BigDecimal.ZERO);
            allocationProduct.setTaxPrice(null != allocationProduct.getTaxPrice() ? allocationProduct.getTaxPrice() : BigDecimal.ZERO);
            allocationProduct.setAllocationCode(allocation.getAllocationCode());
        });
        ChangeStockRequest stockChangeRequest = new ChangeStockRequest();
        stockChangeRequest.setOperationType(1);
        LOGGER.info("保存调拨单商品批次表数据参数{}", JsonUtil.toJson(list));
        WarehouseDTO warehouse = warehouseDao.getWarehouseByCode(allocation.getCallOutWarehouseCode());
        if (!warehouse.getBatchManage().equals(Global.BATCH_MANAGE_0)) {
            List<AllocationProductBatch> lists = new ArrayList<>();
            for (AllocationProductBatch allocationProductBatch : list) {
                if (allocationProductBatch.getCallOutBatchNumber() != null){
                    AllocationProductBatch copy = BeanCopyUtils.copy(allocationProductBatch, AllocationProductBatch.class);
                    lists.add(copy);
                }
            }
            if(CollectionUtils.isNotEmptyCollection(lists)){
                int kp = ((AllocationService) AopContext.currentProxy()).saveListBatch(lists);
                if (kp <= 0) {
                    log.error("调拨单sku批次数据保存失败");
                    throw new GroundRuntimeException("调拨单sku批次数据保存失败");
                }
                List<StockBatchInfoRequest> batchList1 = allocationBatchTransStock(allocation, lists);
                stockChangeRequest.setStockBatchList(batchList1);
            }
        }
        List<AllocationProduct> products = productbatchTransProduct(list);
        LOGGER.info("保存调拨单商品表数据参数{}", JsonUtil.toJson(products));
        ((AllocationService) AopContext.currentProxy()).saveList(products);

        allocation.setUpdateBy(getUser().getPersonName());
        List<StockInfoRequest> list1 = allocationProductTransStock(allocation, products);
        stockChangeRequest.setStockList(list1);

        //TODO 库存锁定
        // StockChangeRequest stockChangeRequest = new StockChangeRequest();
        // stockChangeRequest.setOperationType(1);
        // stockChangeRequest.setOrderCode(allocation.getAllocationCode());
        // allocation.setUpdateBy(getUser().getPersonName());
        // List<StockVoRequest> list1 = allocationProductTransStock(allocation,products);
        // stockChangeRequest.setStockVoRequests(list1);
        // // 调用锁定库存数
        // HttpResponse httpResponse= stockService.changeStock(stockChangeRequest);
        // 调用锁定库存数
        HttpResponse httpResponse = stockService.stockAndBatchChange(stockChangeRequest);
        if (httpResponse.getCode().equals(MsgStatus.SUCCESS)) {

        } else {
            log.error(httpResponse.getMessage());
            throw new BizException(ResultCode.STOCK_LOCK_ERROR);
        }
        //调用审批流
        WorkFlowRespVO workFlowRespVO = workFlow(k, form, vo.getPositionCode());
        // 当申请人和审批人是同一人的话直接调用审批回调接口
        if (workFlowRespVO.getStatus() == 3) {
            AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
            WorkFlowCallbackVO vo1 = new WorkFlowCallbackVO();
            vo1.setApprovalOpinion("自动通过");
            vo1.setApprovalUserCode(authToken.getPersonId());
            vo1.setApprovalUserName(authToken.getPersonName());
            vo1.setFormNo(form);
            vo1.setOptBtn("BTN-004");
            vo1.setUpdateFormStatus(15);
            workFlowService.workFlowCallBack(WorkFlow.getAll().get(type), vo1);
        }
        return k;
//         }else {
//             log.error("调拨单sku保存失败");
//             throw new GroundRuntimeException("调拨单sku保存失败");
//         }
    }

    public void synchrdlStockChange(Allocation allocation, List<AllocationProductResVo> products, List<AllocationProductBatchResVo> list, StockChangeRequest stockChangeDlRequest) {
        // 主表数据
        Long totalCount = 0L;
        if (Global.DL_OPERATION_TYPE_2 == stockChangeDlRequest.getOperationType()) {
            stockChangeDlRequest.setWarehouseCode(allocation.getCallOutWarehouseCode());
            stockChangeDlRequest.setWarehouseName(allocation.getCallOutWarehouseName());
        } else {
            stockChangeDlRequest.setWarehouseCode(allocation.getCallInWarehouseCode());
            stockChangeDlRequest.setWarehouseName(allocation.getCallInWarehouseName());
        }
        stockChangeDlRequest.setOperationCode(allocation.getUpdateById() == null ? "wms推送" : allocation.getUpdateById());
        stockChangeDlRequest.setOperationName(allocation.getUpdateBy() == null ? "wms推送" : allocation.getUpdateBy());
        // 商品数据
        List<ProductRequest> productList = new ArrayList<>();
        for (AllocationProductResVo product : products) {
            ProductRequest productRequest = new ProductRequest();
            productRequest.setLineCode(product.getLineNum().intValue());
            productRequest.setSkuCode(product.getSkuCode());
            productRequest.setSkuName(product.getSkuName());
            productRequest.setTotalCount(product.getQuantity());
            if (Global.DL_OPERATION_TYPE_2 == stockChangeDlRequest.getOperationType()) {
                productRequest.setActualTotalCount(product.getCallOutQuantity());
            }else {
                productRequest.setActualTotalCount(product.getCallInQuantity());
            }

            totalCount += product.getQuantity();
            productRequest.setProductType(0);
            productRequest.setProductAmount(product.getTaxPrice() == null ? BigDecimal.ZERO : product.getTaxPrice());
            productRequest.setTaxRate(product.getTax() == null ? BigDecimal.ZERO : product.getTax());
            BigDecimal noTaxPrice = Calculate.computeNoTaxPrice(productRequest.getProductAmount(), productRequest.getTaxRate());
            productRequest.setNotProductAmount(noTaxPrice == null ? BigDecimal.ZERO : product.getTaxPrice());
            // 批次数据
            List<BatchRequest> batchList = new ArrayList<>();
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
                for (AllocationProductBatchResVo productBatch : list) {
                    if (Objects.equals(product.getSkuCode() + product.getLineNum().toString(),
                            productBatch.getSkuCode() + productBatch.getLineNum().toString())) {
                        BatchRequest batchRequest = new BatchRequest();
                        batchRequest.setLineCode(productBatch.getLineNum().intValue());
                        batchRequest.setSkuCode(productBatch.getSkuCode());
                        if (Global.DL_OPERATION_TYPE_2 == stockChangeDlRequest.getOperationType()) {
                            batchRequest.setBatchCode(productBatch.getBatchNumber());
                            batchRequest.setActualTotalCount(productBatch.getCalloutActualTotalCount());
                        } else {
                            batchRequest.setBatchCode(productBatch.getCallInBatchNumber());
                            batchRequest.setActualTotalCount(productBatch.getCallinActualTotalCount());
                        }
                        batchRequest.setProductDate(productBatch.getProductDate());
                        batchList.add(batchRequest);
                    }
                }
                productRequest.setBatchList(batchList);
            }
            productList.add(productRequest);
        }
        stockChangeDlRequest.setTotalCount(totalCount);
        stockChangeDlRequest.setProductList(productList);
    }

    /**
     * 撤销调拨单转化实体
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    public int revocation(Long id) {
        Allocation allocation = allocationMapper.selectByPrimaryKey(id);
        AllocationDTO allocationDTO = allocationMapper.selectByFormNO1(allocation.getFormNo());
        WorkFlowVO workFlowVO = new WorkFlowVO();
        workFlowVO.setFormNo(allocation.getFormNo());
        // 调用审批流的撤销接口
        WorkFlowRespVO workFlowRespVO = cancelWorkFlow(workFlowVO);
        if (workFlowRespVO.getSuccess()) {
            allocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_CANCEL.getStatus());
            allocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_CANCEL.getName());
            ((AllocationService) AopContext.currentProxy()).updateByPrimaryKeySelective(allocation);
            // 打印撤销的日志
            String content = ApplyStatus.APPROVAL_FAILED.getContent().replace(Global.CREATE_BY, allocation.getUpdateBy()).replace(Global.AUDITOR_BY, getUser().getPersonName());
            supplierCommonService.getInstance(
                    allocation.getAllocationCode() + "",
                    HandleTypeCoce.REVOKED.getStatus(),
                    ObjectTypeCode.ALLOCATION.getStatus(),
                    content, null,
                    HandleTypeCoce.REVOKED.getName(),
                    getUser().getPersonName()
            );
            //  StockChangeRequest stockChangeRequest = new StockChangeRequest();
            //  stockChangeRequest.setOperationType(3);
            //  stockChangeRequest.setOrderCode(allocation.getAllocationCode());
            //  List<StockVoRequest> list1 = allocationProductTransStock(allocation,allocationDTO.getProducts());
            //  stockChangeRequest.setStockVoRequests(list1);
            //  stockService.changeStock(stockChangeRequest);
            ChangeStockRequest stockChangeRequest = new ChangeStockRequest();
            stockChangeRequest.setOperationType(1);
            allocation.setUpdateBy(getUser().getPersonName());
            List<StockInfoRequest> list1 = allocationProductTransStock(allocation, allocationDTO.getProducts());
            stockChangeRequest.setStockList(list1);
            List<StockBatchInfoRequest> batchList1 = allocationBatchTransStock(allocation, allocationDTO.getList());
            stockChangeRequest.setStockBatchList(batchList1);
            // 调用锁定库存数
            stockService.stockAndBatchChange(stockChangeRequest);
        } else {
            throw new GroundRuntimeException(workFlowRespVO.getMsg());
        }
//        撤销wms调拨单
        CancelSource cancelSource = new CancelSource();
        if (Objects.equals(allocation.getAllocationType(), AllocationTypeEnum.ALLOCATION.getType())) {
            cancelSource.setOrderType("5");
        } else if (Objects.equals(allocation.getAllocationType(), AllocationTypeEnum.MOVE.getType())) {
            cancelSource.setOrderType("6");
        } else {
            return 0;
        }

        cancelSource.setOrderCode(allocation.getAllocationCode());
        cancelSource.setWarehouseCode(allocation.getCallOutWarehouseCode());
        cancelSource.setWarehouseName(allocation.getCallOutWarehouseName());
        wmsCancelService.wmsCancel(cancelSource);
        return 1;
    }

    /**
     * 查看调拨单详情
     *
     * @param id
     * @return
     */
    @Override
    public AllocationResVo view(Long id) {
        AllocationResVo allocationResVo = allocationMapper.getAllocationDetailById(id);
        if (null == allocationResVo) {
            throw new BizException(ResultCode.OBJECT_EMPTY);
        }
        allocationResVo.setSkuList(allocationProductMapper.selectByAllocationCode(allocationResVo.getAllocationCode()));
        allocationResVo.setBatchSkuList(allocationProductBatchMapper.selectByAllocationCode(allocationResVo.getAllocationCode()));
        // 获取日志
        if (null != allocationResVo) {
            //获取操作日志
            OperationLogVo operationLogVo = new OperationLogVo();
            operationLogVo.setPageNo(1);
            operationLogVo.setPageSize(100);
            operationLogVo.setObjectType(ObjectTypeCode.ALLOCATION.getStatus());
            operationLogVo.setObjectId(allocationResVo.getAllocationCode());
            BasePage<LogData> pageList = operationLogService.getLogType(operationLogVo, 62);
            List<LogData> logDataList = new ArrayList<>();
            if (null != pageList.getDataList() && pageList.getDataList().size() > 0) {
                logDataList = pageList.getDataList();
            }
            allocationResVo.setLogDataList(logDataList);
        }
        return allocationResVo;

    }


    /**
     * 保存调拨单主体
     *
     * @param record
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @Save
    public Long insertSelective(Allocation record) {
        record.setCompanyCode(getUser().getCompanyCode());
        record.setCompanyName(getUser().getCompanyName());
        record.setCreateBy(getUser().getPersonName());
        record.setCreateById(getUser().getPersonId());
        record.setUpdateBy(getUser().getPersonName());
        record.setUpdateById(getUser().getPersonId());
        long k = allocationMapper.insertSelective(record);
        if (k > 0) {
            return record.getId();
        } else {
            throw new GroundRuntimeException("调拨单主体保存失败");
        }
    }


    /**
     * 批量插入调拨单sku
     *
     * @param record
     * @return
     */
    @Override
    public int saveList(List<AllocationProduct> record) {
        try {
            return allocationProductMapper.saveList(record);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("调拨单Sku保存失败");
            throw new GroundRuntimeException(e.getMessage());
        }
    }

    /**
     * 批量插入调拨单sku批次
     *
     * @param record
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    @SaveList
    public int saveListBatch(List<AllocationProductBatch> record) {
        try {
            int m = allocationProductBatchMapper.saveList(record);
            return m;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("调拨单Sku批次保存失败");
            throw new GroundRuntimeException(e.getMessage());
        }
    }


    /**
     * 有选择的更新调拨单实体
     *
     * @param record
     * @return
     */
    @Override
    @Transactional(rollbackFor = GroundRuntimeException.class)
    public int updateByPrimaryKeySelective(Allocation record) {
        try {
            return allocationMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("调拨单更新失败");
            throw new GroundRuntimeException(e.getMessage());
        }
    }


    //    @Async("myTaskAsyncPool")
    public WorkFlowRespVO workFlow(Long id, String formNo, String positionCode) {
        Allocation allocation1 = allocationMapper.selectByPrimaryKey(id);
        log.info("AllocationServiceImplProduct-workFlow-传入参数是：[{}]", JSON.toJSONString(id));
        try {
            AllocationTypeEnum allocationTypeEnum = AllocationTypeEnum.getAllocationTypeEnumByType(allocation1.getAllocationType());
            WorkFlowVO workFlowVO = new WorkFlowVO();
            //判断类型
            String baseUrl;
            if (allocationTypeEnum.equals(AllocationTypeEnum.SCRAP)) {
                baseUrl = workFlowBaseUrl.scrap;
            } else if (allocationTypeEnum.equals(AllocationTypeEnum.MOVE)) {
                baseUrl = workFlowBaseUrl.movement;
            } else if (allocationTypeEnum.equals(AllocationTypeEnum.ALLOCATION)) {
                baseUrl = workFlowBaseUrl.applyAllocattion;
            } else {
                throw new BizException(ResultCode.DATA_ERROR);
            }

            workFlowVO.setFormUrl(baseUrl + "?id=" + id + "&" + workFlowBaseUrl.authority);
            workFlowVO.setHost(workFlowBaseUrl.supplierHost);
            workFlowVO.setTitle(allocation1.getCreateBy() + "创建" + allocationTypeEnum.getTypeName() + "单");
            workFlowVO.setFormNo(formNo);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("auditPersonId", allocation1.getDirectSupervisorCode());
            workFlowVO.setVariables(jsonObject.toString());
            workFlowVO.setUpdateUrl(workFlowBaseUrl.callBackBaseUrl + allocationTypeEnum.getWorkFlow().getNum());
            workFlowVO.setPositionCode(positionCode);
            WorkFlowRespVO workFlowRespVO = callWorkFlowApi(workFlowVO, allocationTypeEnum.getWorkFlow());
            if (workFlowRespVO.getSuccess()) {
                // 更新调拨单审核状态
                Allocation allocation = new Allocation();
                allocation.setId(id);
                allocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_CHECK.getName());
                allocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_CHECK.getStatus());
                ((AllocationService) AopContext.currentProxy()).updateByPrimaryKeySelective(allocation);
                //存日志
                String applyTypeTitle = "";
                if (Objects.equals(allocation1.getAllocationType(), AllocationTypeEnum.ALLOCATION.getType())) {
                    applyTypeTitle = "调拨";
                } else if (Objects.equals(allocation1.getAllocationType(), AllocationTypeEnum.MOVE.getType())) {
                    applyTypeTitle = "移库";
                } else {
                    applyTypeTitle = "报废";
                }
                String content = ApplyStatus.APPROVAL.getContent().replace(Global.CREATE_BY, allocation1.getUpdateBy()).replace(Global.APPLY_TYPE, applyTypeTitle);
                supplierCommonService.getInstance(
                        allocation1.getAllocationCode() + "",
                        HandleTypeCoce.APPROVAL.getStatus(),
                        ObjectTypeCode.ALLOCATION.getStatus(),
                        content, null,
                        HandleTypeCoce.APPROVAL.getName()
                );

            } else {
                log.error("上传审批接口失败");
                log.error("失败原因是" + workFlowRespVO.getMsg());
                throw new GroundRuntimeException(workFlowRespVO.getMsg());
            }
            return workFlowRespVO;
        } catch (Exception e) {
            throw new GroundRuntimeException(e.getMessage());
        }
    }

    /**
     * 查询调拨状态
     *
     * @return
     */
    @Override
    public List<EnumReqVo> getAllocationStatus() {
        List<EnumReqVo> list = new ArrayList<>();
        AllocationEnum[] allocationEnums = AllocationEnum.values();
        for (AllocationEnum allocationEnum : allocationEnums) {
            list.add(new EnumReqVo(allocationEnum.getStatus(), allocationEnum.getName()));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSubmit(Byte status, String formNo) {
        Allocation allocation = new Allocation();
        Long id = allocationMapper.findIdByFormNo(formNo);
        allocation.setId(id);
        allocation.setAllocationStatusCode(status);
        allocation.setAllocationStatusName(AllocationEnum.getAllocationEnumNameByCode(status));
        int i = allocationMapper.updateByPrimaryKeySelective(allocation);


        return i;
    }

    /**
     * 获取wms返回状态更新数据
     *
     * @param allocationCode
     * @return
     */
    @Override
    public int updateWmsStatus(String allocationCode) {
        // 调拨状态编码(0.待审核，1.审核中，2.待出库，3.已出库，4.待入库，5. 已完成，6.取消，7.审核不通过)
        // 获取调拨数据
        Allocation allocation = allocationMapper.selectByCode(allocationCode);
        List<AllocationProductResVo> aProductLists = allocationProductMapper.selectByAllocationCode(allocationCode);
        List<AllocationProductBatchResVo> aProductBatchLists = allocationProductBatchMapper.selectByAllocationCode(allocationCode);
        // 接收对象
        AllocationWmsInSource aWmsInSource = new AllocationWmsInSource();
        List<AllocationWmsProductSource> aWmsInProSource = new ArrayList<>();
        List<BatchWmsInfo> aWmsInProBatchSource = new ArrayList<>();

        aWmsInSource.setInboundOderCode(allocation.getInboundOderCode());
        aWmsInSource.setAllocationCode(allocation.getAllocationCode());
        aWmsInSource.setCallOutWarehouseCode(allocation.getCallOutWarehouseCode());
        aWmsInSource.setCallOutWarehouseName(allocation.getCallOutWarehouseName());
        aWmsInSource.setCallInWarehouseCode(allocation.getCallInWarehouseCode());
        aWmsInSource.setCallInWarehouseName(allocation.getCallInWarehouseName());
        aWmsInSource.setCreateTime(new Date());
        aWmsInSource.setCreateById(allocation.getCreateById());
        aWmsInSource.setCreateByName(allocation.getCreateBy());
        aWmsInSource.setRemark(allocation.getRemark());
        for (AllocationProductResVo aProductList : aProductLists) {
            AllocationWmsProductSource aWmsProductList = new AllocationWmsProductSource();
            PurchaseSaleStockRespVo purchaseSaleStockRespVo = productSkuSalesInfoDao.selectBarCodeBySkuCode(aProductList.getSkuCode());
            if (purchaseSaleStockRespVo != null) {
                aWmsProductList.setUnitCode(purchaseSaleStockRespVo.getStockUnitCode());
                aWmsProductList.setUnitName(purchaseSaleStockRespVo.getStockUnitName());
                aWmsProductList.setSkuBarCode(purchaseSaleStockRespVo.getBarCode());
            }
            aWmsProductList.setSkuCode(aProductList.getSkuCode());
            aWmsProductList.setSkuName(aProductList.getSkuName());
            aWmsProductList.setTotalCount(String.valueOf(aProductList.getCallOutQuantity()));
            aWmsProductList.setColorName(aProductList.getColor());
            aWmsProductList.setModelNumber(aProductList.getModel());
            aWmsProductList.setLineCode(aProductList.getLineNum().toString());
            aWmsProductList.setColorName(aProductList.getColor());
            aWmsProductList.setModelNumber(aProductList.getModel());
            aWmsInProSource.add(aWmsProductList);
        }
        for (AllocationProductBatchResVo aProductBatchList : aProductBatchLists) {
            BatchWmsInfo aWmsProductBatchList = new BatchWmsInfo();
            aWmsProductBatchList.setSkuCode(aProductBatchList.getSkuCode());
            aWmsProductBatchList.setSkuName(aProductBatchList.getSkuName());
            aWmsProductBatchList.setLineCode(aProductBatchList.getLineNum());
            aWmsProductBatchList.setBatchCode(aProductBatchList.getCallInBatchNumber());
            aWmsProductBatchList.setBatchRemark(aProductBatchList.getBatchNumberRemark());
            aWmsProductBatchList.setTotalCount(aProductBatchList.getCalloutActualTotalCount());
            aWmsInProBatchSource.add(aWmsProductBatchList);
        }
        aWmsInSource.setDetailList(aWmsInProSource);
        aWmsInSource.setBatchInfo(aWmsInProBatchSource);
        LOGGER.info("调拨入单生成wms参数信息{}", JsonUtil.toJson(aWmsInSource));
        String url = urlConfig.WMS_API_URL2 + "/allocation/source/inbound";
        HttpClient httpClient = HttpClient.post(url).json(aWmsInSource).timeout(200000);
        HttpResponse orderDto = httpClient.action().result(HttpResponse.class);
        if (!orderDto.getCode().equals(MessageId.SUCCESS_CODE)) {
            return 0;
        }

        Allocation allocation1 = new Allocation();
        Long id = allocationMapper.findIdByAllocationCode(allocationCode);
        allocation1.setId(id);
        allocation1.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_FINISHED.getStatus());
        allocation1.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_FINISHED.getName());
        int i = allocationMapper.updateByPrimaryKeySelective(allocation);
        return i;
    }

    @Override
    public HttpResponse allocationWms(AllocationRequest request) {
        LOGGER.info("wms回传，开始更新调拨单的实际值：{}", JsonUtil.toJson(request));
        if (request == null) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        // 查询调拨单信息
        Allocation allocation = allocationMapper.selectByCode(request.getAllocationCode());
        allocation.setOutStockTime(Calendar.getInstance().getTime());
        allocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_OUTBOUND.getStatus());
        allocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_OUTBOUND.getName());
        int count = allocationMapper.updateByPrimaryKeySelective(allocation);
        // 更新调拨批次商品
        List<AllocationDetailRequest> detailList = request.getDetailList();
        for (AllocationDetailRequest a: detailList) {
            AllocationProduct allocationProduct = new AllocationProduct();
            allocationProduct.setAllocationCode(request.getAllocationCode());
            allocationProduct.setSkuCode(a.getSkuCode());
            allocationProduct.setLineNum(Long.valueOf(a.getLineCode()));
            allocationProduct.setCalloutActualTotalCount(a.getActualCount());
            allocationProductMapper.updateQuantityBySkuCodeAndSource(allocationProduct);
        }

        // 查看调拨单批次商品信息
        List<AllocationBatchRequest> batchList = request.getBatchList();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(batchList)) {
            for (AllocationBatchRequest detail : batchList) {
                Integer count1 = allocationProductBatchMapper.selectCountByCode(request.getAllocationCode(), detail.getSkuCode(), detail.getBatchCode());
                if (count1 > 0) {
                    // 更新
                    allocationProductBatchMapper.updateByBatch(detail);
                } else {
                    // 保存
                    List<StockBatch> stockBatches;
                    stockBatches = stockBatchDao.stockBatchByOutbound(detail.getSkuCode(), allocation.getCallOutWarehouseCode(), detail.getBatchCode());
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(stockBatches) && stockBatches.size() <= 0) {
                        stockBatches = stockBatchDao.stockBatchByReject(detail.getSkuCode(), allocation.getCallOutWarehouseCode(), null);
                    }
                    AllocationProductBatch allocationProductBatch = new AllocationProductBatch();
                    allocationProductBatch.setAllocationCode(request.getAllocationCode());
                    allocationProductBatch.setCallOutBatchNumber(detail.getBatchCode());
                    allocationProductBatch.setCallOutBatchInfoCode(detail.getBatchInfoCode());
                    allocationProductBatch.setSupplierCode(detail.getSupplierCode());
                    allocationProductBatch.setSkuCode(detail.getSkuCode());
                    allocationProductBatch.setSkuName(detail.getSkuName());
                    allocationProductBatch.setProductDate(detail.getProductDate());
                    allocationProductBatch.setCallOutActualTotalCount(detail.getActualTotalCount());
                    allocationProductBatch.setQuantity(detail.getTotalCount());
                    allocationProductBatch.setLineNum(Long.valueOf(detail.getLineCode()));
                    allocationProductBatch.setTaxPrice(stockBatches.get(0).getTaxCost() == null ? BigDecimal.ZERO : stockBatches.get(0).getTaxCost());
                    allocationProductBatch.setTax(stockBatches.get(0).getTaxRate() == null ? BigDecimal.ZERO : stockBatches.get(0).getTaxRate());
                    allocationProductBatchMapper.insertSelective(allocationProductBatch);
                }
            }
        }

        List<AllocationProductResVo> aProductLists = allocationProductMapper.selectByAllocationCode(request.getAllocationCode());
        List<AllocationProductBatchResVo> aProductBatchLists = allocationProductBatchMapper.selectByAllocationCode(request.getAllocationCode());
        // 调用完库存锁定调用同步dl库存数据
        StockChangeRequest stockChangeDlRequest = new StockChangeRequest();
        stockChangeDlRequest.setOrderCode(allocation.getAllocationCode());
        stockChangeDlRequest.setOrderType(Global.DL_ORDER_TYPE_3);
        stockChangeDlRequest.setOperationType(Global.DL_OPERATION_TYPE_2);
        synchrdlStockChange(allocation, aProductLists, aProductBatchLists, stockChangeDlRequest);
        LOGGER.info("调用完库存锁定调用同步dl库存参数数据:{}", JsonUtil.toJson(stockChangeDlRequest));
        dlService.stockChange(stockChangeDlRequest);

        LOGGER.info("wms回传-更新调拨单的实际值：{}", count);  // 调拨出库不调用sap 入库完成后调用sap
        return HttpResponse.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String workFlowCallback(WorkFlowCallbackVO vo1) {
        return nativeWorkFlowCallback(vo1);
    }

    /**
     * 调拨单生成出库单
     *
     * @param id 调拨单id
     * @return 出库单编码
     */
    public String createOutbound(Long id) {
        try {
            AllocationToOutboundVo allocationResVo = new AllocationToOutboundVo();
            Allocation allocation = allocationMapper.selectByPrimaryKey(id);
            BeanCopyUtils.copy(allocation, allocationResVo);

            List<AllocationProductToOutboundVo> list = allocationProductBatchMapper.selectByPictureUrlAllocationCode(allocation.getAllocationCode());
            allocationResVo.setSkuList(list);
            // 转化成出库单
            OutboundReqVo convert = new AllocationResVo2OutboundReqVoConverter(supplierApiService).convert(allocationResVo);

            return outboundService.save(convert);
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("保存出库单失败");
        }
    }

    private List<StockInfoRequest> allocationProductTransStock(Allocation allocation, List<AllocationProduct> products) {
        List<StockInfoRequest> stockVoRequests = Lists.newArrayList();
        if (CollectionUtils.isNotEmptyCollection(products)) {
//            StockVoRequest stockVoRequest = null;
            for (AllocationProduct allocationProduct : products) {
                StockInfoRequest stockVoRequest = new StockInfoRequest();
                // 设置公司名称编码
                stockVoRequest.setCompanyCode(allocation.getCompanyCode());
                stockVoRequest.setCompanyName(allocation.getCompanyName());
                // 设置物流中心名称编码
                //如果改在途数，需要设置为入库的仓库
                if (allocation.getFlag()) {
                    // 设置仓库名称编码
                    stockVoRequest.setTransportCenterCode(allocation.getCallOutLogisticsCenterCode());
                    stockVoRequest.setTransportCenterName(allocation.getCallOutLogisticsCenterName());
                    //设置库房名称编码
                    stockVoRequest.setWarehouseCode(allocation.getCallOutWarehouseCode());
                    stockVoRequest.setWarehouseName(allocation.getCallOutWarehouseName());
                } else {
                    stockVoRequest.setTransportCenterCode(allocation.getCallInLogisticsCenterCode());
                    stockVoRequest.setTransportCenterName(allocation.getCallInLogisticsCenterName());
                    //设置库房名称编码
                    stockVoRequest.setWarehouseCode(allocation.getCallInWarehouseCode());
                    stockVoRequest.setWarehouseName(allocation.getCallInWarehouseName());
                }
                //设置sku编号名称
                stockVoRequest.setSkuCode(allocationProduct.getSkuCode());
                stockVoRequest.setSkuName(allocationProduct.getSkuName());
                // 变化数/税率
                stockVoRequest.setChangeCount(allocationProduct.getQuantity());
                stockVoRequest.setTaxRate(allocationProduct.getTax());
                //设置类型
                stockVoRequest.setDocumentType(Global.DOCUMENT_TYPE_6);
                stockVoRequest.setDocumentCode(allocation.getAllocationCode());
                stockVoRequest.setSourceDocumentType(Global.DOCUMENT_TYPE_6);
                stockVoRequest.setSourceDocumentCode(allocation.getAllocationCode());
                stockVoRequest.setOperatorId(allocation.getUpdateById());
                stockVoRequest.setOperatorName(allocation.getUpdateBy());
                stockVoRequest.setRemark(allocation.getRemark());
                stockVoRequests.add(stockVoRequest);
            }
        }
        return stockVoRequests;
    }

    private List<StockBatchInfoRequest> allocationBatchTransStock(Allocation allocation, List<AllocationProductBatch> productBatchs) {
        List<StockBatchInfoRequest> stockBatchInfoRequests = Lists.newArrayList();
        if (CollectionUtils.isNotEmptyCollection(productBatchs)) {
            for (AllocationProductBatch allocationProductBatch : productBatchs) {
                String callOutBatchNumber = allocationProductBatch.getCallOutBatchNumber();
                if (callOutBatchNumber != null) {
                    StockBatchInfoRequest stockBatchInfoRequest = new StockBatchInfoRequest();
                    // 设置公司名称编码
                    stockBatchInfoRequest.setCompanyCode(allocation.getCompanyCode());
                    stockBatchInfoRequest.setCompanyName(allocation.getCompanyName());
                    // 设置物流中心名称编码
                    // 设置仓库名称编码
                    stockBatchInfoRequest.setTransportCenterCode(allocation.getCallOutLogisticsCenterCode());
                    stockBatchInfoRequest.setTransportCenterName(allocation.getCallOutLogisticsCenterName());
                    //设置库房名称编码
                    stockBatchInfoRequest.setWarehouseCode(allocation.getCallOutWarehouseCode());
                    stockBatchInfoRequest.setWarehouseName(allocation.getCallOutWarehouseName());
                    //设置sku编号名称
                    stockBatchInfoRequest.setSkuCode(allocationProductBatch.getSkuCode());
                    stockBatchInfoRequest.setSkuName(allocationProductBatch.getSkuName());
                    // 变化数/税率/成本
                    stockBatchInfoRequest.setChangeCount(allocationProductBatch.getQuantity());
                    stockBatchInfoRequest.setTaxRate(allocationProductBatch.getTax().setScale(4, BigDecimal.ROUND_HALF_UP));
                    stockBatchInfoRequest.setTaxCost(allocationProductBatch.getTaxPrice());
                    //设置类型
                    stockBatchInfoRequest.setBatchCode(allocationProductBatch.getCallOutBatchNumber());
                    stockBatchInfoRequest.setProductDate(allocationProductBatch.getProductDate());
                    stockBatchInfoRequest.setBeOverdueDate(allocationProductBatch.getBeOverdueDate());
                    stockBatchInfoRequest.setSupplierCode(allocationProductBatch.getSupplierCode());
                    stockBatchInfoRequest.setDocumentType(AllocationTypeEnum.getAll().get(allocation.getAllocationType()).getLockType());
                    stockBatchInfoRequest.setDocumentCode(allocation.getAllocationCode());
                    stockBatchInfoRequest.setSourceDocumentType(AllocationTypeEnum.getAll().get(allocation.getAllocationType()).getLockType());
                    stockBatchInfoRequest.setSourceDocumentCode(allocation.getAllocationCode());
                    stockBatchInfoRequest.setOperatorId(allocation.getUpdateById());
                    stockBatchInfoRequest.setOperatorName(allocation.getUpdateBy());
                    stockBatchInfoRequest.setBatchRemark(allocationProductBatch.getBatchNumberRemark());
                    stockBatchInfoRequests.add(stockBatchInfoRequest);
                }
            }
        }
        return stockBatchInfoRequests;
    }

    /*private List<StockVoRequest> allocationProductTransStock(Allocation allocation, List<AllocationProduct> products) {
        List<StockVoRequest> stockVoRequests = Lists.newArrayList();
        if(CollectionUtils.isNotEmptyCollection(products)){
//            StockVoRequest stockVoRequest = null;
            for (AllocationProduct allocationProduct : products) {
                StockVoRequest stockVoRequest = new StockVoRequest();
                // 设置公司名称编码
                stockVoRequest.setCompanyCode(allocation.getCompanyCode());
                stockVoRequest.setCompanyName(allocation.getCompanyName());
                // 设置物流中心名称编码
                //如果改在途数，需要设置为入库的仓库
                 if(allocation.getFlag()){
                    stockVoRequest.setTransportCenterCode(allocation.getCallOutLogisticsCenterCode());
                    stockVoRequest.setTransportCenterName(allocation.getCallOutLogisticsCenterName());
                    //设置库房名称编码
                    stockVoRequest.setWarehouseCode(allocation.getCallOutWarehouseCode());
                    stockVoRequest.setWarehouseName(allocation.getCallOutWarehouseName());
                 }else {
                     stockVoRequest.setTransportCenterCode(allocation.getCallInLogisticsCenterCode());
                     stockVoRequest.setTransportCenterName(allocation.getCallInLogisticsCenterName());
                     //设置库房名称编码
                     stockVoRequest.setWarehouseCode(allocation.getCallInWarehouseCode());
                     stockVoRequest.setWarehouseName(allocation.getCallInWarehouseName());
                 }
                //设置采购组编码名称
                stockVoRequest.setPurchaseGroupCode(allocation.getPurchaseGroupCode());
                stockVoRequest.setPurchaseGroupName(allocation.getPurchaseGroupName());
                //设置sku编号名称
                stockVoRequest.setSkuCode(allocationProduct.getSkuCode());
                stockVoRequest.setSkuName(allocationProduct.getSkuName());
                stockVoRequest.setChangeNum(allocationProduct.getQuantity());
                //设置类型
                stockVoRequest.setDocumentType(AllocationTypeEnum.getAll().get(allocation.getAllocationType()).getLockType());
                stockVoRequest.setDocumentNum(allocation.getAllocationCode());
                stockVoRequest.setOperator(allocation.getUpdateBy());
                stockVoRequest.setRemark(allocation.getRemark());
                stockVoRequests.add(stockVoRequest);
            }
        }
        return stockVoRequests;
    }*/

    public List<AllocationProduct> productbatchTransProduct(List<AllocationProductBatch> batches) {
        Map<String, List<AllocationProductBatch>> batchMap = batches.stream().collect(Collectors.groupingBy(AllocationProductBatch::getSkuCode));
        List<AllocationProduct> products = Lists.newArrayList();
        batchMap.forEach((k, v) -> {
            AllocationProduct product = BeanCopyUtils.copy(v.get(0), AllocationProduct.class);
            //合并数量
            Long totalNum = v.stream().mapToLong(AllocationProductBatch::getQuantity).sum();
            product.setQuantity(totalNum);
            //合并含税总成本totalTaxAmount
            BigDecimal totalTaxAmount = v.stream().map(AllocationProductBatch::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            product.setTaxAmount(totalTaxAmount);
            products.add(product);
        });

        return products;
    }

    /**
     * 审核回调接口
     *
     * @param vo1
     * @return
     * @author zth
     * @date 2019/1/15
     */
    @Override
    public String nativeWorkFlowCallback(WorkFlowCallbackVO vo1) {
        //通过编码查询实体
        WorkFlowCallbackVO vo = updateSupStatus(vo1);
        // 通过流水编码查询调拨单实体
        Allocation oldAllocation = new Allocation();
        AllocationDTO allocation = allocationMapper.selectByFormNO1(vo1.getFormNo());
        allocation.setUpdateBy(vo.getApprovalUserName());
        oldAllocation.setId(allocation.getId());
        oldAllocation.setUpdateBy(vo.getApprovalUserName());
        oldAllocation.setUpdateTime(new Date());
        if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
            String content = ApplyStatus.APPROVAL_SUCCESS.getContent().replace(Global.CREATE_BY, allocation.getUpdateBy()).replace(Global.AUDITOR_BY, vo.getApprovalUserName());
            supplierCommonService.getInstance(
                    allocation.getAllocationCode() + "",
                    HandleTypeCoce.APPROVAL_SUCCESS.getStatus(),
                    ObjectTypeCode.ALLOCATION.getStatus(),
                    content, null,
                    HandleTypeCoce.APPROVAL_SUCCESS.getName(),
                    vo.getApprovalUserName()
            );
            //审批成功
            //生成出库单并且返回出库单编码
            //生成入库单
            String outboundCode = null;
//            String inboundCode = null;
            AllocationTypeEnum enumByType = AllocationTypeEnum.getAllocationTypeEnumByType(allocation.getAllocationType());
            LOGGER.info("调拨单数据转出库单前数据参数{}", JsonUtil.toJson(allocation));
            OutboundReqVo convert = new AllocationOrderToOutboundConverter(warehouseService, enumByType, productSkuPicturesDao).convert(allocation);
            LOGGER.info("保存出库单数据参数{}", JsonUtil.toJson(convert));
            outboundCode = outboundService.save(convert);
//            if(!AllocationTypeEnum.SCRAP.getType().equals(allocation.getAllocationType())){
//                InboundReqSave convert1 = new AllocationOrderToInboundConverter(warehouseService, enumByType,productSkuPicturesDao).convert(allocation);
////                inboundCode = inboundService.saveInbound(convert1);
//            }
            //调拨 增加在途数
//            if(AllocationTypeEnum.ALLOCATION.getType().equals(allocation.getAllocationType())){
//                allocation.setFlag(false);
//                StockChangeRequest stockChangeRequest = new StockChangeRequest();
//                stockChangeRequest.setOperationType(7);
//                stockChangeRequest.setOrderCode(allocation.getAllocationCode());
//                List<StockVoRequest> list1 = allocationProductTransStock(allocation,allocation.getProducts());
//                stockChangeRequest.setStockVoRequests(list1);
//                stockService.changeStock(stockChangeRequest);
//            }
//            String outboundOderCode = createOutbound(allocation.getId());
            oldAllocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_TO_OUTBOUND.getStatus());
            oldAllocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_TO_OUTBOUND.getName());
            oldAllocation.setOutboundOderCode(outboundCode);
//            oldAllocation.setInboundOderCode(inboundCode);
            //更新审核状态
            int k = ((AllocationService) AopContext.currentProxy()).updateByPrimaryKeySelective(oldAllocation);
            if (k != 1) {
                throw new BizException(ResultCode.UPDATE_ERROR);
            }

            String content2 = "";
            if (Objects.equals(allocation.getAllocationType(), AllocationTypeEnum.ALLOCATION.getType())) {
                content2 = HandleTypeCoce.OUTBOUND_ALLOCATION.getName();
            } else if (Objects.equals(allocation.getAllocationType(), AllocationTypeEnum.MOVE.getType())) {
                content2 = HandleTypeCoce.OUTBOUND_MOVEMENT.getName();
            } else {
                content2 = HandleTypeCoce.OUTBOUND_SCRAP.getName();
            }
            supplierCommonService.getInstance(allocation.getAllocationCode() + "", AllocationEnum.ALLOCATION_TYPE_TO_OUTBOUND.getStatus(), ObjectTypeCode.ALLOCATION.getStatus(), content2, null, AllocationEnum.ALLOCATION_TYPE_TO_OUTBOUND.getName(), vo.getApprovalUserName());

            // 审核通过的情况下调用wms
            // 调拨状态编码(0.待审核，1.审核中，2.待出库，3.已出库，4.待入库，5. 已完成，6.取消，7.审核不通过)
            // 待出库状态时调用wms。 出库
            // 获取调拨主数据
            Allocation allocation1 = allocationMapper.selectByFormNO(vo1.getFormNo());
            List<AllocationProductResVo> aProductLists = allocationProductMapper.selectByAllocationCode(allocation1.getAllocationCode());
            List<AllocationProductBatchResVo> aProductBatchLists = allocationProductBatchMapper.selectByAllocationCode(allocation1.getAllocationCode());
            if (Objects.equals(allocation1.getAllocationType(), AllocationTypeEnum.ALLOCATION.getType())) {
                // 调拨接收对象
                AllocationWmsOutSource aWmsOutSource = new AllocationWmsOutSource();
                List<AllocationWmsProductSource> aWmsOutProSource = new ArrayList<>();
                List<BatchWmsInfo> aWmsOutProBatchSource = new ArrayList<>();
                // 调拨主表数据
                //BeanUtils.copyProperties(aWmsOutSource,allocation1);
                aWmsOutSource.setOutboundOderCode(allocation1.getOutboundOderCode());
                aWmsOutSource.setAllocationCode(allocation1.getAllocationCode());
                aWmsOutSource.setCallOutWarehouseCode(allocation1.getCallOutWarehouseCode());
                aWmsOutSource.setCallOutWarehouseName(allocation1.getCallOutWarehouseName());
                aWmsOutSource.setCallInWarehouseCode(allocation1.getCallInWarehouseCode());
                aWmsOutSource.setCallInWarehouseName(allocation1.getCallInWarehouseName());
                aWmsOutSource.setCreateById(allocation1.getCreateById());
                aWmsOutSource.setCreateByName(allocation1.getCreateBy());
                // 获取调入方地址
                WarehouseDTO warehouseByCode = warehouseDao.getWarehouseByCode(allocation1.getCallInWarehouseCode());
                if (warehouseByCode != null) {
                    aWmsOutSource.setReceiverName(warehouseByCode.getContact());
                    aWmsOutSource.setReceiverMobil(warehouseByCode.getPhone());
                    aWmsOutSource.setReceiverProvince(warehouseByCode.getProvinceName());
                    aWmsOutSource.setReceiverCity(warehouseByCode.getCityName());
                    aWmsOutSource.setReceiverCounty(warehouseByCode.getCountyName());
                    aWmsOutSource.setAddress(warehouseByCode.getProvinceName() + warehouseByCode.getCityName() + warehouseByCode.getCountyName() + warehouseByCode.getDetailedAddress());
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (allocation1.getCreateTime() != null) {
                    String format = sdf.format(allocation1.getCreateTime());
                    aWmsOutSource.setCreateTime(format);
                }
//                aWmsOutSource.setReceiverName();
//                aWmsOutSource.setReceiverMobil();
//                aWmsOutSource.setReceiverProvince();
//                aWmsOutSource.setReceiverCity();
//                aWmsOutSource.setReceiverCounty();
//                aWmsOutSource.setAddress();
                // 调拨商品数据
                for (AllocationProductResVo aProductList : aProductLists) {
                    PurchaseSaleStockRespVo purchaseSaleStockRespVo = productSkuSalesInfoDao.selectBarCodeBySkuCode(aProductList.getSkuCode());
                    log.info("sku条形码{}", JSON.toJSONString(purchaseSaleStockRespVo));
                    AllocationWmsProductSource aWmsProductList = new AllocationWmsProductSource();
                    // BeanUtils.copyProperties(aWmsProductList,aProductList);
                    aWmsProductList.setLineCode(String.valueOf(aProductList.getLineNum()));
                    aWmsProductList.setSkuCode(aProductList.getSkuCode());
                    aWmsProductList.setSkuName(aProductList.getSkuName());
                    aWmsProductList.setTotalCount(String.valueOf(aProductList.getQuantity()));
                    aWmsProductList.setColorName(aProductList.getColor());
                    aWmsProductList.setModelNumber(aProductList.getModel());
                    if (purchaseSaleStockRespVo != null) {
                        aWmsProductList.setUnitCode(purchaseSaleStockRespVo.getStockUnitCode());
                        aWmsProductList.setUnitName(purchaseSaleStockRespVo.getStockUnitName());
                        aWmsProductList.setSkuBarCode(purchaseSaleStockRespVo.getBarCode());
                    }
                    aWmsOutProSource.add(aWmsProductList);
                }
                // 调拨商品批次数据
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(aProductBatchLists) && aProductBatchLists.size() > 0) {
                    for (AllocationProductBatchResVo aProductBatchList : aProductBatchLists) {
                        BatchWmsInfo aWmsProductBatchList = new BatchWmsInfo();
                        //  BeanUtils.copyProperties(aWmsProductBatchList,aProductBatchList);
                        aWmsProductBatchList.setBatchCode(aProductBatchList.getCallInBatchNumber());
                        aWmsProductBatchList.setSkuCode(aProductBatchList.getSkuCode());
                        aWmsProductBatchList.setSkuName(aProductBatchList.getSkuName());
                        aWmsProductBatchList.setProdcutDate(aProductBatchList.getProductDate());
                        aWmsProductBatchList.setBatchRemark(aProductBatchList.getBatchNumberRemark());
                        aWmsProductBatchList.setTotalCount(aProductBatchList.getQuantity());
                        aWmsProductBatchList.setLineCode(aProductBatchList.getLineNum());
                        aWmsOutProBatchSource.add(aWmsProductBatchList);
                    }
                }
                aWmsOutSource.setDetailList(aWmsOutProSource);
                aWmsOutSource.setBatchInfo(aWmsOutProBatchSource);
                LOGGER.error("审批成功后：调拨调用wms,参数：{}", JsonUtil.toJson(aWmsOutSource));
                String url = urlConfig.WMS_API_URL2 + "/allocation/source/outbound";
                HttpClient httpClient = HttpClient.post(url).json(aWmsOutSource).timeout(200000);
                HttpResponse orderDto = httpClient.action().result(HttpResponse.class);
                if (!orderDto.getCode().equals(MessageId.SUCCESS_CODE)) {
                    LOGGER.error("审批成功后：调拨调用wms失败,wms返回信息参数：{}", String.valueOf(orderDto.getMessage()));
//                        return "调用wms失败";
                    return orderDto.getMessage();
                }
            }
            if (Objects.equals(allocation1.getAllocationType(), AllocationTypeEnum.MOVE.getType())) {
                movementWms(allocation, allocation1, aProductLists, aProductBatchLists);
            }
            return "success";
        } else if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL_FAILED.getNumber())) {

            String content = ApplyStatus.APPROVAL_FAILED.getContent().replace(Global.CREATE_BY, allocation.getUpdateBy()).replace(Global.AUDITOR_BY, vo.getApprovalUserName());
            supplierCommonService.getInstance(
                    allocation.getAllocationCode() + "",
                    HandleTypeCoce.APPROVAL_FAILED.getStatus(),
                    ObjectTypeCode.ALLOCATION.getStatus(),
                    content, null,
                    HandleTypeCoce.APPROVAL_SUCCESS.getName(),
                    vo.getApprovalUserName()
            );
            // 审核不通过
            //  通过编码查询sku
            // 解锁被锁的sku 编码
            //StockChangeRequest stockChangeRequest = new StockChangeRequest();
            //stockChangeRequest.setOperationType(3);
            //stockChangeRequest.setOrderCode(allocation.getAllocationCode());
            //List<StockVoRequest> list1 = allocationProductTransStock(allocation,allocation.getProducts());
            //stockChangeRequest.setStockVoRequests(list1);
            //stockService.changeStock(stockChangeRequest);
            ChangeStockRequest stockChangeRequest = new ChangeStockRequest();
            stockChangeRequest.setOperationType(3);
            List<StockInfoRequest> list1 = allocationProductTransStock(allocation, allocation.getProducts());
            stockChangeRequest.setStockList(list1);
            List<StockBatchInfoRequest> batchList1 = allocationBatchTransStock(allocation, allocation.getList());
            stockChangeRequest.setStockBatchList(batchList1);
            // 调用锁定库存数
            stockService.stockAndBatchChange(stockChangeRequest);

            oldAllocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_REJECTED.getStatus());
            oldAllocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_REJECTED.getName());
            //更新状态
            ((AllocationService) AopContext.currentProxy()).updateByPrimaryKeySelective(oldAllocation);
            return "success";
        } else if (vo.getApplyStatus().intValue() == ApplyStatus.REVOKED.getNumber()) {
            oldAllocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_CANCEL.getStatus());
            oldAllocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_CANCEL.getName());
            ((AllocationService) AopContext.currentProxy()).updateByPrimaryKeySelective(oldAllocation);
            // 打印撤销的日志
            String content = ApplyStatus.APPROVAL_FAILED.getContent().replace(Global.CREATE_BY, allocation.getUpdateBy()).replace(Global.AUDITOR_BY, vo.getApprovalUserName());
            supplierCommonService.getInstance(
                    allocation.getAllocationCode() + "",
                    HandleTypeCoce.REVOKED.getStatus(),
                    ObjectTypeCode.ALLOCATION.getStatus(),
                    content, null,
                    HandleTypeCoce.REVOKED.getName(),
                    vo.getApprovalUserName()
            );
            //StockChangeRequest stockChangeRequest = new StockChangeRequest();
            //stockChangeRequest.setOperationType(3);
            //stockChangeRequest.setOrderCode(allocation.getAllocationCode());
            //List<StockVoRequest> list1 = allocationProductTransStock(allocation,allocation.getProducts());
            //stockChangeRequest.setStockVoRequests(list1);
            //stockService.changeStock(stockChangeRequest);
            ChangeStockRequest stockChangeRequest = new ChangeStockRequest();
            stockChangeRequest.setOperationType(3);
            List<StockInfoRequest> list1 = allocationProductTransStock(allocation, allocation.getProducts());
            stockChangeRequest.setStockList(list1);
            List<StockBatchInfoRequest> batchList1 = allocationBatchTransStock(allocation, allocation.getList());
            stockChangeRequest.setStockBatchList(batchList1);
            return "success";
        } else if (vo.getApplyStatus().equals(ApplyStatus.APPROVAL.getNumber())) {
            //审批中
            return "success";
        } else {
            return "false";
        }
    }

    public HttpResponse movementWms(AllocationDTO allocation, Allocation allocation1, List<AllocationProductResVo> aProductLists, List<AllocationProductBatchResVo> aProductBatchLists) {
        // 移库接收对象
        MovementWmsReqVo movementWmsReqVo = new MovementWmsReqVo();
        List<MovementWmsProductReqVo> movementWmsProductoLists = new ArrayList<>();
        List<BatchWmsInfo> movementWmsProductBatchLists = new ArrayList<>();
        // 获取调入方地址
        WarehouseDTO warehouseByCode = warehouseDao.getWarehouseByCode(allocation1.getCallInWarehouseCode());
        // 需要调用库房配置管理 判断移库类型  0.wms发起移库   1.分别发起移库   2.同时发起移库
        WarehouseConfigReq warehouseConfigReq = new WarehouseConfigReq();
        warehouseConfigReq.setWarehouseCode(allocation1.getCallOutWarehouseCode());
        LOGGER.info("审批成功后：移库调用wms获取库房配置信息参数:{}", JSON.toJSON(warehouseConfigReq));
        String movementUrl = urlConfig.WMS_API_URL2 + "/storehouseConfig/search/info";
        HttpClient movementUrlHttpClient = HttpClient.get(movementUrl).timeout(200000);
        movementUrlHttpClient.addParameter("storehouse_code", allocation1.getCallOutWarehouseCode());
        HttpResponse<WarehouseConfigResp> movementUrlResult = movementUrlHttpClient.action().result(new TypeReference<HttpResponse<WarehouseConfigResp>>() {
        });
        if (!movementUrlResult.getCode().equals(MessageId.SUCCESS_CODE)) {
            LOGGER.error("审批成功后：移库调用wms失败,wms返回信息参数：", movementUrlResult.getMessage());
//                        return "调用wms失败";
            return HttpResponse.failure(null, movementUrlResult.getMessage());
        }
        WarehouseConfigResp data = movementUrlResult.getData();
        if (Global.MOVEMENT_TYPE_0 == data.getMovementType()) {
            // wms发起移库不用操作
        } else if (Global.MOVEMENT_TYPE_1 == data.getMovementType()) {
            // 传出库
            movementWmsReqVo.setFlag(null);
            if (allocation1.getInboundOderCode() == null) {
                movementWmsReqVo.setFlag("0");
            } else {
                movementWmsReqVo.setFlag("1");
            }
        } else if (Global.MOVEMENT_TYPE_2 == data.getMovementType()) {
            AllocationTypeEnum enumByTypeIn = AllocationTypeEnum.getAllocationTypeEnumByType(allocation1.getAllocationType());
            InboundReqSave convert1 = new AllocationOrderToInboundConverter(warehouseService, enumByTypeIn, productSkuPicturesDao).convert(allocation);
            String redisCode = codeUtils.getRedisCode(EncodingRuleType.IN_BOUND_CODE);
            convert1.setInboundOderCode(redisCode);
            String inboundOderCode = inboundService.saveInbound(convert1);
            //更改调拨在途数
            allocation.setInboundOderCode(inboundOderCode);
            allocation.setAllocationStatusCode(AllocationEnum.ALLOCATION_TYPE_INBOUND.getStatus());
            allocation.setAllocationStatusName(AllocationEnum.ALLOCATION_TYPE_INBOUND.getName());
            allocationMapper.updateByPrimaryKeySelective(allocation);
            // 出入库都传
            movementWmsReqVo.setFlag("2");
        } else {
            return HttpResponse.failure(null, ResultCode.NOT_HAVE_PARAM);
        }
        // 移库主表数据
        BeanUtils.copyProperties(allocation1, movementWmsReqVo);
        movementWmsReqVo.setTransferOrderCode(allocation1.getAllocationCode());
        movementWmsReqVo.setOutboundWarehouseCode(allocation1.getCallOutWarehouseCode());
        movementWmsReqVo.setOutboundWarehouseName(allocation1.getCallOutWarehouseName());
        movementWmsReqVo.setOutboundOrderCode(allocation1.getOutboundOderCode());
        movementWmsReqVo.setInboundWarehouseCode(allocation1.getCallInWarehouseCode());
        movementWmsReqVo.setInboundWarehouseName(allocation1.getCallInWarehouseName());
        movementWmsReqVo.setInboundOrderCode(allocation1.getInboundOderCode());
        movementWmsReqVo.setCreateBy(allocation1.getCreateById());
        movementWmsReqVo.setCreateByName(allocation1.getCreateBy());
        movementWmsReqVo.setRemark(allocation1.getRemark());
        if (warehouseByCode != null) {
            movementWmsReqVo.setOrderContactPhone(warehouseByCode.getPhone());
            movementWmsReqVo.setOrderContacts(warehouseByCode.getContact());
            movementWmsReqVo.setReceiverName(warehouseByCode.getContact());
            movementWmsReqVo.setReceiverMobil(warehouseByCode.getPhone());
            movementWmsReqVo.setReceiverProvince(warehouseByCode.getProvinceName());
            movementWmsReqVo.setReceiverCity(warehouseByCode.getCityName());
            movementWmsReqVo.setReceiverCounty(warehouseByCode.getCountyName());
            movementWmsReqVo.setAddress(warehouseByCode.getProvinceName() + warehouseByCode.getCityName() + warehouseByCode.getCountyName() + warehouseByCode.getDetailedAddress());
        }
        // 移库商品表数据
        for (AllocationProductResVo aProductList : aProductLists) {
            PurchaseSaleStockRespVo purchaseSaleStockRespVo = productSkuSalesInfoDao.selectBarCodeBySkuCode(aProductList.getSkuCode());
            MovementWmsProductReqVo movementWmsProductReqVo = new MovementWmsProductReqVo();
            BeanUtils.copyProperties(aProductList, movementWmsProductReqVo);
            movementWmsProductReqVo.setLineCode(String.valueOf(aProductList.getLineNum()));
            movementWmsProductReqVo.setActualMinNum(String.valueOf(aProductList.getQuantity()));
            movementWmsProductReqVo.setTransferRemark(allocation1.getRemark());
            movementWmsProductReqVo.setSkuCode(purchaseSaleStockRespVo.getProductSkuCode());
            movementWmsProductReqVo.setSkuName(purchaseSaleStockRespVo.getProductSkuName());
            if (purchaseSaleStockRespVo != null) {
                movementWmsProductReqVo.setPackageName(purchaseSaleStockRespVo.getStockUnitName());
                movementWmsProductReqVo.setSkuBarCode(purchaseSaleStockRespVo.getBarCode());
            }
            movementWmsProductoLists.add(movementWmsProductReqVo);
        }
        // 移库商品批次表数据
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(aProductBatchLists)) {
            WarehouseDTO warehouse = warehouseDao.getWarehouseByCode(allocation1.getCallOutWarehouseCode());
            for (AllocationProductBatchResVo aProductBatchList : aProductBatchLists) {
                // 移库入库情况下不需要
                if (movementWmsReqVo.getInboundOrderCode() == null) {
                    // 判断是否为指定批次
                    Integer exist = 0;
                    if (warehouse.getBatchManage().equals(Global.BATCH_MANAGE_2) || warehouse.getBatchManage().equals(Global.BATCH_MANAGE_4) || warehouse.getBatchManage().equals(Global.BATCH_MANAGE_6)) {
                        exist = productSkuBatchMapper.productSkuBatchExist(aProductBatchList.getSkuCode(), warehouse.getWarehouseCode());
                    }
                    if (warehouse.getBatchManage().equals(Global.BATCH_MANAGE_1) || exist > 0) {
                        BatchWmsInfo aWmsProductBatchList = new BatchWmsInfo();
                        BeanUtils.copyProperties(aProductBatchList, aWmsProductBatchList);
                        aWmsProductBatchList.setLineCode(aProductBatchList.getLineNum());
                        aWmsProductBatchList.setBatchCode(aProductBatchList.getBatchNumber());
                        aWmsProductBatchList.setBatchRemark(aProductBatchList.getBatchNumberRemark());
                        aWmsProductBatchList.setTotalCount(aProductBatchList.getQuantity());
                        aWmsProductBatchList.setSkuCode(aProductBatchList.getSkuCode());
                        aWmsProductBatchList.setSkuName(aProductBatchList.getSkuName());
                        movementWmsProductBatchLists.add(aWmsProductBatchList);
                    }
                } else {
                    BatchWmsInfo aWmsProductBatchList = new BatchWmsInfo();
                    BeanUtils.copyProperties(aProductBatchList, aWmsProductBatchList);
                    aWmsProductBatchList.setLineCode(aProductBatchList.getLineNum());
                    aWmsProductBatchList.setBatchCode(aProductBatchList.getBatchNumber());
                    aWmsProductBatchList.setBatchRemark(aProductBatchList.getBatchNumberRemark());
                    aWmsProductBatchList.setTotalCount(aProductBatchList.getQuantity());
                    aWmsProductBatchList.setSkuCode(aProductBatchList.getSkuCode());
                    aWmsProductBatchList.setSkuName(aProductBatchList.getSkuName());
                    aWmsProductBatchList.setProdcutDate(aProductBatchList.getProductDate());
                    movementWmsProductBatchLists.add(aWmsProductBatchList);
                }
            }
        }
        movementWmsReqVo.setDetailList(movementWmsProductoLists);
        movementWmsReqVo.setBatchInfoList(movementWmsProductBatchLists);
        LOGGER.info("审批成功后：移库调用wms参数:{}", JsonUtil.toJson(movementWmsReqVo));
        String url = urlConfig.WMS_API_URL2 + "/infoPushAndInquiry/source/transferInfoPush";
        HttpClient httpClient = HttpClient.post(url).json(movementWmsReqVo).timeout(200000);
        HttpResponse orderDto = httpClient.action().result(HttpResponse.class);
        if (!orderDto.getCode().equals(MessageId.SUCCESS_CODE)) {
            LOGGER.error("审批成功后：移库调用wms失败,wms返回信息:" + orderDto.getMessage() + "参数：", JSON.toJSON(movementWmsReqVo));
//                        return "调用wms失败";
            return HttpResponse.failure(null, ResultCode.REPEAT_DATA);
        }
        return HttpResponse.success();
    }

    /**
     * 添加是导入sku
     *
     * @param reqVo
     * @return
     */
    @Override
    public List<AllocationItemRespVo> importAllocationSku(AllocationImportSkuReqVo reqVo) {
        AuthToken currentAuthToken = AuthenticationInterceptor.getCurrentAuthToken();
        reqVo.setCompanyCode(currentAuthToken.getCompanyCode());
        try {
            List<AllocationItemRespVo> list = Lists.newArrayList();
            List<Object[]> excel = ExcelUtil.getExcelAll(reqVo.getFile());
            if (excel.size() <= 1) {
                return list;
            }
            List<String> codes = excel.stream().map(p -> {
                return String.valueOf(p[0]);
            }).collect(Collectors.toList());
            if (CollectionUtils.isEmptyCollection(codes)) {
                return Lists.newArrayList();
            }
            //移除表头数据
            codes.remove(0);
            //调用接口获取sku的详细数据
            QueryStockSkuReqVo copy = BeanCopyUtils.copy(reqVo, QueryStockSkuReqVo.class);
            copy.setSkuList(codes);
            List<QueryStockSkuRespVo> queryStockSkuRespVos = stockService.selectStockSkus(copy);
            List<AllocationItemRespVo> data = JSON.parseArray(JsonUtil.toJson(queryStockSkuRespVos), AllocationItemRespVo.class);
            //key为sku编码
            Map<String, AllocationItemRespVo> map = data.stream().collect(Collectors.toMap(AllocationItemRespVo::getSkuCode, Function.identity()));
            SkuBatchReqVO skuBatchReqVO = new SkuBatchReqVO();
            skuBatchReqVO.setSkuCodes(codes);
            skuBatchReqVO.setTransportCenterCode(reqVo.getTransportCenterCode());
            skuBatchReqVO.setWarehouseCode(reqVo.getWarehouseCode());
            List<SkuBatchRespVO> skuBatchRespVOS = stockService.querySkuBatchList(skuBatchReqVO);
            Map<String, List<SkuBatchRespVO>> skuBatchRespVOMap = skuBatchRespVOS.stream().collect(Collectors.groupingBy(SkuBatchRespVO::getSkuCode));
            //错误信息
            for (int i = 1; i < excel.size(); i++) {
                Object[] objects = excel.get(i);
                String skuCode = String.valueOf(objects[0]);
                String skuName = ExcelUtil.convertNumToString(objects[1]);
                Long num = 0L;
                try {
                    String s = ExcelUtil.convertNumToString(objects[3]);
                    if (StringUtils.isNotBlank(s)) {
                        num = Long.valueOf(s);
                    }
                } catch (Exception e) {
                    log.error(Global.ERROR, e);
                    list.add(new AllocationItemRespVo(skuCode, skuName, "数量数据格式错误"));
                    continue;
                }
                //验证是否重复导入
                if (null != skuCode) {
                    List<String> skus = list.stream().map(AllocationItemRespVo::getSkuCode).collect(Collectors.toList());
                    if (skus.contains(skuCode) == true) {
                        {
                            list.add(new AllocationItemRespVo(skuCode, skuName, "导入重复"));
                            continue;
                        }
                    }
                }
                //验证该编码是否存在
                if (Objects.isNull(map.get(skuCode))) {
                    list.add(new AllocationItemRespVo(skuCode, skuName, "sku编码不存在或者已被禁用"));
                    continue;
                }
                AllocationItemRespVo allocationItemRespVo = map.get(skuCode);
                if (num != null && num > allocationItemRespVo.getStockNum()) {
                    list.add(new AllocationItemRespVo(skuCode, skuName, "数量超出库存范围"));
                    continue;
                }
                if (num != null && num <= allocationItemRespVo.getStockNum()) {
                    allocationItemRespVo.setNumber(num);
                    allocationItemRespVo.setTotalPrice(allocationItemRespVo.getPrice() * num);
                }
                if (allocationItemRespVo == null) {
                    list.add(new AllocationItemRespVo(skuCode, skuName, "该sku没有足够的库存"));
                    continue;
                }
                list.add(allocationItemRespVo);
//                //验证是否存在批次存库
//                if(skuBatchRespVOMap.containsKey(skuCode)){
//                    allocationItemRespVo.setSkuBatchRespVOS(skuBatchRespVOMap.get(skuCode));
//                } else {
//                    allocationItemRespVo.setErrorReason("sku在库房中不存在批次号");
//                }


            }
            return list;
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            throw new GroundRuntimeException("导入异常");
        }
    }

    /**
     * 功能描述: 根据formNo获取主键ID
     *
     * @param formNo
     * @return
     * @auther knight.xie
     * @date 2019/8/8 19:38
     */
    @Override
    public Long getIdByFormNo(String formNo) {
        AllocationDTO allocation = allocationMapper.selectByFormNO1(formNo);
        if (null == allocation) {
            throw new BizException(ResultCode.OBJECT_EMPTY_BY_FORMNO);
        }
        return allocation.getId();
    }

    @Override
    public StockBatch getNumberByBatchAndSkuCode(String skuCode, String batchInfoCode) {
        StockBatch stockBatch = allocationMapper.selectNumberByBatchAndSkuCode(skuCode, batchInfoCode);
        return stockBatch;
    }

    @Override
    public BasePage<ManualChoseProductRespVo> getManualChoseProduct(ManualChoseProductReq m) {
        List<ManualChoseProductRespVo> mLists = allocationMapper.getManualChoseProduct(m);
        Long mCount = allocationMapper.getManualChoseProductCount(m);
        // 查询商品的批次信息
        for (ManualChoseProductRespVo mBtachLists : mLists) {
            List<SkuBatchRespVO> skuBatchRespVOS = stockBatchDao.selectStockBatch(mBtachLists.getSkuCode(), mBtachLists.getTransportCenterCode(), mBtachLists.getWarehouseCode());
            mBtachLists.setSkuBatchRespVOS(skuBatchRespVOS);
//            批次管理 0：自动批次管理 1：全部制定批次模式 2：部分指定批次模式
            //  商品sku是否指定批次 (0:指定 1:不指定)
//            if (Objects.equals(Global.BATCH_MANAGE_2, mBtachLists.getBatchManage()) || Objects.equals(Global.BATCH_MANAGE_4, mBtachLists.getBatchManage()) || Objects.equals(Global.BATCH_MANAGE_6, mBtachLists.getBatchManage())) {
//                Integer count = productSkuBatchMapper.productSkuBatchExist(mBtachLists.getSkuCode(), mBtachLists.getWarehouseCode());
//                if (count > 0) {
//                    mBtachLists.setSkuBatchManage(Global.WAREHOUSE_BATCH_MANAGE_SKU_0);
//                } else {
//                    mBtachLists.setSkuBatchManage(Global.WAREHOUSE_BATCH_MANAGE_SKU_1);
//                }
//            }
        }
        BasePage basePage = new BasePage();
        basePage.setDataList(mLists);
        basePage.setTotalCount(mCount);
        return basePage;
    }


}

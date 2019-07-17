package com.aiqin.bms.scmp.api.purchase.service.impl;

import com.aiqin.bms.scmp.api.base.EncodingRuleType;
import com.aiqin.bms.scmp.api.base.PageResData;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.constant.RejectRecordStatus;
import com.aiqin.bms.scmp.api.product.dao.ProductCategoryDao;
import com.aiqin.bms.scmp.api.product.dao.StockDao;
import com.aiqin.bms.scmp.api.product.domain.request.ILockStockBatchItemReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.ILockStockBatchReqVO;
import com.aiqin.bms.scmp.api.product.domain.request.returnsupply.ReturnSupplyToOutBoundReqVo;
import com.aiqin.bms.scmp.api.product.domain.response.ProductCategoryResponse;
import com.aiqin.bms.scmp.api.product.domain.response.QueryStockBatchSkuRespVo;
import com.aiqin.bms.scmp.api.product.service.OutboundService;
import com.aiqin.bms.scmp.api.product.service.StockService;
import com.aiqin.bms.scmp.api.purchase.dao.*;
import com.aiqin.bms.scmp.api.purchase.domain.*;
import com.aiqin.bms.scmp.api.purchase.domain.request.*;
import com.aiqin.bms.scmp.api.purchase.domain.response.*;
import com.aiqin.bms.scmp.api.purchase.service.GoodsRejectService;
import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.dao.logisticscenter.LogisticsCenterDao;
import com.aiqin.bms.scmp.api.supplier.dao.supplier.SupplyCompanyDao;
import com.aiqin.bms.scmp.api.supplier.dao.warehouse.WarehouseDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.EncodingRule;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.LogisticsCenter;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.SupplyCompany;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.Warehouse;
import com.aiqin.bms.scmp.api.supplier.domain.response.purchasegroup.PurchaseGroupVo;
import com.aiqin.bms.scmp.api.supplier.service.PurchaseGroupService;
import com.aiqin.bms.scmp.api.util.FileReaderUtil;
import com.aiqin.ground.util.id.IdUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.Project;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
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
@SuppressWarnings("unchecked")
public class GoodsRejectServiceImpl implements GoodsRejectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsRejectServiceImpl.class);

    private static final String[] importRejectApplyHeaders = new String[]{
            "SKU编号", "SKU名称", "供应商名称", "仓库名称", "库房名称", "商品批次号", "退供类型", "退供数量", "含税单价",
    };
    /**
     * 品类code递增长度
     */
    private static final int categoryAddLength = 3;

    @Resource
    private RejectApplyRecordDao rejectApplyRecordDao;
    @Resource
    private RejectApplyRecordDetailDao rejectApplyRecordDetailDao;
    @Resource
    private RejectRecordDao rejectRecordDao;
    @Resource
    private RejectRecordDetailDao rejectRecordDetailDao;
    @Resource
    private OperationLogDao operationLogDao;
    @Resource
    private EncodingRuleDao encodingRuleDao;
    @Resource
    private OutboundService outboundService;
    @Resource
    private PurchaseGroupService purchaseGroupService;
    @Resource
    private FileRecordDao fileRecordDao;
    @Resource
    private StockDao stockDao;
    @Resource
    private GoodsRejectApprovalServiceImpl goodsRejectApprovalService;
    @Resource
    private StockService stockService;
    @Resource
    private SupplyCompanyDao supplyCompanyDao;
    @Resource
    private LogisticsCenterDao logisticsCenterDao;
    @Resource
    private WarehouseDao warehouseDao;
    @Resource
    private ProductCategoryDao productCategoryDao;

    @Override
    public HttpResponse<PageResData<RejectApplyQueryResponse>> rejectApplyList(RejectApplyQueryRequest rejectApplyQueryRequest) {
        List<PurchaseGroupVo> groupVoList = purchaseGroupService.getPurchaseGroup();
        if (CollectionUtils.isEmpty(groupVoList)) {
            return HttpResponse.success();
        }
        rejectApplyQueryRequest.setGroupList(groupVoList);
        List<RejectApplyQueryResponse> list = rejectApplyRecordDao.list(rejectApplyQueryRequest);
        for (RejectApplyQueryResponse rejectApplyQueryResponse : list) {
            rejectApplyQueryResponse.setUpdateStatus(Global.USER_ON);
            //查询是否有提交过的商品
            Integer statusCount = rejectApplyRecordDetailDao.countByRejectId(rejectApplyQueryResponse.getRejectApplyRecordCode(), Global.USER_ON);
            if (statusCount > 0) {
                rejectApplyQueryResponse.setUpdateStatus(Global.USER_OFF);
            }
        }
        Integer count = rejectApplyRecordDao.listCount(rejectApplyQueryRequest);
        return HttpResponse.successGenerics(new PageResData<>(count, list));
    }

    @Transactional(rollbackFor = Exception.class)
    public HttpResponse rejectApply(RejectApplyHandleRequest rejectApplyQueryRequest) {
        if (rejectApplyQueryRequest == null || CollectionUtils.isEmpty(rejectApplyQueryRequest.getDetailList())) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        try {
            EncodingRule encodingRule = encodingRuleDao.getNumberingType(EncodingRuleType.GOODS_REJECT_APPLY_CODE);
            RejectApplyRecord rejectApplyRecord = new RejectApplyRecord();
            BeanUtils.copyProperties(rejectApplyQueryRequest, rejectApplyRecord);
            String rejectCode = "RAR" + encodingRule.getNumberingValue();
            rejectApplyRecord.setRejectApplyRecordCode(rejectCode);
            //处理数据
            rejectApplyRecord.setApplyRecordStatus(0);
            Integer count = this.rejectApplyData(rejectApplyQueryRequest, rejectApplyRecord, rejectCode);
            //sku数量等于商品列表条数
            rejectApplyRecord.setSumSku(count);
            Integer counts = rejectApplyRecordDao.insert(rejectApplyRecord);
            LOGGER.info("添加退供申请影响条数:{}", counts);
            //更新编码
            encodingRuleDao.updateNumberValue(encodingRule.getNumberingValue(), encodingRule.getId());
        } catch (Exception e) {
            LOGGER.error("添加退供申请单异常:{}", e);
            throw new RuntimeException(String.format("添加退供申请单异常:{%s}", e.getMessage()));
        }
        return HttpResponse.success();
    }

    /**
     * 处理退供申请单数据
     */
    private Integer rejectApplyData(RejectApplyHandleRequest rejectApplyQueryRequest, RejectApplyRecord rejectApplyRecord, String rejectCode) {
        //总退供数量
        Integer sumCount = 0;
        //总退供金额
        Long sumAmount = 0L;
        for (RejectApplyDetailHandleRequest detail : rejectApplyQueryRequest.getDetailList()) {
            //详情的id
            detail.setRejectApplyRecordDetailId(IdUtil.uuid());
            detail.setRejectApplyRecordCode(rejectCode);
            detail.setApplyRecordStatus(rejectApplyRecord.getApplyRecordStatus());
            //单品数量等于数量
            detail.setSingleCount(detail.getProductCount());
            detail.setCreateById(rejectApplyQueryRequest.getCreateById());
            detail.setCreateByName(rejectApplyQueryRequest.getCreateByName());
            detail.setUpdateById(rejectApplyRecord.getUpdateById());
            detail.setUpdateByName(rejectApplyRecord.getUpdateByName());
            detail.setProductTotalAmount(detail.getProductAmount() * detail.getProductCount());
            detail.setApplyType(rejectApplyQueryRequest.getApplyType());
            sumAmount += detail.getProductTotalAmount();
            sumCount += detail.getProductCount();
        }
        rejectApplyRecord.setSumAmount(sumAmount);
        rejectApplyRecord.setSumCount(sumCount);
        //添加详情
        Integer detailCount = rejectApplyRecordDetailDao.insertAll(rejectApplyQueryRequest.getDetailList());
        LOGGER.info("添加退供申请详情影响条数:{}", detailCount);
        return detailCount;
    }

    @Transactional(rollbackFor = Exception.class)
    public HttpResponse updateRejectApply(RejectApplyHandleRequest rejectApplyRequest) {
        try {
            RejectApplyRecord rejectApplyRecord = rejectApplyRecordDao.selectByRejectCode(rejectApplyRequest.getRejectApplyRecordCode());
            if (rejectApplyRecord == null) {
                LOGGER.error("未查询到退供申请单信息:{}", rejectApplyRequest.getRejectApplyRecordCode());
                return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_APPLY_RECORD);
            }
            rejectApplyRecordDetailDao.deleteAll(rejectApplyRecord.getRejectApplyRecordCode());
            //处理数据
            this.rejectApplyData(rejectApplyRequest, rejectApplyRecord, rejectApplyRecord.getRejectApplyRecordCode());
            //sku数量等于商品列表条数
            rejectApplyRecord.setSumSku(rejectApplyRequest.getDetailList().size());
            rejectApplyRecord.setUpdateById(rejectApplyRecord.getUpdateById());
            rejectApplyRecord.setUpdateByName(rejectApplyRecord.getUpdateByName());
            //更新退供申请单信息
            Integer count = rejectApplyRecordDao.updateByRejectCode(rejectApplyRecord);
            LOGGER.info("修改退供申请详情影响条数:{}", count);
        } catch (Exception e) {
            LOGGER.error("修改退供申请详情影响条数:{}", e.getMessage());
            throw new RuntimeException(String.format("修改退供申请单异常:{%s}", e.getMessage()));
        }
        return HttpResponse.success();
    }

    @Override
    public HttpResponse<RejectApplyHandleResponse> selectRejectApply(String rejectApplyCode) {
        RejectApplyRecord rejectApplyRecord = rejectApplyRecordDao.selectByRejectCode(rejectApplyCode);
        if (rejectApplyRecord == null) {
            LOGGER.error("未查询到退供申请单信息:{}", rejectApplyCode);
            return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_APPLY_RECORD);
        }
        RejectApplyHandleResponse response = new RejectApplyHandleResponse();
        BeanUtils.copyProperties(rejectApplyRecord, response);
        List<RejectApplyDetailHandleResponse> list = rejectApplyRecordDetailDao.selectHandleByRejectCode(rejectApplyCode);
        response.setDetailList(list);
        return HttpResponse.successGenerics(response);
    }

    @Override
    public HttpResponse<PageResData<RejectImportResponse>> rejectApplyImport(MultipartFile file, String purchaseGroupCode) {
        if (file == null) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        try {
            String[][] result = FileReaderUtil.readExcel(file, importRejectApplyHeaders.length);
            if (result.length < 2) {
                return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
            }
            List<RejectImportResponse> list = new ArrayList<>();
            Integer errorCount = 0;
            String validResult = FileReaderUtil.validStoreValue(result, importRejectApplyHeaders);
            if (StringUtils.isNotBlank(validResult)) {
                return HttpResponse.failure(MessageId.create(Project.SCMP_API, 88888, validResult));
            }
            String[] record;
            SupplyCompany supplier;
            Warehouse warehouse;
            LogisticsCenter logisticsCenter;
            RejectImportResponse response;
            RejectApplyDetailHandleResponse rejectApplyDetailHandleResponse;
            for (int i = 1; i <= result.length - 1; i++) {
                record = result[i];
                response = new RejectImportResponse();
                if (StringUtils.isBlank(record[0]) || StringUtils.isBlank(record[1]) || StringUtils.isBlank(record[2]) || StringUtils.isBlank(record[4]) || StringUtils.isBlank(record[3])) {
                    HandleResponse(response, record, "导入的数据不全");
                    errorCount++;
                    list.add(response);
                    continue;
                }
                supplier = supplyCompanyDao.selectBySupplierName(record[2]);
                if (supplier == null) {
                    HandleResponse(response, record, "未查询到供应商信息");
                    errorCount++;
                    list.add(response);
                    continue;
                }
                logisticsCenter = logisticsCenterDao.selectByCenterName(record[3]);
                if (logisticsCenter == null) {
                    HandleResponse(response, record, "未查询到仓库信息");
                    errorCount++;
                    list.add(response);
                    continue;
                }
                warehouse = warehouseDao.selectByWarehouseName(record[4]);
                if (warehouse == null) {
                    HandleResponse(response, record, "未查询到库房.信息");
                    errorCount++;
                    list.add(response);
                    continue;
                }
                rejectApplyDetailHandleResponse = stockDao.rejectProductInfo(purchaseGroupCode, logisticsCenter.getLogisticsCenterCode(), warehouse.getWarehouseCode(), record[0]);
                if (rejectApplyDetailHandleResponse != null) {
                    BeanUtils.copyProperties(rejectApplyDetailHandleResponse, response);
                    response.setProductCount(new Double(record[7]).intValue());
                    response.setProductAmount(Long.valueOf(record[8]));
                    response.setProductTotalAmount(Long.valueOf(record[8]) * Long.valueOf(record[7]));
                    if (rejectApplyDetailHandleResponse.getStockCount() < Integer.valueOf(record[7])) {
                        response.setErrorReason("可用库存数量小于销售数量");
                        errorCount++;
                    }
                } else {
                    HandleResponse(response, record, "未查询到对应的商品");
                    errorCount++;
                }
                list.add(response);
            }
            return HttpResponse.successGenerics(new PageResData<>(errorCount, list));
        } catch (Exception e) {
            LOGGER.error("退供申请单导入异常:{}", e.getMessage());
            return HttpResponse.failure(ResultCode.IMPORT_REJECT_APPLY_ERROR);
        }
    }

    private void HandleResponse(RejectImportResponse response, String[] record, String errorReason) {
        response.setSkuCode(record[0]);
        response.setSkuName(record[1]);
        response.setSupplierCode(record[2]);
        response.setTransportCenterCode(record[3]);
        response.setWarehouseCode(record[4]);
        response.setBatchNo(record[5]);
        response.setProductType(Integer.valueOf(record[6]));
        response.setProductCount(Integer.valueOf(record[7]));
        response.setProductAmount(Long.valueOf(record[8]));
        response.setErrorReason(errorReason);
    }

    @Override
    public HttpResponse<PageResData<RejectApplyRecordDetail>> rejectApplyDetailInfo(RejectApplyRequest response) {
        List<RejectApplyRecordDetail> detailList = rejectApplyRecordDetailDao.listByConditionPage(response.getSupplierCode(), response.getPurchaseGroupCode(), response.getSettlementMethodCode(),
                response.getTransportCenterCode(), response.getWarehouseCode(), response.getRejectApplyRecordCodes(), response.getPageSize(), response.getBeginIndex());
        QueryStockBatchSkuRespVo queryStockBatchSkuRespVo;
        for (RejectApplyRecordDetail detailResponse : detailList) {
            queryStockBatchSkuRespVo = stockDao.selectSkuBatchCode(detailResponse.getPurchaseGroupCode(), detailResponse.getTransportCenterCode(), detailResponse.getWarehouseCode(), detailResponse.getSkuCode(), detailResponse.getBarcode());
            if (queryStockBatchSkuRespVo != null) {
                detailResponse.setStockCount(queryStockBatchSkuRespVo.getAvailableNum().intValue());
            }
        }
        Integer listCount = rejectApplyRecordDetailDao.listByConditionPageCount(response.getSupplierCode(), response.getPurchaseGroupCode(), response.getSettlementMethodCode(),
                response.getTransportCenterCode(), response.getWarehouseCode(), response.getRejectApplyRecordCodes());
        return HttpResponse.successGenerics(new PageResData<>(listCount, detailList));
    }

    @Override
    public HttpResponse<List<RejectApplyResponse>> rejectApplyInfo(RejectApplyRequest request) {
        if (CollectionUtils.isEmpty(request.getRejectApplyRecordCodes())) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        //退供申请单分组后的列表
        List<RejectApplyResponse> list = rejectApplyRecordDetailDao.listForRejectRecord(request);
        SupplyCompany company;
        Long returnAmount;
        for (RejectApplyResponse response : list) {
            company = rejectApplyRecordDetailDao.selectSupplyCompany(response.getSupplierCode());
            //查询实物返金额
            returnAmount = rejectApplyRecordDetailDao.selectReturnAmount(request.getRejectApplyRecordCodes(), response.getSupplierCode(), response.getWarehouseCode(), response.getTransportCenterCode(), response.getPurchaseGroupCode(), response.getSettlementMethodCode());
            response.setReturnAmount(returnAmount);
            if (company != null) {
                response.setCityId(company.getCityId());
                response.setAddress(company.getAddress());
                response.setCityName(company.getCityName());
                response.setProvinceId(company.getProvinceId());
                response.setProvinceName(company.getProvinceName());
                response.setDistrictId(company.getDistrictId());
                response.setDistrictName(company.getDistrictName());
            } else {
                response.setCityId("");
                response.setAddress("");
                response.setCityName("");
                response.setProvinceId("");
                response.setProvinceName("");
                response.setDistrictId("");
                response.setDistrictName("");
            }
        }
        return HttpResponse.successGenerics(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public HttpResponse addReject(RejectRequest request) {
        try {
            EncodingRule encodingRule = encodingRuleDao.getNumberingType(EncodingRuleType.GOODS_REJECT_APPLY_CODE);
            RejectRecord rejectRecord = new RejectRecord();
            BeanUtils.copyProperties(request, rejectRecord);
            String rejectCode = "RR" + encodingRule.getNumberingValue();
            String rejectId = IdUtil.rejectRecordId();
            rejectRecord.setRejectRecordCode(rejectCode);
            rejectRecord.setRejectRecordId(rejectId);
            rejectRecord.setExpectTime(new DateTime(request.getExpectTime()).toDate());
            rejectRecord.setValidDay(new DateTime(request.getValidDay()).toDate());
            //待审核状态
            rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_AUDIT);
            //总退供数量
            Integer sumCount = 0;
            //含税金额
            Long sumAmount = 0L;
            //总单品数量
            Integer sumSingleCount = 0;
            //未税退供金额
            Long untaxedAmount = 0L;
            //总实物返回数量
            Integer returnCount = 0;
            //总实物返回金额
            Long returnAmount = 0L;
            List<RejectApplyRecordDetail> detailList = rejectApplyRecordDetailDao.listByCondition(request.getSupplierCode(), request.getPurchaseGroupCode(), request.getSettlementMethodCode(),
                    request.getTransportCenterCode(), request.getWarehouseCode(), request.getRejectApplyRecordCodes());
            List<RejectRecordDetail> list = new ArrayList<>();
            RejectRecordDetail rejectRecordDetail;
            for (RejectApplyRecordDetail detailResponse : detailList) {
                //计算总数
                sumCount += detailResponse.getProductCount();
                sumSingleCount += detailResponse.getProductCount();
                sumAmount += detailResponse.getProductTotalAmount();
                untaxedAmount += detailResponse.getProductTotalAmount();
                if (detailResponse.getProductType().equals(2)) {
                    returnCount += detailResponse.getProductCount();
                    returnAmount += detailResponse.getProductTotalAmount();
                }
                rejectRecordDetail = new RejectRecordDetail();
                BeanUtils.copyProperties(detailResponse, rejectRecordDetail);
                rejectRecordDetail.setProductCount(Long.valueOf(detailResponse.getProductCount()));
                rejectRecordDetail.setRejectRecordDetailId(IdUtil.uuid());
                list.add(rejectRecordDetail);
            }
            rejectRecord.setSumAmount(sumAmount);
            rejectRecord.setSumCount(sumCount);
            rejectRecord.setUntaxedAmount(untaxedAmount);
            rejectRecord.setSingleCount(sumSingleCount);
            rejectRecord.setReturnAmount(returnAmount);
            rejectRecord.setReturnCount(returnCount);
            //添加退供单记录
            Integer count = rejectRecordDao.insert(rejectRecord);
            LOGGER.info("添加退供影响条数:{}", count);
            //添加退供单详情
            Integer detailCount = rejectRecordDetailDao.insertAll(list, rejectId, rejectCode, request.getCreateById(), request.getCreateByName());
            LOGGER.info("添加退供详情影响条数:{}", detailCount);
            //更改退供申请详情部分记录(reject_apply_record_detail)更改为已提交
            List<String> detailIds = detailList.stream().map(RejectApplyRecordDetail::getRejectApplyRecordDetailId).collect(Collectors.toList());
            Integer updateCount = rejectApplyRecordDetailDao.updateByDetailIds(detailIds);
            LOGGER.info("更改退供申请详情影响条数:{}", updateCount);
            //更新主表的状态 当详情表的状态都为已提交
            for (String id : request.getRejectApplyRecordCodes()) {
                Integer statusCount = rejectApplyRecordDetailDao.countByRejectId(id, Global.USER_OFF);
                LOGGER.info("申请单详情未提交的条数:{}", statusCount);
                if (statusCount == 0) {
                    LOGGER.info("更新申请表主表的状态");
                    rejectApplyRecordDao.updateStatus(id);
                }
            }
            //更新编码
            encodingRuleDao.updateNumberValue(encodingRule.getNumberingValue(), encodingRule.getId());
            //保存上传的文件
            if (CollectionUtils.isNotEmpty(request.getFileList())) {
                Integer fileCount = fileRecordDao.insertAll(rejectId, request.getFileList());
                LOGGER.info("上传文件条数:{}", fileCount);
            }
            //增加操作记录 操作状态  : 0 新增 1 修改 2 下载
            operationLogDao.insert(new OperationLog(rejectId, 0, "新增退供单", "", request.getCreateById(), request.getCreateByName()));
            //锁定库存
//            ILockStockBatchReqVO iLockStockBatchReqVO = handleStockParam(list, rejectRecord);
//            Boolean stockStatus = stockService.returnSupplyLockStockBatch(iLockStockBatchReqVO);
//            if (!stockStatus) {
//                LOGGER.error("锁定库存异常:{}", rejectRecord.toString());
//                throw new RuntimeException(String.format("锁定库存异常:{%s}", rejectRecord.toString()));
//            }
            //提交退供审批
            goodsRejectApprovalService.workFlow(rejectCode, request.getCreateByName(), request.getDictionaryId());
        } catch (BeansException e) {
            LOGGER.error("新增退供单异常:{}", e.getMessage());
            throw new RuntimeException(String.format("新增退供单异常:{%s}", e.getMessage()));
        }
        return HttpResponse.success();
    }

    public ILockStockBatchReqVO handleStockParam(List<RejectRecordDetail> detailList, RejectRecord rejectRecord) {
        ILockStockBatchReqVO iLockStockBatchReqVO = new ILockStockBatchReqVO();
        iLockStockBatchReqVO.setCompanyCode(rejectRecord.getCompanyCode());
        iLockStockBatchReqVO.setTransportCenterCode(rejectRecord.getTransportCenterCode());
        iLockStockBatchReqVO.setWarehouseCode(rejectRecord.getWarehouseCode());
        iLockStockBatchReqVO.setPurchaseGroupCode(rejectRecord.getPurchaseGroupCode());
        List<ILockStockBatchItemReqVo> list = new ArrayList<>();
        ILockStockBatchItemReqVo itemReqVo;
        for (RejectRecordDetail detail : detailList) {
            itemReqVo = new ILockStockBatchItemReqVo();
            itemReqVo.setNum(Long.valueOf(detail.getSingleCount()));
            itemReqVo.setSkuCode(detail.getSkuCode());
            itemReqVo.setSkuName(detail.getSkuName());
            itemReqVo.setBatchCode(detail.getBatchNo());
            itemReqVo.setDocumentType(2);
            itemReqVo.setDocumentNum(detail.getRejectRecordCode());
            itemReqVo.setRemark(rejectRecord.getRemark());
            itemReqVo.setUpdateByName(rejectRecord.getCreateByName());
            itemReqVo.setUpdateByCode(rejectRecord.getCreateById());
            list.add(itemReqVo);
        }
        iLockStockBatchReqVO.setItemReqVos(list);
        return iLockStockBatchReqVO;
    }


    @Override
    public HttpResponse updateReject(String rejectApplyCode) {
        RejectApplyRecord rejectApplyRecord = rejectApplyRecordDao.selectByRejectCode(rejectApplyCode);
        if (rejectApplyRecord == null) {
            LOGGER.error("未查询到退供申请单信息:{}", rejectApplyCode);
            return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_APPLY_RECORD);
        }
        rejectApplyRecordDao.updateStatus(rejectApplyCode);
        rejectApplyRecordDetailDao.updateStatus(rejectApplyCode);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse<PageResData<RejectRecord>> rejectList(RejectQueryRequest rejectApplyQueryRequest) {
        List<PurchaseGroupVo> groupVoList = purchaseGroupService.getPurchaseGroup();
        if (CollectionUtils.isEmpty(groupVoList)) {
            return HttpResponse.successGenerics(new PageResData<>());
        }
        rejectApplyQueryRequest.setGroupList(groupVoList);
        List<RejectRecord> list = rejectRecordDao.list(rejectApplyQueryRequest);
        Integer count = rejectRecordDao.listCount(rejectApplyQueryRequest);
        return HttpResponse.successGenerics(new PageResData<>(count, list));
    }

    @Transactional
    public HttpResponse rejectSupplier(String rejectRecordId) {
        try {
            RejectRecord rejectRecord = rejectRecordDao.selectByRejectId(rejectRecordId);
            if (rejectRecord == null) {
                return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_RECORD);
            }
            RejectRecord request = new RejectRecord();
            request.setRejectRecordId(rejectRecordId);
            request.setRejectStatus(RejectRecordStatus.REJECT_STATUS_STOCK);
            Integer count = rejectRecordDao.updateStatus(request);
            LOGGER.info("供应商确认-更改退供申请详情影响条数:{}", count);
            List<RejectRecordDetail> list = rejectRecordDetailDao.selectByRejectId(rejectRecordId);
            ReturnSupplyToOutBoundReqVo reqVo = new ReturnSupplyToOutBoundReqVo();
            reqVo.setRejectRecord(rejectRecord);
            reqVo.setRejectRecordDetails(list);
            LOGGER.info("调用退供出库:{}", request);
            outboundService.returnSupplySave(reqVo);
            return HttpResponse.success();
        } catch (Exception e) {
            LOGGER.error("更新退供单异常:{}", e.getMessage());
            throw new RuntimeException(String.format("更新退供单异常:{%s}", e.getMessage()));
        }
    }

    @Override
    public HttpResponse rejectTransport(RejectRecord rejectRecord) {
        RejectRecord record = rejectRecordDao.selectByRejectId(rejectRecord.getRejectRecordId());
        if (record == null) {
            return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_RECORD);
        }
        rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_TRANSPORTED);
        Integer count = rejectRecordDao.updateStatus(rejectRecord);
        LOGGER.info("退供发运-更改退供申请详情影响条数:{}", count);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse rejectTransportFinish(String rejectRecordId) {
        RejectRecord record = rejectRecordDao.selectByRejectId(rejectRecordId);
        if (record == null) {
            return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_RECORD);
        }
        RejectRecord rejectRecord = new RejectRecord();
        rejectRecord.setRejectRecordId(rejectRecordId);
        rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_FINISH);
        Integer count = rejectRecordDao.updateStatus(rejectRecord);
        LOGGER.info("退供完成-更改退供申请详情影响条数:{}", count);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse<RejectResponse> rejectInfo(String rejectRecordCode) {
        List<PurchaseGroupVo> groupVoList = purchaseGroupService.getPurchaseGroup();
        if (CollectionUtils.isEmpty(groupVoList)) {
            return HttpResponse.success();
        }
        List<String> groupIdList = groupVoList.stream().map(PurchaseGroupVo::getPurchaseGroupCode).collect(Collectors.toList());
        RejectResponse rejectResponse = new RejectResponse();
        RejectRecord rejectRecord = rejectRecordDao.selectByRejectCode(rejectRecordCode);
        if (rejectRecord == null) {
            return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_RECORD);
        } else if (!groupIdList.contains(rejectRecord.getPurchaseGroupCode())) {
            //没有权限查询详情
            return HttpResponse.failure(ResultCode.JURISDICTION_ERROR);
        }
        BeanUtils.copyProperties(rejectRecord, rejectResponse);
        List<RejectRecordDetail> batchList = rejectRecordDetailDao.selectByRejectId(rejectRecord.getRejectRecordId());
        List<RejectRecordDetailResponse> productList = rejectRecordDetailDao.selectProductByRejectId(rejectRecord.getRejectRecordId());
        List<FileRecord> fileList = fileRecordDao.fileList(rejectRecord.getRejectRecordId());
        List<OperationLog> operationLogList = operationLogDao.list(rejectRecord.getRejectRecordId());
        rejectResponse.setLogList(operationLogList);
        rejectResponse.setBatchList(batchList);
        rejectResponse.setProductList(productList);
        rejectResponse.setFileList(fileList);
        return HttpResponse.successGenerics(rejectResponse);
    }

    /**
     * 出库完成,更新退供单 出库时间 退供单状态
     */
    @Transactional
    public void finishStock(RejectStockRequest request) {
        try {
            LOGGER.info("出库完成,更新退供单信息:{}", request.toString());
            RejectRecord rejectRecord = new RejectRecord();
            rejectRecord.setRejectRecordCode(request.getRejectRecordCode());
            rejectRecord.setOutStockTime(request.getOutStockTime());
            rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_STOCKED);
            Integer count = rejectRecordDao.updateStatus(rejectRecord);
            LOGGER.info("更新退供单信息影响条数:{}", count);
            if (CollectionUtils.isNotEmpty(request.getDetailList())) {
                for (RejectDetailStockRequest detailResponse : request.getDetailList()) {
                    rejectRecordDetailDao.updateByDetailId(detailResponse);
                }
            }
        } catch (Exception e) {
            LOGGER.error("更新退供单异常:{}", e.getMessage());
            throw new RuntimeException(String.format("更新退供单异常:{%s}", e.getMessage()));
        }

    }

    public HttpResponse rejectCancel(String rejectRecordId) {
        try {
            RejectRecord record = rejectRecordDao.selectByRejectId(rejectRecordId);
            if (record == null) {
                return HttpResponse.failure(ResultCode.NOT_HAVE_REJECT_RECORD);
            }
            RejectRecord rejectRecord = new RejectRecord();
            rejectRecord.setRejectRecordId(rejectRecordId);
            rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_CANCEL);
            Integer count = rejectRecordDao.updateStatus(rejectRecord);
            LOGGER.info("取消-更改退供申请详情影响条数:{}", count);
            List<RejectRecordDetail> list = rejectRecordDetailDao.selectByRejectId(rejectRecordId);
            //解锁库存
            ILockStockBatchReqVO iLockStockBatchReqVO = handleStockParam(list, record);
            Boolean stockStatus = stockService.returnSupplyUnLockStockBatch(iLockStockBatchReqVO);
            if (!stockStatus) {
                LOGGER.error("解锁库存异常:{}", rejectRecord.toString());
                throw new RuntimeException(String.format("解锁库存异常:{%s}", rejectRecord.toString()));
            }
            return HttpResponse.success();
        } catch (RuntimeException e) {
            LOGGER.error("取消-更改退供申请异常:{}", e.getMessage());
            throw new RuntimeException(String.format("取消-更改退供申请异常:{%s}", e.getMessage()));
        }
    }

    @Override
    public HttpResponse<PageResData<RejectApplyDetailHandleResponse>> rejectStockProduct(RejectProductRequest rejectQueryRequest) {
        List<RejectApplyDetailHandleResponse> list = stockDao.rejectProductList(rejectQueryRequest);
        for (RejectApplyDetailHandleResponse response : list) {
            response.setCategoryName(selectCategoryName(response.getCategoryId()));
        }
        Integer count = stockDao.rejectProductListCount(rejectQueryRequest);
        return HttpResponse.successGenerics(new PageResData<>(count, list));
    }

    /**
     *  根据品类code 查询所有的名称(包含父级)
     */
    public String selectCategoryName(String categoryCode) {
        StringBuilder stringBuilder = new StringBuilder();
        ProductCategoryResponse productCategoryResponse;
        if (StringUtils.isNotBlank(categoryCode)) {
            int s = categoryCode.length() / categoryAddLength;
            for (int i = 0; i < s; i++) {
                productCategoryResponse = productCategoryDao.selectCategoryLevelByCategoryId(categoryCode.substring(0, (i + 1) * 3));
                if (productCategoryResponse != null) {
                    stringBuilder.append(productCategoryResponse.getCategoryName());
                    if (i < s - 1) {
                        stringBuilder.append("/");
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

}

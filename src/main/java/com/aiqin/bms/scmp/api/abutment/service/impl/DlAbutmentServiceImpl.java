package com.aiqin.bms.scmp.api.abutment.service.impl;

import com.aiqin.bms.scmp.api.abutment.dao.DlOrderBillDao;
import com.aiqin.bms.scmp.api.abutment.dao.DlOtherInfoDao;
import com.aiqin.bms.scmp.api.abutment.dao.DlStoreInfoDao;
import com.aiqin.bms.scmp.api.abutment.domain.DlOrderBill;
import com.aiqin.bms.scmp.api.abutment.domain.DlOtherInfo;
import com.aiqin.bms.scmp.api.abutment.domain.DlStoreInfo;
import com.aiqin.bms.scmp.api.abutment.domain.request.dl.*;
import com.aiqin.bms.scmp.api.abutment.domain.request.product.ProductInfoRequest;
import com.aiqin.bms.scmp.api.abutment.domain.request.product.ProductInspectionRequest;
import com.aiqin.bms.scmp.api.abutment.domain.response.DLResponse;
import com.aiqin.bms.scmp.api.abutment.service.DlAbutmentService;
import com.aiqin.bms.scmp.api.abutment.service.ParameterAssemblyService;
import com.aiqin.bms.scmp.api.base.OrderStatus;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.base.ReturnOrderStatus;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.dao.ProductSkuInspReportDao;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.order.OrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfo;
import com.aiqin.bms.scmp.api.purchase.mapper.OrderInfoMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoMapper;
import com.aiqin.bms.scmp.api.purchase.service.OrderService;
import com.aiqin.bms.scmp.api.purchase.service.ReturnGoodsService;
import com.aiqin.bms.scmp.api.supplier.dao.warehouse.WarehouseDao;
import com.aiqin.bms.scmp.api.supplier.domain.request.warehouse.dto.WarehouseDTO;
import com.aiqin.bms.scmp.api.util.DLHttpClientUtil;
import com.aiqin.ground.util.id.IdUtil;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.Project;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DlAbutmentServiceImpl implements DlAbutmentService {

    private static Logger LOGGER = LoggerFactory.getLogger(DlAbutmentServiceImpl.class);

    @Value("${dl.url}")
    private String DL_URL;
    @Resource
    private DlStoreInfoDao dlStoreInfoDao;
    @Resource
    private DlOtherInfoDao dlOtherInfoDao;
    @Resource
    private DlOrderBillDao dlOrderBillDao;
    @Resource
    @Lazy
    private ParameterAssemblyService parameterAssemblyService;
    @Resource
    private WarehouseDao warehouseDao;
    @Autowired
    private DLHttpClientUtil dlHttpClientUtil;
    @Resource
    private OrderService orderService;
    @Resource
    private ReturnGoodsService returnGoodsService;
    @Resource
    private ProductSkuInspReportDao productSkuInspReportDao;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private ReturnOrderInfoMapper returnOrderInfoMapper;

    @Override
    public HttpResponse orderInfo(OrderInfoRequest request){
        if(null == request){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("DL->熙耘，销售单参数：{}", JsonUtil.toJson(request));
        // 保存DL推送熙耘销售单信息日志
        DlOrderBill info = new DlOrderBill();
        info.setDocumentCode(request.getOrderCode());
        info.setDocumentType(Global.ORDER_TYPE);
        info.setBusinessType(Global.PUSH_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        // 查询单据日志是否存在
        DlOrderBill dlOrderBill = dlOrderBillDao.selectByCode(info);
        if(dlOrderBill == null){
            info.setResponseCount(0);
            Integer logCount = dlOrderBillDao.insert(info);
            LOGGER.info("DL->熙耘，保存销售单日志：{}, 销售单号：{}", logCount, request.getOrderCode());
        }else {
            info.setResponseCount(dlOrderBill.getResponseCount() + 1);
            Integer logCount = dlOrderBillDao.update(info);
            LOGGER.info("DL->熙耘，编辑销售单日志：{}", logCount);
        }

        // 转换生成调用耘链销售单的参数
        parameterAssemblyService.orderInfoParameter(request, info);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse returnInfo(ReturnOrderInfoRequest request){
        if(null == request){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }

        if(request.getBusinessForm() == null){
            LOGGER.info("DL推送退货单业务形式不能为空：{}", request.getReturnOrderCode());
            return HttpResponse.failure(MessageId.create(Project.SCMP_API, 500, "退货单业务形式不能为空"));
        }
        // 拼接退货单号
        String returnCode = request.getReturnOrderCode() + "-" + request.getBusinessForm();

        LOGGER.info("DL->熙耘，退货出库单参数：{}", JsonUtil.toJson(request));
        DlOrderBill info = new DlOrderBill();
        info.setDocumentCode(returnCode);
        if(request.getBusinessForm() != null && request.getBusinessForm() == 7){
            info.setDocumentType(Global.RETURN_INFO_TRANSPORT_TYPE);
        }else {
            info.setDocumentType(Global.RETURN_INFO_TYPE);
        }
        info.setBusinessType(Global.PUSH_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        DlOrderBill dlOrderBill = dlOrderBillDao.selectByCode(info);
        if(dlOrderBill == null){
            info.setResponseCount(0);
            Integer logCount = dlOrderBillDao.insert(info);
            LOGGER.info("DL->熙耘，保存退货出库单日志：{}, 退货单号：{}", logCount, request.getOrderCode());
        }else {
            info.setResponseCount(dlOrderBill.getResponseCount() + 1);
            Integer logCount = dlOrderBillDao.update(info);
            LOGGER.info("DL->熙耘，编辑退货出库单日志：{}", logCount);
        }

        request.setReturnOrderCode(returnCode);
        parameterAssemblyService.returnInfoParameter(request, info);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse orderTransport(OrderTransportRequest request) {
        if (null == request) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("熙耘->DL，销售物流单参数：{}", JsonUtil.toJson(request));
        // 保存DL推送熙耘销售单信息日志
        DlOrderBill info = new DlOrderBill();
        info.setDocumentCode(request.getTransportCode());
        info.setDocumentType(Global.ORDER_TRANSPORT_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        DlOrderBill dlOrderBill = dlOrderBillDao.selectByCode(info);
        if (dlOrderBill == null) {
            info.setResponseCount(0);
            Integer logCount = dlOrderBillDao.insert(info);
            LOGGER.info("熙耘->DL，保存销售物流单日志：{}, 物流单号:{}", logCount, request.getTransportCode());
        } else {
            info.setResponseCount(dlOrderBill.getResponseCount() + 1);
            Integer logCount = dlOrderBillDao.update(info);
            LOGGER.info("熙耘->DL，编辑销售物流单日志：{}", logCount);
        }
        // 转换库房编码
        if (request.getTransportCenterCode() != null) {
            WarehouseDTO warehouse = warehouseDao.getWarehouseByCode(request.getTransportCenterCode());
            request.setTransportCenterCode(warehouse.getWmsWarehouseId());
        }

        // 调用Dl 回传DL物流单
        parameterAssemblyService.orderTransportParameter(request, info);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse echoOrderInfo(EchoOrderRequest request) {
        if (null == request) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("熙耘->DL，回传单据参数：{}", JsonUtil.toJson(request));
        // 保存DL推送熙耘销售单信息日志
        DlOrderBill info = new DlOrderBill();
        info.setDocumentCode(request.getOrderCode());
        String dlUrl = "";
        if (request.getOperationType() == 3) {
            info.setDocumentType(Global.ORDER_TYPE);
            dlUrl = "/back/subOrder";
        } else if (request.getOperationType() == 4) {
            // 截取退货单号
            String returnCode = request.getOrderCode();
            boolean contains = returnCode.contains("-");
            if(contains){
                String str = returnCode.substring(0, returnCode.indexOf("-"));
                request.setOrderCode(str);
            }
            info.setDocumentType(Global.RETURN_INFO_TYPE);
            dlUrl = "/back/orderReturn";
        } else {
            return null;
        }
        info.setBusinessType(Global.ECHO_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        DlOrderBill dlOrderBill = dlOrderBillDao.selectByCode(info);
        if (dlOrderBill == null) {
            info.setResponseCount(0);
            Integer logCount = dlOrderBillDao.insert(info);
            LOGGER.info("熙耘->DL，保存回传单据日志：{}, 回传单据单号：{}", logCount, request.getOrderCode());
        } else {
            info.setResponseCount(dlOrderBill.getResponseCount() + 1);
            Integer logCount = dlOrderBillDao.update(info);
            LOGGER.info("熙耘->DL，编辑回传单据日志：{}", logCount);
        }
        // 调用Dl 回传DL单据
        String url = DL_URL + dlUrl;
        parameterAssemblyService.echoOrderInfoParameter(request, info, url);
        return HttpResponse.success();
    }


    @Override
    @Async("myTaskAsyncPool")
    public HttpResponse stockChange(StockChangeRequest request) {
        if (null == request) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("熙耘->DL，库存变更参数：{}", JsonUtil.toJson(request));
        // 保存熙耘推送DL库存变更日志
        DlOtherInfo info = new DlOtherInfo();
        info.setDocumentCode(request.getOrderCode());
        info.setDocumentType(Global.STOCK_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
        info.setStockType(request.getOrderType());
        info.setDocumentContent(JsonUtil.toJson(request));
        Integer logCount = dlOtherInfoDao.insert(info);
        LOGGER.info("熙耘->DL，保存库存变更日志：{}", logCount);

        // 库房转换
        WarehouseDTO warehouse = warehouseDao.getWarehouseByCode(request.getWarehouseCode());
        if (warehouse == null) {
            LOGGER.info("熙耘->DL，库存变更库房转换未查询到对应的库房信息：{}", JsonUtil.toJson(request));
            return null;
        }
        request.setWarehouseCode(warehouse.getWmsWarehouseId());
        request.setWarehouseName(warehouse.getWmsWarehouseName());
        request.setWmsWarehouseType(warehouse.getWmsWarehouseType());
        LOGGER.info("转换库存变动，dl日志：{}", JsonUtil.toJson(request));
        // 调用Dl 回传DL库存变更
        parameterAssemblyService.stockChangeParameter(request, info);
        return HttpResponse.success();

    }

    @Override
    public HttpResponse storeInfo(DlStoreInfo request){
        if(null == request){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("DL->熙耘，门店信息参数：{}", JsonUtil.toJson(request));
        // 保存DL推送熙耘门店信息日志
        DlOtherInfo info = new DlOtherInfo();
        info.setDocumentCode(request.getStoreCode());
        info.setDocumentType(Global.STORE_TYPE);
        info.setBusinessType(Global.PUSH_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        Integer logCount = dlOtherInfoDao.insert(info);
        LOGGER.info("DL->熙耘，保存门店日志信息日志：{}", logCount);
        // 查询门店的信息
        DlStoreInfo storeInfo = dlStoreInfoDao.selectStoreInfo(request.getStoreCode());
        if(storeInfo == null){
            // 保存门店信息
            Integer count = dlStoreInfoDao.insert(request);
            LOGGER.info("保存DL->熙耘门店信息：{}", count);
        }else {
            //  修改门店信息
            Integer count = dlStoreInfoDao.update(request);
            LOGGER.info("编辑DL->熙耘门店信息：{}", count);
        }
        return HttpResponse.success();
    }

    @Override
    public HttpResponse orderCancel(OrderCancelRequest request) {
        if (null == request) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }

        if (request.getCancelType() == 1) {
            OrderInfo orderInfo = orderInfoMapper.selectByOrderCode2(request.getOrderCode());
            if(orderInfo.getOrderStatus().equals(OrderStatus.TRANSACTION_TERMINATED_ABNORMALLY.getStatusCode()) ||
                    orderInfo.getOrderStatus().equals(OrderStatus.CANCELLED.getStatusCode())){
                LOGGER.info("重复调用，订单已经取消成功：", request.getOrderCode());
                return HttpResponse.success();
            }
        }else {
            ReturnOrderInfo returnOrderInfo = returnOrderInfoMapper.selectByCode(request.getOrderCode());
            if(returnOrderInfo.getOrderStatus().equals(ReturnOrderStatus.RETURN_ABNORMALLY_TERMINATED.getStatusCode()) ||
                    returnOrderInfo.getOrderStatus().equals(ReturnOrderStatus.cancelled.getStatusCode())){
                LOGGER.info("重复调用，退货单已经取消成功：", request.getOrderCode());
                return HttpResponse.success();
            }
        }

        LOGGER.info("DL->熙耘，单据取消参数：{}", JsonUtil.toJson(request));
        // 保存DL->熙耘，单据取消日志
        DlOrderBill info = new DlOrderBill();
        info.setDocumentCode(request.getOrderCode());
        info.setDocumentType(Global.CANCEL_ORDER_TYPE);
        info.setBusinessType(Global.PUSH_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        DlOrderBill dlOrderBill = dlOrderBillDao.selectByCode(info);
        if(dlOrderBill == null){
            info.setResponseCount(0);
            Integer logCount = dlOrderBillDao.insert(info);
            LOGGER.info("DL->熙耘，保存取消单据日志：{}, 单据号：{}", logCount, request.getOrderCode());
        }else {
            info.setResponseCount(dlOrderBill.getResponseCount() + 1);
            Integer logCount = dlOrderBillDao.update(info);
            LOGGER.info("DL->熙耘，编辑取消单据日志：{}", logCount);
        }

        // 调用耘链的取消单据接口
        HttpResponse response;
        if (request.getCancelType() == 1) {
            response = orderService.orderCancel(request.getOrderCode(), request.getOperatorId(), request.getOperatorName());
        }else {
            ReturnOrderInfo returnOrderInfo = new ReturnOrderInfo();
            returnOrderInfo.setReturnOrderCode(request.getOrderCode());
            returnOrderInfo.setReturnReasonContent(request.getRemark());
            returnOrderInfo.setUpdateById(request.getOperatorId());
            returnOrderInfo.setUpdateByName(request.getOperatorName());
            response = returnGoodsService.returnOrderCancel(returnOrderInfo);
        }

        if(response.getCode().equals(MessageId.SUCCESS_CODE)){
            LOGGER.info("DL->熙耘，保存取消单据成功！单据号：{}", request.getOrderCode());
            info.setReturnStatus(Global.SUCCESS);
        }else {
            LOGGER.info("DL->熙耘，保存取消单据失败! 单据号：{}， 错误信息：{}", request.getOrderCode(), response.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        // 调用之后变更日志状态
        info.setResponseDesc(response.getMessage());
        Integer count = dlOrderBillDao.update(info);
        LOGGER.info("DL->熙耘，变更取消单据日志状态：{}", count);
        return HttpResponse.success();
    }

    @Override
    @Async("myTaskAsyncPool")
    public HttpResponse supplierInfo(SupplierInfoRequest request){
        if(null == request){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("熙耘->DL，供应商信息参数：{}", JsonUtil.toJson(request));
        // 保存DL推送熙耘门店信息日志
        DlOtherInfo info = new DlOtherInfo();
        info.setDocumentCode(request.getSupplierCode());
        info.setDocumentType(Global.SUPPLIER_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
//        info.setDocumentContent(JsonUtil.toJson(request));
//        Integer logCount = dlOtherInfoDao.insert(info);
//        LOGGER.info("熙耘->DL，保存供应商日志：{}", logCount);

        // 调用耘链新增供应商接口
        String url = DL_URL + "/update/supplier";
        DLResponse dlResponse = dlHttpClientUtil.HttpHandler1(JsonUtil.toJson(request), url);
        if (dlResponse.getStatus() == 0) {
            LOGGER.info("熙耘->DL，保存供应商信息成功！供应商编码：{}", request.getSupplierCode());
            info.setReturnStatus(Global.SUCCESS);
        }else {
            LOGGER.info("熙耘->DL，保存供应商信息失败! 供应商编码：{}， 错误信息：{}",request.getSupplierCode(), dlResponse.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        // 调用之后变更日志状态
        info.setResponseDesc(dlResponse.getMessage());
        info.setRequestUrl(url);
        Integer count = dlOtherInfoDao.update(info);
        LOGGER.info("熙耘->DL，变更供应商日志状态：{}", count);
        return HttpResponse.success();
    }

    @Override
    @Async("myTaskAsyncPool")
    public HttpResponse productInspection(ProductInspectionDlRequest request) {
        DlOtherInfo info = new DlOtherInfo();
        info.setDocumentCode(request.getDocumentCode());
        info.setDocumentType(Global.INSPECTION_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
        // 调用DL推送质检报告
        String url = DL_URL + "/update/inspectionreport";
        DLResponse dlResponse = dlHttpClientUtil.HttpHandler1(JsonUtil.toJson(request.getList()), url);
        if (dlResponse.getStatus() == 0) {
            LOGGER.info("熙耘->DL，保存质检报告信息成功! 质检报告编码：{}", request.getDocumentCode());
            info.setReturnStatus(Global.SUCCESS);
        }else {
            LOGGER.info("熙耘->DL，保存质检报告信息失败！ 质检报告编码:{}， 错误信息：{}", request.getDocumentCode(), dlResponse.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        // 调用之后变更日志状态
        info.setResponseDesc(dlResponse.getMessage());
        info.setRequestUrl(url);
        Integer count = dlOtherInfoDao.update(info);
        LOGGER.info("熙耘->DL，变更质检报告日志状态：{}", count);
        return HttpResponse.success();
    }

    @Override
    public HttpResponse manualInspection() {
        // 查询所有的质检报告
        List<ProductInspectionRequest> list = productSkuInspReportDao.inspectionReportAll();
        ProductInspectionDlRequest request = new ProductInspectionDlRequest();
        DlOtherInfo info = new DlOtherInfo();
        String uuid = IdUtil.uuid();
        info.setDocumentCode(uuid);
        info.setDocumentType(Global.INSPECTION_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
        info.setDocumentContent(JsonUtil.toJson(list));
        LOGGER.info("熙耘->DL，保存质检报告数量：{}", list.size());
        Integer logCount = dlOtherInfoDao.insert(info);
        LOGGER.info("熙耘->DL，保存质检报告日志：{}", logCount);
        request.setDocumentCode(uuid);
        request.setList(list);
        HttpResponse response = this.productInspection(request);
        return response;

    }

    @Override
    public HttpResponse productInfo(ProductInfoRequest request){
        if(null == request){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        LOGGER.info("熙耘->DL，商品信息参数：{}", JsonUtil.toJson(request));
        // 保存熙耘推送DL库存变更日志
        DlOtherInfo info = new DlOtherInfo();
        info.setDocumentCode(request.getSkuCode());
        info.setDocumentType(Global.SKU_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
        info.setDocumentContent(JsonUtil.toJson(request));
        Integer logCount = dlOtherInfoDao.insert(info);
        LOGGER.info("熙耘->DL，保存商品信息日志：{}", logCount);

        // 调用Dl 回传商品变更
//        String url = "";
//        HttpClient httpClient = HttpClient.post(url).json(request).timeout(20000);
//        HttpResponse response = httpClient.action().result(HttpResponse.class);
//        if(response.getCode().equals(MessageId.SUCCESS_CODE)){
//            LOGGER.info("熙耘->DL，保存商品信息成功");
//            info.setReturnStatus(Global.SUCCESS);
//        }else {
//            LOGGER.info("熙耘->DL，保存商品信息失败：{}", response.getMessage());
//            info.setReturnStatus(Global.FAIL);
//        }
//        // 调用之后变更日志状态
//        info.setRequestUrl(url);
//        Integer count = dlOtherInfoDao.update(info);
//        LOGGER.info("熙耘->DL，变更商品信息日志状态：{}", count);
        return HttpResponse.success();
    }
}

package com.aiqin.bms.scmp.api.abutment.service.impl;

import com.aiqin.bms.scmp.api.abutment.dao.DlOrderBillDao;
import com.aiqin.bms.scmp.api.abutment.dao.DlOtherInfoDao;
import com.aiqin.bms.scmp.api.abutment.dao.DlStoreInfoDao;
import com.aiqin.bms.scmp.api.abutment.domain.DlOrderBill;
import com.aiqin.bms.scmp.api.abutment.domain.DlOtherInfo;
import com.aiqin.bms.scmp.api.abutment.domain.request.dl.*;
import com.aiqin.bms.scmp.api.abutment.domain.request.product.ProductInspectionRequest;
import com.aiqin.bms.scmp.api.abutment.domain.response.DLResponse;
import com.aiqin.bms.scmp.api.abutment.service.DlAbutmentService;
import com.aiqin.bms.scmp.api.abutment.service.ParameterAssemblyService;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.domain.pojo.ProductSkuInspReport;
import com.aiqin.bms.scmp.api.product.domain.request.ReturnOrderDetailReq;
import com.aiqin.bms.scmp.api.product.domain.request.ReturnOrderInfoReq;
import com.aiqin.bms.scmp.api.product.domain.request.ReturnReq;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.request.order.ErpOrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.request.order.ErpOrderItem;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoMapper;
import com.aiqin.bms.scmp.api.purchase.service.OrderService;
import com.aiqin.bms.scmp.api.purchase.service.ReturnGoodsService;
import com.aiqin.bms.scmp.api.supplier.dao.dictionary.SupplierDictionaryInfoDao;
import com.aiqin.bms.scmp.api.supplier.dao.supplier.SupplyCompanyAccountDao;
import com.aiqin.bms.scmp.api.supplier.dao.warehouse.WarehouseDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.DeliveryInformation;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.SupplyCompanyAccount;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.SupplyCompanyPurchaseGroup;
import com.aiqin.bms.scmp.api.supplier.domain.request.warehouse.dto.WarehouseDTO;
import com.aiqin.bms.scmp.api.util.BeanCopyUtils;
import com.aiqin.bms.scmp.api.util.DLHttpClientUtil;
import com.aiqin.bms.scmp.api.util.DateUtils;
import com.aiqin.ground.util.id.IdUtil;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ParameterAssemblyServiceImpl implements ParameterAssemblyService {

    private static Logger LOGGER = LoggerFactory.getLogger(ParameterAssemblyServiceImpl.class);
    @Resource
    private WarehouseDao warehouseDao;
    @Resource
    private SupplyCompanyAccountDao supplyCompanyAccountDao;
    @Resource
    private SupplierDictionaryInfoDao supplierDictionaryInfoDao;
    @Resource
    @Lazy
    private DlAbutmentService dlAbutmentService;
    @Resource
    @Lazy
    private OrderService orderService;
    @Resource
    @Lazy
    private ReturnGoodsService returnGoodsService;
    @Resource
    private DlOtherInfoDao dlOtherInfoDao;
    @Resource
    private DlOrderBillDao dlOrderBillDao;
    @Value("${dl.url}")
    private String DL_URL;
    @Resource
    private DLHttpClientUtil dlHttpClientUtil;
    @Resource
    private ReturnOrderInfoMapper returnOrderInfoMapper;

    @Override
    @Async("myTaskAsyncPool")
    public ErpOrderInfo orderInfoParameter(OrderInfoRequest request, DlOrderBill info) {
        ErpOrderInfo orderInfo = BeanCopyUtils.copy(request, ErpOrderInfo.class);
        orderInfo.setSourceCode(request.getOrderId());
        orderInfo.setRemake(request.getRemark());
        orderInfo.setOrderStoreCode(request.getOrderCode());
        // 默认公司为宁波熙耘
        orderInfo.setCompanyCode(Global.COMPANY_09);
        orderInfo.setCompanyName(Global.COMPANY_09_NAME);
        orderInfo.setOrderTypeCode(request.getOrderType().toString());
        // 供货方式 1配送、2直送、3货架直送、4采购直送
        orderInfo.setOrderTypeName(request.getOrderType() == 1 ? "配送" :(request.getOrderType() == 2 ? "直送" :
                (request.getOrderType() == 3 ? "供货直送" : "采购直送")));
        orderInfo.setOrderCategoryName(request.getOrderCategory());
        orderInfo.setOrderCategoryCode(request.getOrderCategoryCode());
        orderInfo.setStoreCode(request.getCustomerCode());
        orderInfo.setStoreName(request.getCustomerName());
        if(request.getOrderType() == 1){
            // 配送默认状态为6
            orderInfo.setOrderStatus(6);
        }else {
            // 其他的默认状态为5
            orderInfo.setOrderStatus(5);
        }
        // 默认转账
        orderInfo.setPaymentCode("1");
        orderInfo.setPaymentName("转账");
        orderInfo.setProvinceId(request.getProvinceCode());
        orderInfo.setOrderException(0);
        orderInfo.setOrderDelete(0);
        orderInfo.setOrderLock(0);
        orderInfo.setCityId(request.getCityCode());
        orderInfo.setDistrictId(request.getDistrictCode());
        orderInfo.setReceiveAddress(request.getDetailAddress());
        orderInfo.setReceivePerson(request.getConsignee());
        orderInfo.setReceiveMobile(request.getConsigneePhone());
        orderInfo.setProductChannelTotalAmount(request.getChannelTotalAmount());
        orderInfo.setTotalProductAmount(request.getProductTotalAmount());
        orderInfo.setDiscountAmount(request.getActivityDiscount());
        orderInfo.setTotalVolume(request.getVolume());
        orderInfo.setTotalWeight(request.getWeight());
        orderInfo.setCreateByName("DL同步");
        orderInfo.setSourceCode(request.getOrderId());
        // 默认为DL平台类型
        orderInfo.setPlatformType(1);
        // 默认主订单
        orderInfo.setOrderLevel(0);
        // 默认已支付
        if(StringUtils.isNotBlank(request.getDistributionModeCode())){
            orderInfo.setDistributionModeName(request.getDistributionModeCode().equals("0") ? "货运站" : "收货地址");
        }
        orderInfo.setPaymentStatus(0);
        if(StringUtils.isNotBlank(request.getChannelName())){
            // 渠道类型 1爱亲科技、2萌贝树、3小红马、4爱亲母婴
            orderInfo.setChannelName(request.getChannelName());
            String channelCode = "";
            if(request.getChannelName().equals("爱亲科技")){
                channelCode = "1";
            }else if(request.getChannelName().equals("萌贝树")){
                channelCode = "2";
            }else if(request.getChannelName().equals("小红马")){
                channelCode = "3";
            }else {
                channelCode = "4";
            }
            orderInfo.setChannelCode(channelCode);
        }
        // 转换库房信息
        WarehouseDTO warehouse = warehouseDao.warehouseDl(request.getWarehouseCode(), request.getWmsWarehouseType());
        if(warehouse == null){
            LOGGER.info("DL 推送销售单耘链的库房转换失败：{}", JsonUtil.toJson(request));
            return null;
        }
        orderInfo.setWarehouseCode(warehouse.getWarehouseCode());
        orderInfo.setWarehouseName(warehouse.getWarehouseName());
        orderInfo.setTransportCenterCode(warehouse.getLogisticsCenterCode());
        orderInfo.setTransportCenterName(warehouse.getLogisticsCenterName());
        if(CollectionUtils.isEmpty(request.getProductList()) && request.getProductList().size() == 0){
           LOGGER.info("DL 推送耘链销售单商品信息为空：{}", JsonUtil.toJson(request));
           return null;
        }
        ErpOrderItem item;
        List<ErpOrderItem> itemList = Lists.newArrayList();
        for (ProductRequest product : request.getProductList()){
            item = BeanCopyUtils.copy(product, ErpOrderItem.class);
            item.setOrderStoreCode(request.getOrderCode());
            item.setModelCode(product.getModelNumber());
            item.setTotalAcivityAmount(product.getActivityApportionment());
            item.setTotalPreferentialAmount(product.getPreferentialAllocation());
            item.setPurchaseAmount(product.getChannelAmount());
            item.setProductCount(product.getTotalCount());
            item.setLineCode(product.getLineCode().longValue());
            if(CollectionUtils.isNotEmpty(product.getBatchList()) && product.getBatchList().size() > 0){
               for (BatchRequest batch : product.getBatchList()){
                   item.setProductCount(batch.getTotalCount());
//                   item.setBatchDate(DateUtils.strToDateLong(batch.getProductDate()));
                   item.setBatchDate(batch.getProductDate());
                   item.setBatchCode(batch.getBatchCode());
                   itemList.add(item);
               }
            }else {
                item.setProductCount(product.getTotalCount());
                itemList.add(item);
            }
        }
        orderInfo.setItemList(itemList);
        LOGGER.info("DL 推送销售单耘链的参数转换：{}", JsonUtil.toJson(orderInfo));

        // 调用耘链接口 生成对应的销售单信息
        HttpResponse response = orderService.insertSaleOrder(orderInfo);
        if(response.getCode().equals(MessageId.SUCCESS_CODE)){
            LOGGER.info("DL->熙耘，保存耘链销售单成功, 销售单号：{}", request.getOrderCode());
            info.setReturnStatus(Global.SUCCESS);
        }else {
            LOGGER.info("DL->熙耘，保存耘链销售单失败！ 销售单号:{}, 错误信息：{}", request.getOrderCode(), response.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        info.setResponseDesc(response.getMessage());

        // 调用之后变更日志状态
        Integer count = dlOrderBillDao.update(info);
        LOGGER.info("DL->熙耘，变更销售单日志状态：{}", count);
        return orderInfo;
    }

    @Override
    @Async("myTaskAsyncPool")
    public ReturnReq returnInfoParameter(ReturnOrderInfoRequest request, DlOrderBill info) {
        // 查询退货单是否已经回传
        ReturnOrderInfo returnOrderInfo = returnOrderInfoMapper.selectByCode(request.getReturnOrderCode());
        ReturnReq returnRequest = new ReturnReq();
        if(returnOrderInfo != null && request.getBusinessForm() == 7){
            // 更新退货单的物流信息
            ReturnOrderInfo returnOrder = new ReturnOrderInfo();
            returnOrder.setReturnOrderCode(request.getReturnOrderCode());
            returnOrder.setRemake(request.getRemark());
            returnOrder.setTransportCompanyCode(request.getTransportCompanyCode());
            returnOrder.setTransportCompany(request.getTransportCompanyName());
            returnOrder.setTransportNumber(request.getTransportCompanyNumber());
            Integer count = returnOrderInfoMapper.update(returnOrder);
            if (count > 0) {
                LOGGER.info("DL->熙耘，多次回传更新物流单信息成功");
                info.setReturnStatus(Global.SUCCESS);
            } else {
                LOGGER.info("DL->熙耘，多次回传更新物流单信息失败！ 退货单号：{}", request.getReturnOrderCode());
                info.setReturnStatus(Global.FAIL);
            }
        }else {
            ReturnOrderInfoReq returnInfo = BeanCopyUtils.copy(request, ReturnOrderInfoReq.class);
            returnInfo.setReturnOrderId(request.getReturnOrderId());
            returnInfo.setOrderStoreCode(request.getOrderCode());
            returnInfo.setRemark(request.getRemark());
            // 默认公司为宁波熙耘
            returnInfo.setCompanyCode(Global.COMPANY_09);
            returnInfo.setCompanyName(Global.COMPANY_09_NAME);
            returnInfo.setOrderType(request.getOrderType());
            // 默认售后退货
            returnInfo.setReturnOrderType(4);
            returnInfo.setReturnLock(1);
            // 默认已支付
            returnInfo.setPaymentStatus(0);
            // 默认转账
            returnInfo.setPaymentCode("1");
            returnInfo.setPaymentName("转账");
            // 默认退货退款
            returnInfo.setTreatmentMethod(1);
            returnInfo.setStoreCode(request.getCustomerCode());
            returnInfo.setStoreName(request.getCustomerName());
            returnInfo.setLogisticsCompanyCode(request.getTransportCompanyCode());
            returnInfo.setLogisticsCompanyName(request.getTransportCompanyName());
            returnInfo.setLogisticsCode(request.getTransportCompanyNumber());
            returnInfo.setReturnOrderAmount(request.getProductTotalAmount());
            returnInfo.setReceivePerson(request.getConsignor());
            returnInfo.setReceiveMobile(request.getConsignorPhone());
            returnInfo.setTotalWeight(request.getWeight());
            returnInfo.setTotalVolume(request.getVolume());
            returnInfo.setRemark(request.getReturnReason());
            returnInfo.setUseStatus(0);
            returnInfo.setUpdateById(request.getCreateById());
            returnInfo.setUpdateByName(request.getCreateByName());
            // 默认未退款
            returnInfo.setRefundStatus(0);
            // 默认整单退
            returnInfo.setProcessType(0);
            // 来源DL
            returnInfo.setSourceType(4);
            // 默认为DL平台类型
            returnInfo.setPlatformType(1);
            returnInfo.setCopartnerAreaId(request.getPartnerCode());
            returnInfo.setCopartnerAreaName(request.getPartnerName());
            // 转换库房信息
            WarehouseDTO warehouse = warehouseDao.warehouseDl(request.getTransportCenterCode(), request.getWmsWarehouseType());
            if (warehouse == null) {
                LOGGER.info("DL 推送退货单耘链的库房转换失败：{}", JsonUtil.toJson(request));
                return null;
            }
            returnInfo.setWarehouseCode(warehouse.getWarehouseCode());
            returnInfo.setWarehouseName(warehouse.getWarehouseName());
            returnInfo.setTransportCenterCode(warehouse.getLogisticsCenterCode());
            returnInfo.setTransportCenterName(warehouse.getLogisticsCenterName());
            returnInfo.setChannelName(request.getChannelName());
            String channelCode;
            // 渠道类型 1爱亲科技、2萌贝树、3小红马、4爱亲母婴
            if (StringUtils.isNotBlank(request.getChannelName()) && request.getChannelName().equals("爱亲科技")) {
                channelCode = "1";
            } else if (StringUtils.isNotBlank(request.getChannelName()) && request.getChannelName().equals("萌贝树")) {
                channelCode = "2";
            } else if (StringUtils.isNotBlank(request.getChannelName()) && request.getChannelName().equals("小红马")) {
                channelCode = "3";
            } else {
                channelCode = "4";
            }
            returnInfo.setChannelCode(channelCode);
            returnRequest.setReturnOrderInfo(returnInfo);
            if (CollectionUtils.isEmpty(request.getProductList()) && request.getProductList().size() <= 0) {
                LOGGER.info("DL->耘链 推送耘链退货单商品信息为空");
                return null;
            }
            ReturnOrderDetailReq item;
            List<ReturnOrderDetailReq> itemList = Lists.newArrayList();
            for (ProductRequest product : request.getProductList()) {
                item = BeanCopyUtils.copy(product, ReturnOrderDetailReq.class);
                item.setReturnOrderDetailId(IdUtil.uuid());
                item.setReturnOrderCode(request.getReturnOrderCode());
                item.setModelCode(product.getModelNumber());
                // 默认商品类型 - 商品
                item.setProductType(product.getProductType());
                item.setZeroDisassemblyCoefficient(1L);
                item.setReturnProductCount(product.getTotalCount());
                item.setTotalProductAmount(product.getProductTotalAmount());
                item.setUseStatus(0);
                item.setCreateById(request.getCreateById());
                item.setCreateByName(request.getCreateByName());
                item.setLineCode(product.getLineCode().longValue());
                item.setReturnProductCount(product.getTotalCount());
                itemList.add(item);
            }
            returnRequest.setReturnOrderDetailReqList(itemList);
            LOGGER.info("DL->耘链 推送退货单到耘链的参数转换：{}", JsonUtil.toJson(returnRequest));

            // 调用耘链 生成耘链对应的退货单、出库单
            HttpResponse response = returnGoodsService.record(returnRequest);
            if (response.getCode().equals(MessageId.SUCCESS_CODE)) {
                LOGGER.info("DL->熙耘，保存退货单成功! 退货单号：{}", request.getReturnOrderCode());
                info.setReturnStatus(Global.SUCCESS);
            } else {
                LOGGER.info("DL->熙耘，保存退货单失败! 退货单号:{}， 错误信息：{}", request.getReturnOrderCode(), response.getMessage());
                info.setReturnStatus(Global.FAIL);
            }
            info.setResponseDesc(response.getMessage());
        }
        Integer count = dlOrderBillDao.update(info);
        LOGGER.info("DL->熙耘，变更退货单日志状态：{}", count);
        return returnRequest;
    }

    @Override
    public SupplierInfoRequest supplierParameter(SupplierAbutmentRequest request){
        LOGGER.info("供应商开始转换调用DL参数：{}", JsonUtil.toJson(request));
        SupplierInfoRequest supplierInfo = BeanCopyUtils.copy(request, SupplierInfoRequest.class);
        supplierInfo.setSupplierCode(request.getSupplyCode());
        supplierInfo.setSupplierName(request.getSupplyName());
        supplierInfo.setSupplierAbbreviation(request.getSupplyAbbreviation() == null ? "" : request.getSupplyAbbreviation() );
        // 查询供应商的类型
        String typeName = supplierDictionaryInfoDao.dictionaryDetailInfo(request.getSupplyType(), "106");
        LOGGER.info("查询供应商属性信息：{}", typeName);
        supplierInfo.setSupplierType(typeName);
        supplierInfo.setMobile(request.getPhone());
        supplierInfo.setContactsPhone(request.getMobilePhone());
        supplierInfo.setContacts(request.getContactName());
        supplierInfo.setProvinceCode(request.getProvinceId());
        supplierInfo.setCityCode(request.getCityId());
        supplierInfo.setDistrictCode(request.getDistrictId());
        supplierInfo.setDetailAddress(request.getAddress());
        supplierInfo.setEnable(request.getEnable().intValue());
        supplierInfo.setSupplierCompanyCode(request.getSupplierCode());
        supplierInfo.setSupplierCompanyName(request.getSupplierName());
        supplierInfo.setPurchaseGroupCode(request.getPurchasingGroupCode());
        supplierInfo.setPurchaseGroupName(request.getPurchasingGroupName());
        // 查询供应商的结算账户信息
        SupplyCompanyAccount account = supplyCompanyAccountDao.companyAccount(supplierInfo.getSupplierCode());
        if(account != null){
            supplierInfo.setBankAccount(account.getBankAccount());
            supplierInfo.setAccount(account.getAccount());
            supplierInfo.setAccountName(account.getAccountName());
            supplierInfo.setMaxPaymentAmount(account.getMaxPaymentAmount());
        }else {
            supplierInfo.setBankAccount("");
            supplierInfo.setAccount("");
            supplierInfo.setAccountName("");
            supplierInfo.setMaxPaymentAmount(BigDecimal.ZERO);
        }

        if(CollectionUtils.isNotEmpty(request.getDeliveryList())){
            List<SupplierDeliveryRequest> deliveryRequests = Lists.newArrayList();
            SupplierDeliveryRequest deliveryRequest;
            for (DeliveryInformation delivery : request.getDeliveryList()){
                deliveryRequest = new SupplierDeliveryRequest();
                deliveryRequest.setDeliveryType(delivery.getDeliveryType().intValue());
                deliveryRequest.setProvinceCode(delivery.getSendProvinceId());
                deliveryRequest.setProvinceName(delivery.getSendProvinceName());
                deliveryRequest.setCityCode(delivery.getSendCityId());
                deliveryRequest.setCityName(delivery.getSendCityName());
                deliveryRequest.setDistrictCode(delivery.getSendDistrictId());
                deliveryRequest.setDistrictName(delivery.getSendDistrictName());
                deliveryRequest.setDetailAddress(delivery.getSendingAddress());
                deliveryRequests.add(deliveryRequest);
            }
            supplierInfo.setDeliveryList(deliveryRequests);
        }
        LOGGER.info("供应商转换调用dl参数：{}", JsonUtil.toJson(supplierInfo));

        // 保存DL推送熙耘门店信息日志
        DlOtherInfo info = new DlOtherInfo();
        info.setDocumentCode(supplierInfo.getSupplierCode());
        info.setDocumentType(Global.SUPPLIER_TYPE);
        info.setBusinessType(Global.ECHO_TYPE);
        info.setDocumentContent(JsonUtil.toJson(supplierInfo));
        Integer logCount = dlOtherInfoDao.insert(info);
        LOGGER.info("熙耘->DL，保存供应商日志：{}", logCount);

        dlAbutmentService.supplierInfo(supplierInfo);
        return supplierInfo;
    }

    @Override
    @Async("myTaskAsyncPool")
    public void orderTransportParameter(OrderTransportRequest request, DlOrderBill info){
        // 调用Dl 回传DL物流单
        String url = DL_URL + "/back/logisticsOrder";
        DLResponse dlResponse = dlHttpClientUtil.HttpHandler1(JsonUtil.toJson(request), url);
        if (dlResponse.getStatus() == 0) {
            LOGGER.info("熙耘->DL，保存销售物流单成功! 物流单号：{}", request.getTransportCode());
            info.setReturnStatus(Global.SUCCESS);
        } else {
            LOGGER.info("熙耘->DL，保存销售物流单失败! 物流单号：{}, 错误信息：{}",  request.getTransportCode(), dlResponse.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        // 调用之后变更日志状态
        info.setRequestUrl(url);
        info.setResponseDesc(dlResponse.getMessage());
        Integer count = dlOrderBillDao.update(info);
        LOGGER.info("熙耘->DL，变更销售物流单日志状态：{}", count);
    }

    @Override
    @Async("myTaskAsyncPool")
    public void echoOrderInfoParameter(EchoOrderRequest request, DlOrderBill info, String url) {
        if (request.getOperationType() == 4) {
            if (CollectionUtils.isNotEmpty(request.getProductList()) && request.getProductList().size() > 0) {
                WarehouseDTO warehouse;
                for (ProductRequest product : request.getProductList()) {
                    warehouse = warehouseDao.getWarehouseByCode(product.getWarehouseCode());
                    if (warehouse != null) {
                        product.setWarehouseCode(warehouse.getWmsWarehouseId());
                        product.setWarehouseName(warehouse.getWmsWarehouseName());
                        product.setWmsWarehouseType(warehouse.getWmsWarehouseType());
                        if (warehouse.getWmsWarehouseType() == 2) {
                            product.setReturnType(2);
                        } else {
                            product.setReturnType(1);
                        }
                    }
                }
            }
        }

        DLResponse dlResponse = dlHttpClientUtil.HttpHandler1(JsonUtil.toJson(request), url);
        if (dlResponse.getStatus() == 0) {
            LOGGER.info("熙耘->DL，保存回传DL单据成功! 单据编号：{}", request.getOrderCode());
            info.setReturnStatus(Global.SUCCESS);
        } else {
            LOGGER.info("熙耘->DL，保存回传单据失败! 单号：{}, 错误信息：{}",  request.getOrderCode(), dlResponse.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        // 调用之后变更日志状态
        info.setResponseDesc(dlResponse.getMessage());
        info.setRequestUrl(url);
        Integer count = dlOrderBillDao.update(info);
        LOGGER.info("熙耘->DL，变更回传单据日志状态：{}", count);
    }

    @Override
    @Async("myTaskAsyncPool")
    public void stockChangeParameter(StockChangeRequest request, DlOtherInfo info){
        String url = DL_URL + "/back/stock/change";
        DLResponse dlResponse = dlHttpClientUtil.HttpHandler1(JsonUtil.toJson(request), url);
        if (dlResponse.getStatus() == 0) {
            LOGGER.info("熙耘->DL，保存库存变更成功! 单据号：{}", request.getOrderCode());
            info.setReturnStatus(Global.SUCCESS);
        } else {
            LOGGER.info("熙耘->DL，保存库存变更失败! 单据号：{}， 错误信息：{}", request.getOrderCode(), dlResponse.getMessage());
            info.setReturnStatus(Global.FAIL);
        }
        // 调用之后变更日志状态
        info.setRequestUrl(url);
        info.setResponseDesc(dlResponse.getMessage());
        Integer count = dlOtherInfoDao.update(info);
        LOGGER.info("熙耘->DL，变更库存变更日志状态：{}", count);
    }

    @Override
    public ProductInspectionDlRequest productInspectionParameter(List<ProductSkuInspReport> productSkuInspReports){
        LOGGER.info("推送DL质检报告参数：{}", JsonUtil.toJson(productSkuInspReports));
        ProductInspectionDlRequest request = new ProductInspectionDlRequest();
        List<ProductInspectionRequest> list = Lists.newArrayList();
        ProductInspectionRequest productInspection;
        if(CollectionUtils.isNotEmpty(productSkuInspReports)){
            for(ProductSkuInspReport report : productSkuInspReports){
                productInspection = new ProductInspectionRequest();
                productInspection.setInspectionReportPath(report.getInspectionReportPath());
                productInspection.setSkuCode(report.getSkuCode());
                productInspection.setProductDate(report.getProductionDate());
                list.add(productInspection);
            }
            LOGGER.info("转化为推送DL的质检报告：{}", JsonUtil.toJson(list));
            // 保存DL推送熙耘门店信息日志
            String uuid = IdUtil.uuid();
            request.setDocumentCode(uuid);
            request.setList(list);

            DlOtherInfo info = new DlOtherInfo();
            info.setDocumentCode(uuid);
            info.setDocumentType(Global.INSPECTION_TYPE);
            info.setBusinessType(Global.ECHO_TYPE);
            info.setDocumentContent(JsonUtil.toJson(request));
            Integer logCount = dlOtherInfoDao.insert(info);
            LOGGER.info("熙耘->DL，保存质检报告日志：{}", logCount);
            dlAbutmentService.productInspection(request);
        }
        return request;
    }

    @Override
    @Async("myTaskAsyncPool")
    public void monthStockDlParameter(List<MonthStockRequest> list) {
        List<List<MonthStockRequest>> partitionList = Lists.partition(list, 500);
        String url = DL_URL + "/update/productdate";
        LOGGER.info("调用DL路径：{}", url);
        for (List<MonthStockRequest> monthList : partitionList) {
            DLResponse dlResponse = dlHttpClientUtil.HttpHandler1(JsonUtil.toJson(monthList), url);
            if (dlResponse.getStatus() == 0) {
                LOGGER.info("熙耘->DL，同步日期批次信息成功:{}", dlResponse.getMessage());
            } else {
                LOGGER.info("熙耘->DL，同步日期批次信息失败:{}", dlResponse.getMessage());
            }
        }


    }

}

package com.aiqin.bms.scmp.api.excel.service;

import com.aiqin.bms.scmp.api.excel.domain.*;
import com.aiqin.bms.scmp.api.excel.utils.ListUtils;
import com.aiqin.bms.scmp.api.purchase.dao.PurchaseOrderDao;
import com.aiqin.bms.scmp.api.purchase.dao.PurchaseOrderProductDao;
import com.aiqin.bms.scmp.api.purchase.dao.RejectRecordDao;
import com.aiqin.bms.scmp.api.purchase.dao.RejectRecordDetailDao;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseOrder;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseOrderProduct;
import com.aiqin.bms.scmp.api.purchase.domain.RejectRecord;
import com.aiqin.bms.scmp.api.purchase.domain.RejectRecordDetail;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.order.OrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.order.OrderInfoItem;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfoItem;
import com.aiqin.bms.scmp.api.purchase.mapper.OrderInfoItemMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.OrderInfoMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoItemMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoMapper;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import com.aiqin.bms.scmp.api.util.DateUtils;
import com.aiqin.bms.scmp.api.util.excel.utils.ExcelUtil;
import com.aiqin.ground.util.id.IdUtil;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.Project;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaveService {

    @Resource
    private PurchaseOrderDao purchaseOrderDao;

    @Resource
    private PurchaseOrderProductDao purchaseOrderProductDao;

    @Resource
    private RejectRecordDao rejectRecordDao;

    @Resource
    private RejectRecordDetailDao rejectRecordDetailDao;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private OrderInfoItemMapper orderInfoItemMapper;

    @Resource
    private ReturnOrderInfoMapper returnOrderInfoMapper;

    @Resource
    private ReturnOrderInfoItemMapper returnOrderInfoItemMapper;




    /**
     * 存储采购单主表信息
     *
     * @param purchaseOrders
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDb(List<PurchaseOrderExcel> purchaseOrders) throws Exception {
        purchaseOrders = purchaseOrders.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PurchaseOrderExcel :: getPurchaseOrderCode))), ArrayList::new));
        List<String> code = purchaseOrders.stream().map(PurchaseOrderExcel::getPurchaseOrderCode).collect(Collectors.toList());
        if (CollectionUtils.isNotEmptyCollection(code)) {
            //查询出已经存在的采购编号数据
            List<PurchaseOrder> existOrderCodeList = this.purchaseOrderDao.selectByPurchaseOrderCode(code);
            if (CollectionUtils.isNotEmptyCollection(existOrderCodeList)) {
                //已经存在采购单号
                List<String> existOrderCodes = existOrderCodeList.stream().map(PurchaseOrder::getPurchaseOrderCode).collect(Collectors.toList());
                //删除已经存在的
                Iterator<PurchaseOrderExcel> iterator = purchaseOrders.iterator();
                while (iterator.hasNext()) {
                    PurchaseOrderExcel next = iterator.next();
                    if (existOrderCodes.contains(next.getPurchaseOrderCode())) {
                        iterator.remove();
                    }
                }

            }

            if (CollectionUtils.isNotEmptyCollection(purchaseOrders)) {
                String s = JsonUtil.toJson(purchaseOrders);
                List<PurchaseOrder> saves = JSONObject.parseArray(s, PurchaseOrder.class);
                purchaseOrders.clear();

                saves.forEach(p -> {
                    p.setPurchaseOrderId(IdUtil.purchaseId());
                    //因为状态都一样所以写死了
                    p.setPurchaseOrderStatus(8);
                    Date date = DateUtils.getDate(p.getPreArrivalDate());
                    p.setPreArrivalTime(date);
                    p.setCompanyCode("09");
                    p.setCompanyName("宁波熙耘科技有限公司");
                });
                if (CollectionUtils.isNotEmptyCollection(saves)) {
                    Map<Integer, List<PurchaseOrder>> itemMap = new ListUtils<PurchaseOrder>().batchList(saves, 3000);
                    for (Integer i : itemMap.keySet()) {
                        //数据量太多会mysql报错 分批次插入
                        List<PurchaseOrder> orderInfosItems = itemMap.get(i);
                        this.purchaseOrderDao.insertMany(orderInfosItems);
                    }

                }
                log.info("执行完成采购单主表数据插入条数===================={}", saves.size());
                saves = null;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }


    }

    /**
     * 保存采购单明细
     *
     * @param pus
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void savepurchaseOrderProductDb(List<PurchaseOrderProductExcel> pus) throws Exception {
        if (CollectionUtils.isNotEmptyCollection(pus)) {
            String s = JsonUtil.toJson(pus);
            List<PurchaseOrderProduct> saves = JSONObject.parseArray(s, PurchaseOrderProduct.class);
            pus.clear();
            //通过采购单号获取采购id
            List<String> purchaseOrderCode = saves.stream().map(PurchaseOrderProduct::getPurchaseOrderCode).distinct().collect(Collectors.toList());
            List<PurchaseOrder> purchaseOrders = this.purchaseOrderDao.selectByPurchaseOrderCode(purchaseOrderCode);
            Map<String, String> purchaseOrderIdMap = new HashMap<>();

            if (CollectionUtils.isNotEmptyCollection(purchaseOrders)) {
                purchaseOrderIdMap = purchaseOrders.stream().collect(Collectors.toMap(PurchaseOrder::getPurchaseOrderCode, PurchaseOrder::getPurchaseOrderId));
                purchaseOrders.clear();
            }
            List<String> existPurchaseOrders = this.purchaseOrderProductDao.selectByPurchaseOrderList(purchaseOrderCode);

            Iterator<PurchaseOrderProduct> iterator = saves.iterator();
            while (iterator.hasNext()) {
                PurchaseOrderProduct save = iterator.next();
                if (CollectionUtils.isNotEmptyCollection(existPurchaseOrders)) {
                    if (existPurchaseOrders.contains(save.getPurchaseOrderCode())) {
                        iterator.remove();
                    }
                } else {
                    save.setBoxGauge(save.getProductCount() + "/" + save.getDanwei());
                    if ("普通商品".equals(save.getLiebie())) {
                        save.setProductType(0);
                    } else if ("实物返".equals(save.getLiebie())) {
                        save.setProductType(2);
                    } else if ("赠品".equals(save.getLiebie())) {
                        save.setProductType(1);
                    }
                    //设置采购单id
                    String purchaseOrderId = purchaseOrderIdMap.get(save.getPurchaseOrderCode());
                    save.setPurchaseOrderId(purchaseOrderId);
                    save.setOrderProductId(UUID.randomUUID().toString().replaceAll("-", ""));

                }

            }
            purchaseOrderIdMap.clear();
            if (CollectionUtils.isNotEmptyCollection(saves)) {

                Map<Integer, List<PurchaseOrderProduct>> itemMap = new ListUtils<PurchaseOrderProduct>().batchList(saves, 3000);
                for (Integer i : itemMap.keySet()) {
                    //数据量太多会mysql报错 分批次插入
                    List<PurchaseOrderProduct> purchaseOrderProducts = itemMap.get(i);
                    this.purchaseOrderProductDao.insertAll(purchaseOrderProducts);
                }

            }
            log.info("执行完成采购单明细表数据插入条数==============================={}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }

    /**
     * 保存退供单主表
     *
     * @param rrs
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRejectRecord(List<RejectRecordExcel> rrs) throws Exception {
        if (CollectionUtils.isNotEmptyCollection(rrs)) {
            rrs = rrs.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(RejectRecordExcel :: getRejectRecordCode))), ArrayList::new));
            List<String> rejectCodeList = rrs.stream().map(RejectRecordExcel::getRejectRecordCode).collect(Collectors.toList());

            //查询出已经在退供主表存在的退供单单号信息
            List<RejectRecord> existRejectCodeList = this.rejectRecordDao.selectByRejectCodeList(rejectCodeList);
            if (CollectionUtils.isNotEmptyCollection(existRejectCodeList)) {
                //已经存在退供单信息
                List<String> existOrderCodes = existRejectCodeList.stream().map(RejectRecord::getRejectRecordCode).collect(Collectors.toList());

                Iterator<RejectRecordExcel> iterator = rrs.iterator();
                while (iterator.hasNext()) {
                    RejectRecordExcel next = iterator.next();
                    if (existOrderCodes.contains(next.getRejectRecordCode())) {
                        //删除已经存在的
                        iterator.remove();
                    }
                }

            }


            String s = JsonUtil.toJson(rrs);
            List<RejectRecord> saves = JSONObject.parseArray(s, RejectRecord.class);
            rrs = null;
            //遍历设置reject_record_id的值
            saves.forEach(p -> {
                p.setRejectRecordId(IdUtil.rejectRecordId());
                p.setRejectStatus(3);
                p.setCompanyCode("09");
                p.setCompanyName("宁波熙耘科技有限公司");
                p.setOutStockTime(p.getUpdateTime());
            });
            if (CollectionUtils.isNotEmptyCollection(saves)) {
                Map<Integer, List<RejectRecord>> itemMap = new ListUtils<RejectRecord>().batchList(saves, 3000);
                for (Integer i : itemMap.keySet()) {
                    //数据量太多会mysql报错 分批次插入
                    List<RejectRecord> rejectRecord = itemMap.get(i);
                    this.rejectRecordDao.insertMany(rejectRecord);
                }
            }

            log.info("执行完成退供单主表插入条数================================{}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }

    /**
     * 保存退供单明细
     *
     * @param rrd
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRejectRecordDetail(List<RejectRecordDetailExcel> rrd) throws Exception {

        if (CollectionUtils.isNotEmptyCollection(rrd)) {
            String s = JsonUtil.toJson(rrd);
            List<RejectRecordDetail> saves = JSONObject.parseArray(s, RejectRecordDetail.class);
            rrd = null;
            //通过退供单号获取 reject_record_id退供单id
            List<String> purchaseOrderCodeList = saves.stream().map(RejectRecordDetail::getRejectRecordCode).distinct().collect(Collectors.toList());
            List<RejectRecord> purchaseOrders = this.rejectRecordDao.selectByRejectCodeList(purchaseOrderCodeList);
            Map<String, String> purchaseOrderIdMap = new HashMap<>();

            if (CollectionUtils.isNotEmptyCollection(purchaseOrders)) {
                purchaseOrderIdMap = purchaseOrders.stream().collect(Collectors.toMap(RejectRecord::getRejectRecordCode, RejectRecord::getRejectRecordId));
                purchaseOrders.clear();
            }

            //删除已经存在的数据
            List<String> existRejectRecordCodes = this.rejectRecordDetailDao.selectByRejectCodeList(purchaseOrderCodeList);
            Iterator<RejectRecordDetail> iterator = saves.iterator();
            while (iterator.hasNext()) {
                RejectRecordDetail save = iterator.next();
                if (CollectionUtils.isNotEmptyCollection(existRejectRecordCodes)) {
                    if (existRejectRecordCodes.contains(save.getRejectRecordCode())) {
                        iterator.remove();
                    }
                } else {
                    //设置采购单id
                    String rejectRecordId = purchaseOrderIdMap.get(save.getRejectRecordCode());
                    save.setRejectRecordId(rejectRecordId);
                    save.setRejectRecordDetailId(UUID.randomUUID().toString().replaceAll("-", ""));
                }

            }
            purchaseOrderIdMap = null;
            if (CollectionUtils.isNotEmptyCollection(saves)) {
                Map<Integer, List<RejectRecordDetail>> itemMap = new ListUtils<RejectRecordDetail>().batchList(saves, 3000);
                for (Integer i : itemMap.keySet()) {
                    //数据量太多会mysql报错 分批次插入
                    List<RejectRecordDetail> orderInfosItems = itemMap.get(i);
                    this.rejectRecordDetailDao.insertAll(orderInfosItems);
                }
            }
            log.info("执行完成退供单明细表插入条数=================================={}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 保存销售主表信息
     *
     * @param of
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderInfo(List<OrderInfoExcel> of) {

        if (CollectionUtils.isNotEmptyCollection(of)) {
            of = of.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OrderInfoExcel :: getOrderCode))), ArrayList::new));
            List<String> orderCodeList = of.stream().map(OrderInfoExcel::getOrderCode).collect(Collectors.toList());

            //查询出已经在存在的销售单信息
            List<String> existOrderCodeeList = this.orderInfoMapper.selectCodesByOrderCodes(orderCodeList);
            if (CollectionUtils.isNotEmptyCollection(existOrderCodeeList)) {
                //已经存在销售单信息
                Iterator<OrderInfoExcel> iterator = of.iterator();
                while (iterator.hasNext()) {
                    OrderInfoExcel next = iterator.next();
                    if (existOrderCodeeList.contains(next.getOrderCode())) {
                        //删除已经存在的
                        iterator.remove();
                    }
                }

            }

            String s = JsonUtil.toJson(of);
            List<OrderInfo> saves = JSONObject.parseArray(s, OrderInfo.class);
            saves.forEach(o -> {
                o.setPaymentStatus(1);
                o.setOrderStatus(12);
                o.setBeException(0);
                o.setBeLock(0);
                o.setBeDelete(0);
                o.setPaymentType("转账");
                o.setOrderType("配送");
                o.setOrderTypeCode(1);
                o.setChannelCode("1");
                o.setChannelName("爱亲科技");
                o.setOrderOriginalName("爱亲科技");
                o.setBusinessForm(1);
                o.setPlatformType(1);
                o.setOrderProductType(0);
                o.setBeMasterOrder(1);
                //o.setActualProductTotalAmount(o.getProductTotalAmount());
                o.setCompanyCode("09");
                o.setCompanyName("宁波熙耘科技有限公司");
                o.setStoreName(o.getCustomerName());
                o.setActualDeliverAmount(o.getDeliverAmount());
                if (StringUtils.isNotBlank(o.getCustomerCode())) {
                    o.setStoreCode(o.getCustomerCode());
                }
                o.setProductTotalAmount(o.getActualProductTotalAmount());
                o.setActualChannelOrderAmount(o.getActualProductTotalAmount());
                o.setChannelOrderAmount(o.getActualProductTotalAmount());
                o.setOrderAmount(o.getProductTotalAmount());
                //商品渠道总金额
                o.setProductChannelTotalAmount(o.getChannelOrderAmount());
                //实际渠道总价actual_product_channel_total_amount
                o.setActualProductChannelTotalAmount(o.getActualChannelOrderAmount());
                //实际分销订单金额  actual_order_amount
                o.setActualOrderAmount(o.getActualProductTotalAmount());
                o.setProductNum(o.getActualProductNum());
                o.setActualDeliverAmount(o.getDeliverAmount());
                System.out.println(o);

            });
            of = null;
            if (CollectionUtils.isNotEmptyCollection(saves)) {
                //数据量太多会mysql报错 分批次插入
                Map<Integer, List<OrderInfo>> itemMap = new ListUtils<OrderInfo>().batchList(saves, 3000);

                for (Integer i : itemMap.keySet()) {
                    List<OrderInfo> orderInfos1 = itemMap.get(i);
                    orderInfoMapper.insertMany(orderInfos1);
                }
            }
            log.info("执行完成销售主表插入条数=================================={}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 保存销售单明细数据
     *
     * @param oft
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderInfoItem(List<OrderInfoItemExcel> oft) {
        if (CollectionUtils.isNotEmptyCollection(oft)) {
            String s = JsonUtil.toJson(oft);
            List<OrderInfoItem> saves = JSONObject.parseArray(s, OrderInfoItem.class);
            s = null;
            oft = null;
            List<String> orderCodeList = saves.stream().map(OrderInfoItem::getOrderCode).distinct().collect(Collectors.toList());
            //删除已经存在的数据
            List<String> existRejectRecordCodes = this.orderInfoItemMapper.selectByOrderCodes(orderCodeList);
            Set set = new HashSet();
            if (CollectionUtils.isNotEmptyCollection(existRejectRecordCodes)) {
                set.addAll(existRejectRecordCodes);
            }
            orderCodeList = null;
            Iterator<OrderInfoItem> iterator = saves.iterator();
            while (iterator.hasNext()) {
                OrderInfoItem save = iterator.next();
                if (CollectionUtils.isNotEmptyCollection(set)&&set.contains(save.getOrderCode())) {
                    iterator.remove();
                } else {
                    save.setPrice(save.getActualPrice());
                    save.setNum(save.getActualDeliverNum());
                    save.setAmount(save.getActualAmount());
                    save.setCompanyCode("09");
                    save.setCompanyName("宁波熙耘科技有限公司");
                    save.setChannelUnitPrice(save.getPrice());
                    save.setTotalChannelPrice(save.getAmount());
                    save.setActualTotalChannelPrice(save.getActualAmount());

                }
            }
            existRejectRecordCodes = null;

//            saves.forEach(o -> {
//                o.setPrice(o.getActualPrice());
//                o.setNum(o.getActualDeliverNum());
//                o.setAmount(o.getActualAmount());
//                o.setCompanyCode("09");
//                o.setCompanyName("宁波熙耘科技有限公司");
//
//            });
            if (CollectionUtils.isNotEmptyCollection(saves)) {
                Map<Integer, List<OrderInfoItem>> itemMap = new ListUtils<OrderInfoItem>().batchList(saves, 3000);
                for (Integer i : itemMap.keySet()) {
                    //数据量太多会mysql报错 分批次插入
                    List<OrderInfoItem> orderInfosItems = itemMap.get(i);
                    orderInfoItemMapper.insertBatch(orderInfosItems);
                }
            }
            log.info("执行完成售单明细表插入条数====================={}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存退货主表数据
     *
     * @param returnOrderInfoExcels
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReturnOrderInfo(List<ReturnOrderInfoExcel> returnOrderInfoExcels) {


        if (CollectionUtils.isNotEmptyCollection(returnOrderInfoExcels)) {
            returnOrderInfoExcels = returnOrderInfoExcels.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ReturnOrderInfoExcel :: getReturnOrderCode))), ArrayList::new));

            List<String> retrunOrderCodeList = returnOrderInfoExcels.stream().map(ReturnOrderInfoExcel::getReturnOrderCode).collect(Collectors.toList());

            //查询出已经在存在的退货信息
            List<String> existReturnOrderCodeeList = this.returnOrderInfoMapper.selectByReturnOrderCodeList(retrunOrderCodeList);
            retrunOrderCodeList = null;
            if (CollectionUtils.isNotEmptyCollection(existReturnOrderCodeeList)) {
                //已经存在退货信息删除
                Iterator<ReturnOrderInfoExcel> iterator = returnOrderInfoExcels.iterator();
                while (iterator.hasNext()) {
                    ReturnOrderInfoExcel next = iterator.next();
                    if (existReturnOrderCodeeList.contains(next.getReturnOrderCode())) {
                        //删除已经存在的
                        iterator.remove();
                    }
                }
            }
            existReturnOrderCodeeList.clear();
            String s = JsonUtil.toJson(returnOrderInfoExcels);
            List<ReturnOrderInfo> saves = JSONObject.parseArray(s, ReturnOrderInfo.class);
            saves.forEach(r -> {
                r.setOrderStatus(11);
                r.setReturnOrderType(4);
                r.setOrderType(1);
                r.setPaymentStatus(0);
                r.setBeLock(0);
                r.setBeDelete(0);
                r.setBeException(0);
                r.setPaymentType("转账");
                r.setBusinessForm(1);
                r.setPlatformType(0);
                r.setOrderProductType(1);
                r.setCompanyCode("09");
                r.setCompanyName("宁波熙耘科技有限公司");
                r.setStoreName(r.getCustomerName());
                r.setOrderOriginalName("爱亲科技");
                if (StringUtils.isNotBlank(r.getCustomerCode())) {
                    r.setStoreCode(r.getCustomerCode());
                }
                // `actual_product_total_amount` decimal(20,4) DEFAULT NULL COMMENT '实际商品分销总金额',
                r.setActualProductTotalAmount(r.getReturnOrderAmount());
                r.setProductTotalAmount(r.getReturnOrderAmount());
                r.setActualProductChannelTotalAmount(r.getReturnOrderAmount());
                r.setProductChannelTotalAmount(r.getActualProductTotalAmount());

                //渠道退货金额 == `product_channel_total_amount` decimal(20,4) DEFAULT NULL COMMENT '渠道总金额',
                r.setProductChannelTotalAmount(r.getReturnOrderAmount());
                //实退 渠道退货金额    `actual_product_channel_total_amount` decimal(20,4) DEFAULT NULL COMMENT '实际渠道总金额',
                r.setActualProductChannelTotalAmount(r.getReturnOrderAmount());



            });
            returnOrderInfoExcels = null;
            if (CollectionUtils.isNotEmptyCollection(saves)) {
                //数据量太多会mysql报错 分批次插入
                Map<Integer, List<ReturnOrderInfo>> itemMap = new ListUtils<ReturnOrderInfo>().batchList(saves, 3000);
                for (Integer i : itemMap.keySet()) {
                    List<ReturnOrderInfo> returnOrderInfos = itemMap.get(i);
                    this.returnOrderInfoMapper.insertMany(returnOrderInfos);
                }
            }
            log.info("执行完成退货单主表插入条数=================================={}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 保存退货单明细数据
     *
     * @param returnOrderInfoItemExcels
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReturnOrderInfoItem(List<ReturnOrderInfoItemExcel> returnOrderInfoItemExcels) {

        if (CollectionUtils.isNotEmptyCollection(returnOrderInfoItemExcels)) {
            String s = JsonUtil.toJson(returnOrderInfoItemExcels);
            List<ReturnOrderInfoItem> saves = JSONObject.parseArray(s, ReturnOrderInfoItem.class);
            returnOrderInfoItemExcels = null;
            //查询出已经存在的明细删除掉
            List<String> returnOrderList = saves.stream().map(ReturnOrderInfoItem::getReturnOrderCode).distinct().collect(Collectors.toList());
            List<String> existReturnOrderCodeList = this.returnOrderInfoItemMapper.selectByReturnOrderList(returnOrderList);
            if (CollectionUtils.isNotEmptyCollection(existReturnOrderCodeList)) {
                Iterator<ReturnOrderInfoItem> iterator = saves.iterator();
                while (iterator.hasNext()) {
                    ReturnOrderInfoItem next = iterator.next();
                    if (existReturnOrderCodeList.contains(next.getReturnOrderCode())) {
                        //删除已经存在的
                        iterator.remove();
                    }
                }

            }
            saves.forEach(r -> {
                r.setCompanyCode("09");
                r.setCompanyName("宁波熙耘科技有限公司");
                r.setChannelUnitPrice(r.getPrice());
                r.setTotalChannelPrice(r.getAmount());
                r.setActualTotalChannelPrice(r.getActualAmount());
            });
            if (CollectionUtils.isNotEmptyCollection(saves)) {
                Map<Integer, List<ReturnOrderInfoItem>> itemMap = new ListUtils<ReturnOrderInfoItem>().batchList(saves, 3000);
                for (Integer i : itemMap.keySet()) {
                    //数据量太多会mysql报错 分批次插入
                    List<ReturnOrderInfoItem> orderInfosItems = itemMap.get(i);
                    returnOrderInfoItemMapper.insertList(orderInfosItems);
                }
            }
            log.info("执行完成退货单明细表插入条数==================================={}", saves.size());
            saves = null;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

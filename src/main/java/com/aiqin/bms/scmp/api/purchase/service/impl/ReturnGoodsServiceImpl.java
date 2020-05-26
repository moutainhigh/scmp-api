package com.aiqin.bms.scmp.api.purchase.service.impl;

import com.aiqin.bms.scmp.api.base.*;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.common.InboundTypeEnum;
import com.aiqin.bms.scmp.api.constant.CommonConstant;
import com.aiqin.bms.scmp.api.product.domain.converter.returnorder.ReturnOrderToInboundConverter;
import com.aiqin.bms.scmp.api.product.domain.dto.returnorder.ReturnOrderInfoDTO;
import com.aiqin.bms.scmp.api.product.domain.pojo.Inbound;
import com.aiqin.bms.scmp.api.product.domain.request.*;
import com.aiqin.bms.scmp.api.product.domain.request.inbound.InboundProductReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.inbound.InboundReqSave;
import com.aiqin.bms.scmp.api.product.domain.request.returngoods.ReturnReceiptReqVO;
import com.aiqin.bms.scmp.api.product.domain.response.ReturnDLResp;
import com.aiqin.bms.scmp.api.product.domain.response.ReturnResp;
import com.aiqin.bms.scmp.api.product.domain.response.inbound.SupplyReturnOrderInfoReqVOReturn;
import com.aiqin.bms.scmp.api.product.domain.response.inbound.SupplyReturnOrderMainReqVOReturn;
import com.aiqin.bms.scmp.api.product.domain.response.inbound.SupplyReturnOrderProductBatchItemReqVOReturn;
import com.aiqin.bms.scmp.api.product.domain.response.inbound.SupplyReturnOrderProductItemReqVOReturn;
import com.aiqin.bms.scmp.api.product.service.InboundService;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseBatch;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfoInspectionItem;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfoItem;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfoLog;
import com.aiqin.bms.scmp.api.purchase.domain.request.order.ChangeOrderStatusReqVO;
import com.aiqin.bms.scmp.api.purchase.domain.request.returngoods.QueryReturnInspectionReqVO;
import com.aiqin.bms.scmp.api.purchase.domain.request.returngoods.QueryReturnOrderManagementReqVO;
import com.aiqin.bms.scmp.api.purchase.domain.request.returngoods.ReturnInspectionReq;
import com.aiqin.bms.scmp.api.purchase.domain.request.returngoods.ReturnOrderInfoReqVO;
import com.aiqin.bms.scmp.api.purchase.domain.response.returngoods.*;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoInspectionItemMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoItemMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoLogMapper;
import com.aiqin.bms.scmp.api.purchase.mapper.ReturnOrderInfoMapper;
import com.aiqin.bms.scmp.api.purchase.service.ReturnGoodsService;
import com.aiqin.bms.scmp.api.supplier.domain.response.warehouse.WarehouseResVo;
import com.aiqin.bms.scmp.api.supplier.service.WarehouseService;
import com.aiqin.bms.scmp.api.util.BeanCopyUtils;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import com.aiqin.bms.scmp.api.util.PageUtil;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import com.aiqin.ground.util.http.HttpClient;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @author: NullPointException
 * @date: 2019-06-13
 * @time: 17:35
 */
@Service
@Slf4j
public class ReturnGoodsServiceImpl extends BaseServiceImpl implements ReturnGoodsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnGoodsServiceImpl.class);

    @Autowired
    private ReturnOrderInfoMapper returnOrderInfoMapper;

    @Autowired
    private ReturnOrderInfoItemMapper returnOrderInfoItemMapper;

    @Autowired
    private UrlConfig urlConfig;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private InboundService inboundService;

    @Autowired
    private ReturnOrderInfoInspectionItemMapper returnOrderInfoInspectionItemMapper;

    @Autowired
    private ReturnOrderInfoLogMapper returnOrderInfoLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean save(List<ReturnOrderInfoReqVO> reqVO) {
        //校验
        validateOrderData(reqVO);
        Date date = new Date();
        //数据处理
        List<ReturnOrderInfo> orders = Lists.newCopyOnWriteArrayList();
        List<ReturnOrderInfoItem> orderItems = Lists.newCopyOnWriteArrayList();
        List<ReturnOrderInfoLog> logs = Lists.newCopyOnWriteArrayList();
        reqVO.parallelStream().forEach(o -> {
            ReturnOrderInfo info = BeanCopyUtils.copy(o, ReturnOrderInfo.class);
            info.setCreateDate(date);
            info.setOperator(CommonConstant.SYSTEM_AUTO);
            info.setOperatorCode(CommonConstant.SYSTEM_AUTO_CODE);
            info.setOperatorTime(date);
            orders.add(info);
            List<ReturnOrderInfoItem> orderItem = BeanCopyUtils.copyList(o.getItemReqVOList(), ReturnOrderInfoItem.class);
            orderItems.addAll(orderItem);
            //拼装日志信息
            ReturnOrderInfoLog log = new ReturnOrderInfoLog(null,info.getReturnOrderCode(),info.getOrderStatus(), ReturnOrderStatus.getAllStatus().get(info.getOrderStatus()).getBackgroundOrderStatus(),ReturnOrderStatus.getAllStatus().get(info.getOrderStatus()).getStandardDescription(),null,info.getOperator(),date,info.getCompanyCode(),info.getCompanyName());
            logs.add(log);
        });
        //保存
        saveData(orderItems, orders);
        //存日志
        saveLog(logs);
        return true;
    }

    @Override
    public void saveLog(List<ReturnOrderInfoLog> logs) {
        if(CollectionUtils.isEmptyCollection(logs)){
            return;
        }
        int i = returnOrderInfoLogMapper.insertBatch(logs);
        if (i != logs.size()) {
            log.info("需要插入订单日志条数[{}]，实际插入订单日志的条数：[{}]",logs.size(),i);
//            throw new BizException(ResultCode.LOG_SAVE_ERROR);
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<ReturnOrderInfoItem> orderItems, List<ReturnOrderInfo> orders) {
        if (CollectionUtils.isNotEmptyCollection(orders)) {
            int i = returnOrderInfoMapper.insertBatch(orders);
            if (i != orderItems.size()) {
                throw new BizException(ResultCode.SAVE_RETURN_ORDER_FAILED);
            }
        }
        if (CollectionUtils.isNotEmptyCollection(orderItems)) {
            int i = returnOrderInfoItemMapper.insertBatch(orderItems);
            if (i != orderItems.size()) {
                throw new BizException(ResultCode.SAVE_RETURN_ORDER_ITEM_FAILED);
            }
        }
    }

    @Override
    public BasePage<QueryReturnOrderManagementRespVO> returnOrderManagement(QueryReturnOrderManagementReqVO reqVO) {
        PageHelper.startPage(reqVO.getPageNo(), reqVO.getPageSize());
        reqVO.setCompanyCode(getUser().getCompanyCode());
        List<QueryReturnOrderManagementRespVO> list = returnOrderInfoMapper.selectReturnOrderManagementList(reqVO);
        return PageUtil.getPageList(reqVO.getPageNo(), list);
    }

    @Override
    public ReturnOrderDetailRespVO returnOrderDetail(String code) {
        ReturnOrderDetailRespVO respVO =  returnOrderInfoMapper.selectReturnOrderDetail(code);
        if(Objects.isNull(respVO)){
            throw new BizException(ResultCode.GET_RETURN_GOODS_DETAIL_FAILED);
        }
        respVO.setInboundList(inboundInfo(code));
        return respVO;
    }
    @Override
    public List<ReturnOrderInfoApplyInboundRespVO> inboundInfo(String code) {
       return returnOrderInfoMapper.selectInbound(code);
    }
    @Override
    public BasePage<QueryReturnInspectionRespVO> returnInspection(QueryReturnInspectionReqVO reqVO) {
        PageHelper.startPage(reqVO.getPageNo(), reqVO.getPageSize());
        List<QueryReturnInspectionRespVO> list = returnOrderInfoMapper.selectreturnInspectionList(reqVO);
        return PageUtil.getPageList(reqVO.getPageNo(), list);
    }

    @Override
    public InspectionDetailRespVO inspectionDetail(String code) {
        //首先查出数据
        InspectionDetailRespVO respVO = returnOrderInfoMapper.selectInspectionDetail(code);
        if(Objects.isNull(respVO)){
            throw new BizException(ResultCode.QUERY_INSPECTION_DETAIL_ERROR);
        }
       List<ReturnOrderInfoInspectionItemRespVO> inspectionItemRespVO =  returnOrderInfoMapper.selectInspectionItemList(code,respVO.getOrderCode());
        //根据仓编码查询下面的库
        List<WarehouseResVo> warehouse = warehouseService.getWarehouseByLogisticsCenterCode(respVO.getTransportCenterCode());
        if(CollectionUtils.isEmptyCollection(warehouse)){
            throw new BizException(ResultCode.DATA_ERROR);
        }
        respVO.setWarehouseResVoList(warehouse);
        Map<Byte, WarehouseResVo> warehouseTypeMap = warehouse.stream().collect(Collectors.toMap(WarehouseResVo::getWarehouseTypeCode, Function.identity(), (k1, k2) -> k1));
        for (ReturnOrderInfoInspectionItemRespVO o : inspectionItemRespVO) {
            o.setOriginalLineNum(o.getProductLineNum().intValue());
            o.setProductLineNum(null);
            //根据批次判断需要入哪个仓
            //首先判断新品/残品
            if (Objects.equals(CommonConstant.NEW_PRODUCT, o.getProductStatus())) {
                o.setWarehouseCode(warehouseTypeMap.get((byte) 1).getWarehouseCode());
            } else if (Objects.equals(CommonConstant.DEFECTIVE, o.getProductStatus())) {
                o.setWarehouseCode(warehouseTypeMap.get((byte) 2).getWarehouseCode());
            } else {
                throw new BizException(ResultCode.DATA_ERROR);
            }
        }
        respVO.setInspectionItemList(inspectionItemRespVO);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveReturnInspection(List<ReturnInspectionReq> reqVO,String remark) {
        //首先保存传过来的数据
        List<ReturnOrderInfoInspectionItem> items = BeanCopyUtils.copyList(reqVO, ReturnOrderInfoInspectionItem.class);
        ReturnOrderInfo info = new ReturnOrderInfo();
        info.setReturnOrderCode(items.get(0).getReturnOrderCode());
        info.setInspectionRemark(remark);
        int i2 = returnOrderInfoMapper.updateByReturnOrderCodeSelective(info);
        if (i2<0){
            throw new BizException(ResultCode.UPDATE_RETURN_ORDER_INFO_FAILED);
        }
        for (int i = 0; i < items.size(); i++) {
            ReturnOrderInfoInspectionItem item = items.get(i);
            item.setProductLineNum((long)i*10);
        }
        int i = returnOrderInfoInspectionItemMapper.insertBatch(items);
        if(i!=items.size()){
            throw new BizException(ResultCode.SAVE_INSPECTION_DATA_FAILED);
        }
        //调用异步方法传入库单信息
        ReturnGoodsServiceImpl returnGoodsService =  (ReturnGoodsServiceImpl)AopContext.currentProxy();
        returnGoodsService.sendToInBound(items);
        return Boolean.TRUE;
    }
    @Override
    @Async("myTaskAsyncPool")
    @Transactional(rollbackFor = Exception.class)
    public void sendToInBound(List<ReturnOrderInfoInspectionItem> items) {
        //TODO 调用入库接口
        //这里会有多个入库单的信息
        List<InboundReqSave> list = dealData(items);
        Boolean b = inboundService.saveList(list);
        if(b){
            //TODO 存日志
            //该状态
        }
    }
    /**
     * 补充数据
     * @author NullPointException
     * @date 2019/6/27
     * @param items
     * @return java.util.List<com.aiqin.bms.scmp.api.product.domain.request.inbound.InboundReqSave>
     */
    private List<InboundReqSave> dealData(List<ReturnOrderInfoInspectionItem> items) {
        if (CollectionUtils.isEmptyCollection(items)) {
            throw new BizException(ResultCode.CAN_NOT_FIND_RETURN_ORDER);
        }
        //查数据
        ReturnOrderInfoDTO dto = returnOrderInfoMapper.selectByCode(items.get(0).getReturnOrderCode());
        if(Objects.isNull(dto)){
            throw new BizException(ResultCode.CAN_NOT_FIND_RETURN_ORDER);
        }
        dto.setItems(items);
        return new ReturnOrderToInboundConverter().convert(dto);
    }

    @Override
    public InspectionViewRespVO inspectionView(String code) {
        return returnOrderInfoMapper.selectInspectionView(code);
    }

    @Override
    public BasePage<QueryReturnOrderManagementRespVO> directReturnOrderManagement(QueryReturnOrderManagementReqVO reqVO) {
        PageHelper.startPage(reqVO.getPageNo(), reqVO.getPageSize());
        List<Integer> orderTypes = Lists.newArrayList();
        orderTypes.add(OrderType.DIRECT_DELIVERY.getNum());
        orderTypes.add(OrderType.DIRECT_DELIVERY_FUCAI.getNum());
        reqVO.setOrderTypeCode(orderTypes);
        reqVO.setCompanyCode(getUser().getCompanyCode());
        List<QueryReturnOrderManagementRespVO> list = returnOrderInfoMapper.selectReturnOrderManagementList(reqVO);
        return PageUtil.getPageList(reqVO.getPageNo(), list);
    }

    @Override
    public ReturnOrderDetailRespVO directReturnOrderDetail(String code) {
        ReturnOrderDetailRespVO respVO =  returnOrderInfoMapper.selectReturnOrderDetail(code);
        if(Objects.isNull(respVO)){
            throw new BizException(ResultCode.GET_RETURN_GOODS_DETAIL_FAILED);
        }
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnReceipt(List<ReturnReceiptReqVO> reqVO, String code) {
        //更新状态
        ChangeOrderStatusReqVO vo = new ChangeOrderStatusReqVO();
        vo.setOperatorCode(getUser().getPersonName());
        vo.setOperatorCode(getUser().getPersonId());
        vo.setOrderCode(code);
        vo.setOrderStatus(ReturnOrderStatus.RETURN_COMPLETED.getStatusCode());
        changeStatus(vo);
        //更新数量
        saveReturnReceipt(reqVO);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(ChangeOrderStatusReqVO reqVO) {
        Date date = new Date();
        //先查后改
        ReturnOrderInfo order = returnOrderInfoMapper.selectByCode1(reqVO.getOrderCode());
        if (Objects.isNull(order)) {
            throw new BizException(ResultCode.CAN_NOT_FIND_ORDER);
        }
        //校验 TODO
        order.setOrderStatus(reqVO.getOrderStatus());
        order.setOperator(reqVO.getOperator());
        order.setOperatorCode(reqVO.getOperatorCode());
        order.setOperatorTime(date);
        order.setRemake(reqVO.getRemark());
        //更新
        updateByOrderCode(order);
        //存日志
        ReturnOrderInfoLog log = new ReturnOrderInfoLog(null,reqVO.getOrderCode(),reqVO.getOrderStatus(), ReturnOrderStatus.getAllStatus().get(reqVO.getOrderStatus()).getBackgroundOrderStatus(),ReturnOrderStatus.getAllStatus().get(reqVO.getOrderStatus()).getStandardDescription(),null,reqVO.getOperator(),date,order.getCompanyCode(),order.getCompanyName());
        List<ReturnOrderInfoLog> logs = Lists.newArrayList();
        logs.add(log);
        saveLog(logs);
        return Boolean.TRUE;
    }

    public void updateByOrderCode(ReturnOrderInfo order) {
       int i =  returnOrderInfoMapper.updateByOrderCode(order);
        if(i<1){
            throw new BizException(ResultCode.UPDATE_ORDER_STATUS_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReturnReceipt(List<ReturnReceiptReqVO> reqVO) {
        int i = returnOrderInfoItemMapper.updateActualInboundNumByIdAndReturnOrderCode(reqVO);
        if (i!=reqVO.size()) {
            throw new BizException(ResultCode.SAVE_RETURN_RECEIPT_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeOrderStatus(String code, Integer status) {
        ChangeOrderStatusReqVO vo = new ChangeOrderStatusReqVO();
        vo.setOperatorCode(getUser().getPersonName());
        vo.setOperatorCode(getUser().getPersonId());
        vo.setOrderCode(code);
        vo.setOrderStatus(status);
        changeStatus(vo);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String record(ReturnReq reqVO) {
        //进行退货单记录
        returnOrderAdd(reqVO);
        //进行入库记录
        InboundReqSave inbound = getInboundReqSave(reqVO);
        //回传入库单编号
        return inboundService.saveInbound2(inbound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recordDL(ReturnDLReq reqVO) {
//        reqVO=test1();
        if(ObjectUtils.equals(null,reqVO)
                ||ObjectUtils.equals(null,reqVO.getReturnOrderInfoDLReq())
                ||CollectionUtils.isEmptyCollection(reqVO.getReturnOrderDetailDLReqList())){
         throw new BizException("有必填项为空");
        }
        //进行主表修改
        ReturnOrderInfoDLReq returnOrderInfoDLReq=reqVO.getReturnOrderInfoDLReq();
        if (ObjectUtils.equals(null,returnOrderInfoMapper.selectByCode(returnOrderInfoDLReq.getReturnOrderCode()))){
            throw new BizException("没有对应退货主单");
        }
        ReturnOrderInfo returnOrderInfo=new ReturnOrderInfo();
        returnOrderInfo.setReturnOrderCode(returnOrderInfoDLReq.getReturnOrderCode());
        returnOrderInfo.setActualProductNum(returnOrderInfoDLReq.getActualProductCount());
        returnOrderInfo.setUpdateById(returnOrderInfoDLReq.getReturnById());
        returnOrderInfo.setUpdateTime(returnOrderInfoDLReq.getReturnTime());
        returnOrderInfoMapper.updateByReturnOrderCodeSelective(returnOrderInfo);
        //进行验证
        List<ReturnOrderInfoItem> returnOrderInfoItems=returnOrderInfoItemMapper.selectByReturnOrderCode(returnOrderInfoDLReq.getReturnOrderCode());
        if(CollectionUtils.isEmptyCollection(returnOrderInfoItems)){
            throw new BizException("没有对应退货商品明细");
        }
        //进行商品修改
        List<ReturnOrderDetailDLReq> returnOrderDetailDLReqList= reqVO.getReturnOrderDetailDLReqList();
        for (ReturnOrderDetailDLReq returnOrderDetailDLReq:
        returnOrderDetailDLReqList  ) {
            ReturnOrderInfoItem returnOrderInfoItem=new ReturnOrderInfoItem();
            returnOrderInfoItem.setActualInboundNum(Math.toIntExact(returnOrderDetailDLReq.getActualReturnProductCount()));
            returnOrderInfoItem.setReturnOrderCode(returnOrderInfoDLReq.getReturnOrderCode());
            returnOrderInfoItem.setSkuCode(returnOrderDetailDLReq.getSkuCode());
            returnOrderInfoItem.setSkuName(returnOrderDetailDLReq.getSkuName());
            returnOrderInfoItem.setProductLineNum(returnOrderDetailDLReq.getLineCode());
            returnOrderInfoItemMapper.updateByReturnOrderCodeSelective(returnOrderInfoItem);
        }
      //进行批次的添加
      if(CollectionUtils.isNotEmptyCollection(reqVO.getReturnBatchDetailDLReqList())){
          for (ReturnBatchDetailDLReq returnBatchDetailDLReq:
          reqVO.getReturnBatchDetailDLReqList()) {
              ReturnOrderInfoInspectionItem returnOrderInfoInspectionItem=new ReturnOrderInfoInspectionItem();
              returnOrderInfoInspectionItem.setReturnOrderCode(returnOrderInfoDLReq.getReturnOrderCode());
              returnOrderInfoInspectionItem.setSkuCode(returnBatchDetailDLReq.getSkuCode());
              returnOrderInfoInspectionItem.setSkuName(returnBatchDetailDLReq.getSkuName());
              returnOrderInfoInspectionItem.setProductLineNum(returnBatchDetailDLReq.getLineCode());
              returnOrderInfoInspectionItem.setBatchCode(String.valueOf(returnBatchDetailDLReq.getBatchNum()));
              returnOrderInfoInspectionItemMapper.insert(returnOrderInfoInspectionItem);
          }

      }
      Boolean isok;
      //发送请求
        if (sendRecordDL(reqVO)){
            log.info("回调成功");
            isok=true;
        }else {
            log.info("回调失败");
            isok=false;
        }
        return isok;
    }

    @Override
    public Boolean recordWMS(SupplyReturnOrderMainReqVOReturn reqVO) {

        // 更新商品信息
        List<SupplyReturnOrderProductItemReqVOReturn> orderItems = reqVO.getOrderItems();
        for (SupplyReturnOrderProductItemReqVOReturn orderItem : orderItems) {
            ReturnOrderInfoItem returnOrderInfoItem = new ReturnOrderInfoItem();
            returnOrderInfoItem.setReturnOrderCode(orderItem.getReturnOrderCode());
            returnOrderInfoItem.setActualInboundNum(orderItem.getReturnNum().intValue());
            returnOrderInfoItem.setSkuCode(orderItem.getSkuCode());
            returnOrderInfoItem.setProductLineNum(orderItem.getLinenum());
            int returnInfoProduct = returnOrderInfoItemMapper.updateByReturnActualNum(returnOrderInfoItem);
            if (returnInfoProduct <= 0) {
                log.info("更新退货单商品表数据sku为"+orderItem.getSkuCode()+"失败");
                throw new GroundRuntimeException("更新退货单商品表数据失败");
            }
        }
        // 更新商品批次信息
        List<SupplyReturnOrderProductBatchItemReqVOReturn> orderBatchItems = reqVO.getOrderBatchItems();
        List<ReturnOrderInfoInspectionItem> returnOrderInfoInspectionItems = new ArrayList<>();
        for (SupplyReturnOrderProductBatchItemReqVOReturn orderBatchItem : orderBatchItems) {
            // 根据批次编号 退货单号确认改批次是否存在
            ReturnOrderInfoInspectionItem returnBatchItem = returnOrderInfoInspectionItemMapper.returnOrderInfo(orderBatchItem.getBatchInfoCode(),orderBatchItem.getReturnOderCode(),orderBatchItem.getLineCode());
            if(returnBatchItem != null){
                returnBatchItem.setActualInboundNum(orderBatchItem.getActualTotalCount().intValue());
                int count = returnOrderInfoInspectionItemMapper.updateByPrimaryKey(returnBatchItem);
                LOGGER.info("变更退货单批次参数：" + JsonUtil.toJson(returnBatchItem)+ "，-条数：", count);
                continue;
            }

            ReturnOrderInfoInspectionItem returnOrderInfoInspectionItem = BeanCopyUtils.copy(orderBatchItem, ReturnOrderInfoInspectionItem.class);
            returnOrderInfoInspectionItem.setReturnOrderCode(orderBatchItem.getReturnOderCode());
            returnOrderInfoInspectionItem.setNum(0L);
            returnOrderInfoInspectionItem.setActualInboundNum(orderBatchItem.getActualTotalCount().intValue());
            returnOrderInfoInspectionItem.setProductLineNum(orderBatchItem.getLineCode().longValue());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try{
                Date parse = simpleDateFormat.parse(orderBatchItem.getProductDate());
                returnOrderInfoInspectionItem.setProductDate(parse);
            }catch (Exception e){
                e.printStackTrace();
            }
            returnOrderInfoInspectionItems.add(returnOrderInfoInspectionItem);
        }
        int count = returnOrderInfoInspectionItemMapper.insertBatch(returnOrderInfoInspectionItems);
        LOGGER.info("添加退货单单批次参数：" + JsonUtil.toJson(returnOrderInfoInspectionItems) + "，-条数：", count);
        // 更新主信息
        SupplyReturnOrderInfoReqVOReturn mainOrderInfo = reqVO.getMainOrderInfo();
        ReturnOrderInfo returnOrderInfo = new ReturnOrderInfo();
        returnOrderInfo.setReturnOrderCode(mainOrderInfo.getReturnOrderCode());
        returnOrderInfo.setCompanyCode(mainOrderInfo.getCompanyCode());
        returnOrderInfo.setCompanyName(mainOrderInfo.getCompanyName());
        returnOrderInfo.setOperator(mainOrderInfo.getOperator());
        returnOrderInfo.setProductNum(mainOrderInfo.getProductNum());
        int returnInfo = returnOrderInfoMapper.updateByReturnOrderCodeSelective(returnOrderInfo);
        if (returnInfo <= 0) {
            log.info("更新退货单主表数据失败");
            throw new GroundRuntimeException("更新退货单主表数据失败");
        }
        return Boolean.TRUE;
    }


    public Boolean sendRecordDL(ReturnDLReq reqVO) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(urlConfig.Order_URL).append("/reject/info");
            HttpClient httpClient = HttpClient.post(String.valueOf(sb)).json(reqVO).timeout(100000);
            HttpResponse<Boolean> response = httpClient.action().result(new TypeReference<HttpResponse<Boolean>>(){});
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new BizException("查询出错");
        }
    }



    private ReturnDLReq test1() {
        ReturnDLReq returnDLReq=new ReturnDLReq();
        ReturnOrderInfoDLReq returnOrderInfoDLReq=new ReturnOrderInfoDLReq();
        returnOrderInfoDLReq.setActualProductCount(1000L);
        returnOrderInfoDLReq.setReturnOrderCode("20191220015100001");
        returnOrderInfoDLReq.setReturnById("12272");
        returnOrderInfoDLReq.setReturnTime(new Date());
        returnDLReq.setReturnOrderInfoDLReq(returnOrderInfoDLReq);

        List<ReturnOrderDetailDLReq> returnOrderDetailDLReqList=Lists.newArrayList();
        ReturnOrderDetailDLReq returnOrderDetailDLReq=new ReturnOrderDetailDLReq();
        returnOrderDetailDLReq.setActualReturnProductCount(Long.valueOf(11));
        returnOrderDetailDLReq.setSkuCode("102423");
        returnOrderDetailDLReq.setSkuName("奶粉");
        returnOrderDetailDLReq.setLineCode(Long.valueOf(1));
        returnOrderDetailDLReqList.add(returnOrderDetailDLReq);
        returnDLReq.setReturnOrderDetailDLReqList(returnOrderDetailDLReqList);


        List<ReturnBatchDetailDLReq> returnBatchDetailDLReqList=Lists.newArrayList();
        ReturnBatchDetailDLReq returnBatchDetailDLReq=new ReturnBatchDetailDLReq();
        returnBatchDetailDLReq.setActualReturnProductCount(Long.valueOf(10));
        returnBatchDetailDLReq.setSkuCode("102423");
        returnBatchDetailDLReq.setSkuName("奶粉");
        returnBatchDetailDLReq.setBatchNum(1);
        returnBatchDetailDLReq.setLineCode(1L);
        returnBatchDetailDLReqList.add(returnBatchDetailDLReq);
        returnDLReq.setReturnBatchDetailDLReqList(returnBatchDetailDLReqList);

        return returnDLReq;
    }

    private void returnOrderAdd(ReturnReq reqVO) {
        //进行主表添加
        ReturnOrderInfoReq returnOrderInfoReq=reqVO.getReturnOrderInfo();
        ReturnOrderInfo returnOrderInfo=new ReturnOrderInfo();
        BeanUtils.copyProperties(returnOrderInfoReq,returnOrderInfo);
        returnOrderInfo.setOrderCode(returnOrderInfoReq.getOrderStoreCode());
        returnOrderInfo.setId(null);
        returnOrderInfoMapper.insert(returnOrderInfo);
        List<ReturnOrderDetailReq> returnOrderDetailReqList=reqVO.getReturnOrderDetailReqList();
        //进行商品书写
        if(CollectionUtils.isEmptyCollection(returnOrderDetailReqList)){
            throw  new BizException("集合为空");
        }
        for (ReturnOrderDetailReq returnOrderDetailReq:
        returnOrderDetailReqList ) {
            ReturnOrderInfoItem returnOrderInfoItem=new ReturnOrderInfoItem();
            returnOrderInfoItem.setReturnOrderCode(returnOrderDetailReq.getReturnOrderCode());
            returnOrderInfoItem.setSkuCode(returnOrderDetailReq.getSkuCode());
            returnOrderInfoItem.setSkuName(returnOrderDetailReq.getSkuName());
            returnOrderInfoItem.setPictureUrl(returnOrderDetailReq.getPictureUrl());
            returnOrderInfoItem.setColorCode(returnOrderDetailReq.getColorCode());
            returnOrderInfoItem.setColorName(returnOrderDetailReq.getColorName());
            returnOrderInfoItem.setModelCode(returnOrderDetailReq.getModelCode());
            returnOrderInfoItem.setUnitCode(returnOrderDetailReq.getUnitCode());
            returnOrderInfoItem.setUnitName(returnOrderDetailReq.getUnitName());
            returnOrderInfoItem.setZeroDisassemblyCoefficient(Math.toIntExact(returnOrderDetailReq.getZeroDisassemblyCoefficient()));
            returnOrderInfoItem.setPrice(returnOrderDetailReq.getProductAmount());
            returnOrderInfoItem.setNum(returnOrderDetailReq.getReturnProductCount());
            returnOrderInfoItem.setAmount(returnOrderDetailReq.getTotalProductAmount());
            returnOrderInfoItem.setActivityCode(returnOrderDetailReq.getActivityCode());
            returnOrderInfoItem.setProductLineNum(returnOrderDetailReq.getLineCode());
            returnOrderInfoItem.setProductStatus(returnOrderDetailReq.getProductStatus());
            returnOrderInfoItem.setActualInboundNum(Math.toIntExact(returnOrderDetailReq.getActualReturnProductCount()));
            returnOrderInfoItem.setCompanyCode(returnOrderInfoReq.getCompanyCode());
            returnOrderInfoItem.setCompanyName(returnOrderInfoReq.getCompanyName());
            returnOrderInfoItem.setChannelUnitPrice(returnOrderDetailReq.getProductAmount());
            returnOrderInfoItem.setActualChannelUnitPrice(returnOrderDetailReq.getProductAmount());
            returnOrderInfoItem.setActualAmount(returnOrderDetailReq.getProductAmount());
            returnOrderInfoItem.setActualPrice(returnOrderDetailReq.getTotalProductAmount());
            returnOrderInfoItem.setWarehouseCode(returnOrderInfoReq.getWarehouseCode());
            returnOrderInfoItem.setWarehouseName(returnOrderInfoReq.getWarehouseName());
            returnOrderInfoItem.setSupplyCode(returnOrderInfoReq.getSupplierCode());
            returnOrderInfoItem.setSupplyName(returnOrderInfoReq.getSupplierName());
            returnOrderInfoItem.setTax(returnOrderDetailReq.getTaxRate());
            returnOrderInfoItemMapper.insert(returnOrderInfoItem);

        }
        //书写日志
        ReturnOrderInfoLog returnOrderInfoLog=new ReturnOrderInfoLog(null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        returnOrderInfoLog.setOrderCode(returnOrderInfoReq.getReturnOrderCode());
        returnOrderInfoLog.setStatus(Integer.valueOf(InOutStatus.CREATE_INOUT.getCode()));
        returnOrderInfoLog.setStatusDesc(InOutStatus.CREATE_INOUT.getName());
        returnOrderInfoLog.setRemark(returnOrderInfoReq.getRemark());
        returnOrderInfoLog.setOperator(returnOrderInfoReq.getCreateByName());
        returnOrderInfoLog.setOperatorTime(returnOrderInfoReq.getCreateTime());
        returnOrderInfoLog.setCompanyCode(returnOrderInfoReq.getCompanyCode());
        returnOrderInfoLog.setCompanyName(returnOrderInfoReq.getCompanyName());
        returnOrderInfoLogMapper.insert(returnOrderInfoLog);
    }

    private InboundReqSave getInboundReqSave(ReturnReq reqVO) {
        InboundReqSave inbound=new InboundReqSave();
        ReturnOrderInfoReq returnOrderInfo=reqVO.getReturnOrderInfo();
        inbound.setCompanyCode(returnOrderInfo.getCompanyCode());
        inbound.setCompanyName(returnOrderInfo.getCompanyName());
        //进行第一次状态
        inbound.setInboundStatusCode(InOutStatus.CREATE_INOUT.getCode());
        inbound.setInboundStatusName(InOutStatus.CREATE_INOUT.getName());
        // 进行退货设置
        inbound.setInboundTypeCode(InboundTypeEnum.ORDER.getCode());
        inbound.setInboundTypeName(InboundTypeEnum.ORDER.getName());
        inbound.setSourceOderCode(returnOrderInfo.getReturnOrderCode());
        inbound.setInboundTime(new Date());
        inbound.setLogisticsCenterCode(returnOrderInfo.getTransportCenterCode());
        inbound.setLogisticsCenterName(returnOrderInfo.getTransportCenterName());
        inbound.setWarehouseCode(returnOrderInfo.getWarehouseCode());
        inbound.setWarehouseName(returnOrderInfo.getWarehouseName());
        inbound.setSupplierCode(returnOrderInfo.getSupplierCode());
        inbound.setSupplierName(returnOrderInfo.getSupplierName());
        //预计到货时间?
        inbound.setPreArrivalTime(null);
        //预计入库数量？
        inbound.setPreInboundNum(returnOrderInfo.getActualProductCount());
        //预计入库数量？
        inbound.setPraInboundNum(returnOrderInfo.getActualProductCount());
        //预计主单位数量?
        inbound.setPreMainUnitNum(returnOrderInfo.getActualProductCount());
        inbound.setPreTaxAmount(returnOrderInfo.getReturnOrderAmount());
        inbound.setPraAmount(returnOrderInfo.getReturnOrderAmount());
        //省编码
        inbound.setProvinceCode(returnOrderInfo.getProvinceId());
        inbound.setProvinceName(returnOrderInfo.getProvinceName());
        //城市
        inbound.setCityCode(returnOrderInfo.getCityId());
        inbound.setCityName(returnOrderInfo.getCityName());
        //区
        inbound.setCountyCode(returnOrderInfo.getDistrictId());
        inbound.setCountyName(returnOrderInfo.getDistrictName());
        //详细地址
        inbound.setDetailedAddress(returnOrderInfo.getReceiveAddress());
        //创建人
        inbound.setCreateBy(returnOrderInfo.getCreateByName());
        inbound.setCreateTime(returnOrderInfo.getCreateTime());
        //修改人
        inbound.setUpdateBy(returnOrderInfo.getUpdateByName());
        inbound.setUpdateTime(returnOrderInfo.getUpdateTime());

        if(CollectionUtils.isEmptyCollection(reqVO.getReturnOrderDetailReqList())){
         throw new BizException("传输列表详情为空");
        }
        //进行商品设置
        List<InboundProductReqVo> list= Lists.newArrayList();
        for (ReturnOrderDetailReq returnOrderDetailReq:
        reqVO.getReturnOrderDetailReqList()) {
            InboundProductReqVo inboundProductReqVo=new InboundProductReqVo();
            inboundProductReqVo.setInboundOderCode(returnOrderDetailReq.getReturnOrderDetailId());
            inboundProductReqVo.setSkuCode(returnOrderDetailReq.getSkuCode());
            inboundProductReqVo.setSkuName(returnOrderDetailReq.getSkuName());
            inboundProductReqVo.setPictureUrl(returnOrderDetailReq.getPictureUrl());
            inboundProductReqVo.setNorms(returnOrderDetailReq.getProductSpec());
            inboundProductReqVo.setColorCode(returnOrderDetailReq.getColorCode());
            inboundProductReqVo.setColorName(returnOrderDetailReq.getColorName());
            inboundProductReqVo.setModel(returnOrderDetailReq.getModelCode());
            inboundProductReqVo.setUnitCode(returnOrderDetailReq.getUnitCode());
            inboundProductReqVo.setUnitName(returnOrderDetailReq.getUnitName());
            inboundProductReqVo.setInboundNorms(returnOrderDetailReq.getModelCode());
            inboundProductReqVo.setInboundBaseUnit(String.valueOf(returnOrderDetailReq.getZeroDisassemblyCoefficient()));
            inboundProductReqVo.setPreInboundNum(returnOrderDetailReq.getReturnProductCount());
            inboundProductReqVo.setPreInboundMainNum(returnOrderDetailReq.getReturnProductCount());
            inboundProductReqVo.setPreTaxPurchaseAmount(returnOrderDetailReq.getProductAmount());
            inboundProductReqVo.setPreTaxAmount(returnOrderDetailReq.getTotalProductAmount());
            inboundProductReqVo.setPraInboundNum(returnOrderDetailReq.getReturnProductCount());
            inboundProductReqVo.setPraInboundMainNum(returnOrderDetailReq.getReturnProductCount());
            inboundProductReqVo.setPraTaxPurchaseAmount(returnOrderDetailReq.getProductAmount());
            inboundProductReqVo.setPraTaxAmount(returnOrderDetailReq.getTotalProductAmount());
            inboundProductReqVo.setCreateBy(returnOrderDetailReq.getCreateByName());
            inboundProductReqVo.setCreateTime(returnOrderDetailReq.getCreateTime());
            inboundProductReqVo.setUpdateBy(returnOrderDetailReq.getUpdateByName());
            inboundProductReqVo.setUpdateTime(returnOrderDetailReq.getUpdateTime());
            inboundProductReqVo.setSupplyCode(inboundProductReqVo.getSupplyCode());
            inboundProductReqVo.setSupplyName(inboundProductReqVo.getSupplyName());
            list.add(inboundProductReqVo);
        }
        inbound.setList(list);

        return inbound;
    }


    /**
     * 参数验证
     *
     * @param reqVO
     * @return void
     * @author NullPointException
     * @date 2019/6/19
     */
    private void validateOrderData(List<ReturnOrderInfoReqVO> reqVO) {

    }
}

package com.aiqin.bms.scmp.api.product.service.impl;

import com.aiqin.bms.scmp.api.base.EncodingRuleType;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.common.Save;
import com.aiqin.bms.scmp.api.common.SaveList;
import com.aiqin.bms.scmp.api.common.SupervisoryWarehouseOrderTypeEnum;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.dao.ProductSkuPicturesDao;
import com.aiqin.bms.scmp.api.product.domain.converter.supervisorywarehouseorder.WarehouseOrderToInboundConverter;
import com.aiqin.bms.scmp.api.product.domain.converter.supervisorywarehouseorder.WarehouseOrderToOutboundConverter;
import com.aiqin.bms.scmp.api.product.domain.pojo.SupervisoryWarehouseOrder;
import com.aiqin.bms.scmp.api.product.domain.pojo.SupervisoryWarehouseOrderProduct;
import com.aiqin.bms.scmp.api.product.domain.request.supervisory.SaveSupervisoryWarehouseOrderReqVo;
import com.aiqin.bms.scmp.api.product.mapper.SupervisoryWarehouseOrderMapper;
import com.aiqin.bms.scmp.api.product.mapper.SupervisoryWarehouseOrderProductMapper;
import com.aiqin.bms.scmp.api.product.service.InboundService;
import com.aiqin.bms.scmp.api.product.service.OutboundService;
import com.aiqin.bms.scmp.api.product.service.SupervisoryWarehouseOrderService;
import com.aiqin.bms.scmp.api.supplier.domain.response.warehouse.WarehouseResVo;
import com.aiqin.bms.scmp.api.supplier.service.WarehouseService;
import com.aiqin.bms.scmp.api.util.BeanCopyUtils;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author knight.xie
 * @className SupervisoryWarehouseOrderServiceImpl
 * @date 2019/6/29 17:47

 * @version 1.0
 */
@Service
public class SupervisoryWarehouseOrderServiceImpl extends BaseServiceImpl implements SupervisoryWarehouseOrderService {

    @Autowired
    private SupervisoryWarehouseOrderMapper mapper;

    @Autowired
    private SupervisoryWarehouseOrderProductMapper productMapper;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private InboundService inboundService;

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private ProductSkuPicturesDao productSkuPicturesDao;

    /**
     * 功能描述: 保存
     * @param reqVo
     * @return
     * @auther knight.xie
     * @date 2019/6/29 18:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer save(SaveSupervisoryWarehouseOrderReqVo reqVo) {
        SupervisoryWarehouseOrder order = new SupervisoryWarehouseOrder();
        BeanCopyUtils.copy(reqVo,order);
        if(CollectionUtils.isEmptyCollection(reqVo.getProducts())){
            throw new BizException(ResultCode.PRODUCT_NO_EXISTS);
        }
        //查询仓库下面是否存在监管仓
        List<WarehouseResVo> warehouseResVos = warehouseService.getWarehouseTypeByLogisticsCenterCode(order.getTransportCenterCode(), Global.SUPERVISORY_WAREHOUSE_TYPE);
        if(CollectionUtils.isEmptyCollection(warehouseResVos)){
            throw new BizException(ResultCode.SUPERVISORY_WAREHOUSE_NOT_EXISTS);
        }
        if (Objects.isNull(reqVo.getOrderType())) {
            throw new BizException(ResultCode.ORDER_TYPE_EMPTY);
        }
        order.setWarehouseCode(warehouseResVos.get(0).getWarehouseCode());
        order.setWarehouseName(warehouseResVos.get(0).getWarehouseName());
        synchronized (SupervisoryWarehouseOrderServiceImpl.class){
            order.setOrderCode(getCode("JG", EncodingRuleType.SUPERVISORY_WAREHOUSE_ORDER_CODE));
        }
        //获取订单类型名称
        SupervisoryWarehouseOrderTypeEnum typeName = SupervisoryWarehouseOrderTypeEnum.getName(order.getOrderType());
        if(Objects.isNull(typeName)){
            throw new BizException(ResultCode.ORDER_TYPE_ERROR);
        }
        order.setOrderTypeName(typeName.getName());
        //保存订单信息
        int insert = ((SupervisoryWarehouseOrderService) AopContext.currentProxy()).insert(order);
        List<SupervisoryWarehouseOrderProduct> records = BeanCopyUtils.copyList(reqVo.getProducts(),SupervisoryWarehouseOrderProduct.class);
        AtomicInteger i = new AtomicInteger();
        records.forEach(item->{
            item.setOrderCode(order.getOrderCode());
            item.setOrderTypeName(typeName.getName());
            item.setOrderType(order.getOrderType());
            if(Objects.isNull(item.getProductTotalAmount())){
                item.setProductTotalAmount(item.getProductAmount().multiply(BigDecimal.valueOf(item.getNum())).setScale(4, BigDecimal.ROUND_HALF_UP));
            }
            item.setLineNum(i.getAndIncrement()*10);
        });
        //保存商品信息
        ((SupervisoryWarehouseOrderService)AopContext.currentProxy()).insertBatchProduct(records);
        order.setRecords(records);
        if (Objects.equals(SupervisoryWarehouseOrderTypeEnum.INBOUND, typeName)) {
            inboundService.saveInbound(new WarehouseOrderToInboundConverter(productSkuPicturesDao).convert(order));
        } else {
            //TODO 锁库
            outboundService.save(new WarehouseOrderToOutboundConverter(productSkuPicturesDao).convert(order));
        }
        return insert;
    }

    /**
     * 功能描述: 插入数据库
     *
     * @param record
     * @return
     * @auther knight.xie
     * @date 2019/6/29 18:16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Save
    public int insert(SupervisoryWarehouseOrder record) {
        record.setCompanyCode(getUser().getCompanyCode());
        record.setCompanyName(getUser().getCompanyName());
        return mapper.insertSelective(record);
    }

    /**
     * 功能描述: 批量插入商品
     *
     * @param records
     * @return
     * @auther knight.xie
     * @date 2019/6/29 18:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @SaveList
    public int insertBatchProduct(List<SupervisoryWarehouseOrderProduct> records) {
        return productMapper.insertBatch(records);
    }
}

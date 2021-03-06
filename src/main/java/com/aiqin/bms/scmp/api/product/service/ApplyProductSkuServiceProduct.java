package com.aiqin.bms.scmp.api.product.service;

import com.aiqin.bms.scmp.api.base.service.BaseService;
import com.aiqin.bms.scmp.api.product.domain.pojo.ApplyProductSku;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowCallbackVO;

import java.util.List;

/**
 * @author knight.xie
 * @version 1.0
 * @className ApplyProductSkuServiceProduct
 * @date 2019/3/29 16:04

 */
public interface ApplyProductSkuServiceProduct extends BaseService {

    /**
     * 通过sku编码集合和申请状态查询sku列表信息
     * @author zth
     * @date 2019/4/4
     * @param skuCodes 编码集合
     * @param number 状态
     * @return java.util.List<com.aiqin.mgs.product.api.domain.pojo.ApplyProductSku>
     */
    List<ApplyProductSku> getProductApplyList(List<String> skuCodes, Byte number);
}

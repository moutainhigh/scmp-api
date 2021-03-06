package com.aiqin.bms.scmp.api.supplier.domain.request.rule;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author knight.xie
 * @version 1.0
 * @className SaveReqVo
 * @date 2019/5/23 10:57

 */
@ApiModel("保存规则Vo")
@Data
public class SaveReqVo {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("自动退货天数")
    private Integer autoReturnGoodsDay;

    @ApiModelProperty("订单体积计算系数")
    private BigDecimal orderVolumeCoefficient;

    @ApiModelProperty("订单重量计算系数")
    private BigDecimal orderWeightCoefficient;

    @ApiModelProperty("采购流程天数")
    private int purchaseProcessDay;

    @ApiModelProperty("采购流程审核天数")
    private int purchaseProcessReviewDay;

    @ApiModelProperty("采购流程财务付款天数")
    private int purchaseProcessPaymentDay;

    @ApiModelProperty("采购流程供应商确认天数")
    private int purchaseProcessSupplierConfirmDay;
}

package com.aiqin.bms.scmp.api.purchase.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: zhao shuai
 * @create: 2019-08-08
 **/
@Data
public class PurchaseNewContrastResponse {

    @ApiModelProperty(value="采购前_预计营业额")
    @JsonProperty("front_turnover")
    private BigDecimal frontTurnover;

    @ApiModelProperty(value="采购前_采购成本")
    @JsonProperty("front_purchase_cost")
    private BigDecimal frontPurchaseCost;

    @ApiModelProperty(value="采购前_毛利额")
    @JsonProperty("front_gross_profit")
    private BigDecimal frontGrossProfit;

//    @ApiModelProperty(value="采购前_毛利率")
//    @JsonProperty("front_gross_rate")
//    private Long frontGrossRate;

    @ApiModelProperty(value="采购后_预计营业额")
    @JsonProperty("after_turnover")
    private BigDecimal afterTurnover;

    @ApiModelProperty(value="采购后_采购成本")
    @JsonProperty("after_purchase_cost")
    private BigDecimal afterPurchaseCost;

    @ApiModelProperty(value="采购后_毛利额")
    @JsonProperty("after_gross_profit")
    private BigDecimal afterGrossProfit;

//    @ApiModelProperty(value="采购后_毛利率")
//    @JsonProperty("after_gross_rate")
//    private Long afterGrossRate;

    @ApiModelProperty(value="采购前_滞销sku数")
    @JsonProperty("front_unsalable_sku")
    private Integer frontUnsalableSku;

//    @ApiModelProperty(value="采购前_滞销率")
//    @JsonProperty("front_unsalable_rate")
//    private Long frontUnsalableRate;

    @ApiModelProperty(value="采购后_滞销sku数")
    @JsonProperty("after_unsalable_sku")
    private Integer afterUnsalableSku;

//    @ApiModelProperty(value="采购前_滞销率")
//    @JsonProperty("after_unsalable_rate")
//    private Long afterUnsalableRate;

    @ApiModelProperty(value="采购前_缺货数")
    @JsonProperty("front_shortage_count")
    private Integer frontShortageCount;

    @ApiModelProperty(value="采购后_缺货数")
    @JsonProperty("after_shortage_count")
    private Integer afterShortageCount;

    @ApiModelProperty(value="总sku数")
    @JsonProperty("sku_sum")
    private Integer skuSum;
}

package com.aiqin.bms.scmp.api.bireport.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ApiModel("负毛利respVo")
@Data
public class NegativeMarginRespVo {

    @ApiModelProperty("主键")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty("渠道编码")
    @JsonProperty("order_code")
    private String orderCode;

    @ApiModelProperty("渠道名称")
    @JsonProperty("order_original")
    private String orderOriginal;

    @ApiModelProperty("sku编码")
    @JsonProperty("sku_code")
    private String skuCode;

    @ApiModelProperty("sku名称")
    @JsonProperty("sku_name")
    private String skuName;

    @ApiModelProperty("品类编码")
    @JsonProperty("category_code")
    private String categoryCode;

    @ApiModelProperty("品类名称")
    @JsonProperty("category_name")
    private String categoryName;

    @ApiModelProperty("品牌编码")
    @JsonProperty("product_brand_code")
    private String productBrandCode;

    @ApiModelProperty("品牌")
    @JsonProperty("product_brand_name")
    private String productBrandName;

    @ApiModelProperty("销售数量")
    @JsonProperty("product_num")
    private Long productNum;

    @ApiModelProperty("销售成本")
    @JsonProperty("sales_cost")
    private BigDecimal salesCost;

    @ApiModelProperty("渠道销售金额")
    @JsonProperty("channel_order_amount")
    private BigDecimal channelOrderAmount;

    @ApiModelProperty("渠道毛利额")
    @JsonProperty("channel_maori")
    private BigDecimal channelMaori;

    @ApiModelProperty("渠道毛利率")
    @JsonProperty("channel_maori_rate")
    private BigDecimal channelMaoriRate;

    @ApiModelProperty("分销销售金额")
    @JsonProperty("distribution_order_amount")
    private BigDecimal distributionOrderAmount;

    @ApiModelProperty("分销毛利额")
    @JsonProperty("distribution_maori")
    private BigDecimal distributionMaori;

    @ApiModelProperty("分销毛利率")
    @JsonProperty("distribution_maori_rate")
    private BigDecimal distributionMaoriRate;

    @ApiModelProperty("所属部门编码")
    @JsonProperty("product_sort_code")
    private String productSortCode;

    @ApiModelProperty("所属部门")
    @JsonProperty("product_sort_name")
    private String productSortName;

    @ApiModelProperty("入库时间")
    @JsonProperty("put_storage_create_time")
    private String putStorageCreateTime;

    @ApiModelProperty("计算时间")
    @JsonProperty("create_time")
    private String createTime;

    @ApiModelProperty("销售数量合计")
    @JsonProperty("product_nums")
    private Long productNums;

    @ApiModelProperty("销售成本合计")
    @JsonProperty("sales_costs")
    private BigDecimal salesCosts;

    @ApiModelProperty("渠道销售金额合计")
    @JsonProperty("channel_order_amounts")
    private BigDecimal channelOrderAmounts;

    @ApiModelProperty("渠道毛利额合计")
    @JsonProperty("channel_maoris")
    private BigDecimal channelMaoris;

    @ApiModelProperty("渠道毛利率合计")
    @JsonProperty("channel_maori_rates")
    private BigDecimal channelMaoriRates;

    @ApiModelProperty("分销销售金额合计")
    @JsonProperty("distribution_order_amounts")
    private BigDecimal distributionOrderAmounts;

    @ApiModelProperty("分销毛利额合计")
    @JsonProperty("distribution_maoris")
    private BigDecimal distributionMaoris;

    @ApiModelProperty("分销毛利率合计")
    @JsonProperty("distribution_maori_rates")
    private BigDecimal distributionMaoriRates;

    @ApiModelProperty("返回列名")
    @JsonProperty("column_list")
    private List<Map> columnList;

}

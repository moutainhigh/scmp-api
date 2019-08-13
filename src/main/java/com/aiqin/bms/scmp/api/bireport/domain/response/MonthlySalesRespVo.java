package com.aiqin.bms.scmp.api.bireport.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@ApiModel("月销售情况respVo")
@Data
public class MonthlySalesRespVo {

    @ApiModelProperty("主键")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty("年月")
    @JsonProperty("year_month")
    private String yearMonth;

    @ApiModelProperty("销售成本")
    @JsonProperty("sales_cost")
    private String salesCost;

    @ApiModelProperty("渠道编码")
    @JsonProperty("price_channel_code")
    private String priceChannelCode;

    @ApiModelProperty("渠道")
    @JsonProperty("price_channel_name")
    private String priceChannelName;

    @ApiModelProperty("所属部门编码")
    @JsonProperty("product_sort_code")
    private String productSortCode;

    @ApiModelProperty("所属部门")
    @JsonProperty("product_sort_name")
    private String productSortName;

    @ApiModelProperty("商品属性编码(ab品)")
    @JsonProperty("product_property_code")
    private String productPropertyCode;

    @ApiModelProperty("商品属性名称(ab品)")
    @JsonProperty("product_property_name")
    private String productPropertyName;

    @ApiModelProperty("门店类型code")
    @JsonProperty("store_type_code")
    private String storeTypeCode;

    @ApiModelProperty("门店类型名称")
    @JsonProperty("store_type")
    private String storeType;

    @ApiModelProperty("数据类型code")
    @JsonProperty("data_type_code")
    private String dataTypeCode;

    @ApiModelProperty("数据类型name")
    @JsonProperty("data_type")
    private String dataType;

    @ApiModelProperty("渠道销售额")
    @JsonProperty("channel_order_amount")
    private Long channelOrderAmount;

    @ApiModelProperty("去年同期渠道销售额")
    @JsonProperty("channel_last_year_sales")
    private Long channelLastYearSales;

    @ApiModelProperty("上月渠道销售额")
    @JsonProperty("channel_last_month_sales")
    private Long channelLastMonthSales;

    @ApiModelProperty("渠道同比")
    @JsonProperty("channel_compared_same")
    private Double channelComparedSame;

    @ApiModelProperty("渠道环比")
    @JsonProperty("channel_sequential")
    private Double channelSequential;

    @ApiModelProperty("分销销售额")
    @JsonProperty("distribution_order_amount")
    private Long distributionOrderAmount;

    @ApiModelProperty("分销去年同期销售额")
    @JsonProperty("distribution_last_year_sales")
    private Long distributionLastYearSales;

    @ApiModelProperty("分销上月销售额")
    @JsonProperty("distribution_last_month_sales")
    private Long distributionLastMonthSales;

    @ApiModelProperty("分销同比")
    @JsonProperty("distribution_compared_same")
    private Double distributionComparedSame;

    @ApiModelProperty("分销环比")
    @JsonProperty("distribution_sequential")
    private Double distributionSequential;

    @ApiModelProperty("渠道毛利")
    @JsonProperty("channel_margin")
    private Double channelMargin;

    @ApiModelProperty("分销毛利")
    @JsonProperty("distribution_margin")
    private Double distributionMargin;

    @ApiModelProperty("渠道毛利率")
    @JsonProperty("channel_margin_rate")
    private Double channelMarginRate;

    @ApiModelProperty("分销毛利率")
    @JsonProperty("distribution_margin_rate")
    private Double distributionMarginRate;

    @ApiModelProperty("渠道毛利环比")
    @JsonProperty("channel_margin_sequential")
    private Double channelMarginSequential;

    @ApiModelProperty("分销毛利环比")
    @JsonProperty("distribution_margin_sequential")
    private Double distributionMarginSequential;

    @ApiModelProperty("渠道毛利同比")
    @JsonProperty("channel_margin_compared_same")
    private Double channelMarginComparedSame;

    @ApiModelProperty("分销毛利同比")
    @JsonProperty("distribution_margin_compared_same")
    private Double distributionMarginComparedSame;

    @ApiModelProperty("渠道毛利率环比")
    @JsonProperty("channel_margin_rate_sequential")
    private Double channelMarginRateSequential;

    @ApiModelProperty("渠道毛利率同比")
    @JsonProperty("channel_margin_rate_compared_same")
    private Double channelMarginRateComparedSame;

    @ApiModelProperty("分销毛利率同比")
    @JsonProperty("distribution_margin_rate_compared_same")
    private Double distributionMarginRateComparedSame;

    @ApiModelProperty("分销毛利率环比")
    @JsonProperty("distribution_margin_rate_sequential")
    private Double distributionMarginRateSequential;

    @ApiModelProperty("渠道预算")
    @JsonProperty("channel_budget")
    private Double channelBudget;

    @ApiModelProperty("渠道达成率")
    @JsonProperty("channel_achievement")
    private Double channelAchievement;

    @ApiModelProperty("分销预算")
    @JsonProperty("distribution_budget")
    private Double distributionBudget;

    @ApiModelProperty("分销达成率")
    @JsonProperty("distribution_achievement")
    private Double distributionAchievement;

    @ApiModelProperty("计算时间")
    @JsonProperty("create_time")
    private String createTime;

    @ApiModelProperty("返回列名")
    @JsonProperty("column_list")
    private List<Map> columnList;

}

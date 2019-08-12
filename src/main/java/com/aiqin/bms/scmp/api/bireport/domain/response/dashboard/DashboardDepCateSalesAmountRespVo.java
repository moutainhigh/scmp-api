package com.aiqin.bms.scmp.api.bireport.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("当月各部门品类下的销售情况respVo")
@Data
public class DashboardDepCateSalesAmountRespVo {

    @ApiModelProperty("主键")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty("月")
    @JsonProperty("stat_month")
    private String statMonth;

    @ApiModelProperty("年")
    @JsonProperty("stat_year")
    private String statYear;

    @ApiModelProperty("部门编码")
    @JsonProperty("product_sort_code")
    private String productSortCode;

    @ApiModelProperty("部门")
    @JsonProperty("product_sort_name")
    private String productSortName;

    @ApiModelProperty("品类编码")
    @JsonProperty("product_category_code")
    private String productCategoryCode;

    @ApiModelProperty("品类名")
    @JsonProperty("product_category_name")
    private String productCategoryName;

    @ApiModelProperty("渠道成本")
    @JsonProperty("channel_costs")
    private Long channelCosts;

    @ApiModelProperty("渠道销售额")
    @JsonProperty("channel_sales_amount")
    private Long channelSalesAmount;

    @ApiModelProperty("渠道销售同比")
    @JsonProperty("channel_sales_yearonyear")
    private Long channelSalesYearonyear;

    @ApiModelProperty("渠道销售环比")
    @JsonProperty("channel_sales_link_relative")
    private Long channelSalesLinkRelative;

    @ApiModelProperty("分销销售额")
    @JsonProperty("distribution_sales_amount")
    private Long distributionSalesAmount;

    @ApiModelProperty("渠道毛利")
    @JsonProperty("channel_margin")
    private Long channelMargin;

    @ApiModelProperty("渠道毛利同比")
    @JsonProperty("channel_margin_yearonyear")
    private Long channelMarginYearonyear;

    @ApiModelProperty("渠道毛利环比")
    @JsonProperty("channel_margin_link_relative")
    private Long channelMarginLinkRelative;

    @ApiModelProperty("分销毛利")
    @JsonProperty("distribution_margin")
    private Long distributionMargin;


}

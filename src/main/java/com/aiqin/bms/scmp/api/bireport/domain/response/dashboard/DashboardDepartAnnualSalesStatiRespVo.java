package com.aiqin.bms.scmp.api.bireport.domain.response.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("年销售情况(部门)respVo")
@Data
public class DashboardDepartAnnualSalesStatiRespVo {

    @ApiModelProperty("主键")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty("年份")
    @JsonProperty("stat_year")
    private String statYear;

    @ApiModelProperty("部门")
    @JsonProperty("product_sort_name")
    private String productSortName;

    @ApiModelProperty("渠道成本")
    @JsonProperty("channel_costs")
    private Long channelCosts;

    @ApiModelProperty("渠道销售额")
    @JsonProperty("channel_amount")
    private Long channelAmount;

    @ApiModelProperty("分销销售额")
    @JsonProperty("distribution_amount")
    private Long distributionAmount;

    @ApiModelProperty("渠道毛利")
    @JsonProperty("channel_margin")
    private Long channelMargin;

    @ApiModelProperty("分销毛利")
    @JsonProperty("distribution_margin")
    private Long distributionMargin;

    @ApiModelProperty("销售预算")
    @JsonProperty("sales_budget")
    private Long salesBudget;

    @ApiModelProperty("销售达成率")
    @JsonProperty("achieve_rate")
    private Double achieveRate;

}

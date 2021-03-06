package com.aiqin.bms.scmp.api.statistics.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: zhao shuai
 * @create: 2019-09-09
 **/
@Data
public class SupplierRequest {

    @ApiModelProperty(value="日期")
    @JsonProperty("date")
    private String date;

    @ApiModelProperty(value="年")
    @JsonProperty("year")
    private Long year;

    @ApiModelProperty(value="月")
    @JsonProperty("month")
    private Long month;

    @ApiModelProperty(value="报表类型: 0 年报 2 月报")
    @JsonProperty("report_type")
    private Integer reportType;

    @ApiModelProperty(value = "所属部门")
    @JsonProperty("product_sort_code")
    private String productSortCode;

    @ApiModelProperty(value="供应商code")
    @JsonProperty("supplier_code")
    private String supplierCode;

    @ApiModelProperty(value="一级品类code")
    @JsonProperty("lv1")
    private String lv1;

    public SupplierRequest(String date, Integer reportType, String productSortCode) {
        this.date = date;
        this.reportType = reportType;
        this.productSortCode = productSortCode;
    }
}

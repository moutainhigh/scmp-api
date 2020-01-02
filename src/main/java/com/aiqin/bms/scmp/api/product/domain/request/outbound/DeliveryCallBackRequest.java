package com.aiqin.bms.scmp.api.product.domain.request.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author: zhao shuai
 * @create: 2019-12-31
 **/
@Data
@Api("发运单信息")
public class DeliveryCallBackRequest {

    @ApiModelProperty("发运单号")
    @NotEmpty(message = "发运单号不能为空")
    @JsonProperty("delivery_code")
    private String deliveryCode;

    @ApiModelProperty("客户编号")
    @JsonProperty("customer_code")
    private String customerCode;

    @ApiModelProperty("客户名称")
    @JsonProperty("customer_cname")
    private String customerName;

    @ApiModelProperty("发运时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("transport_date")
    private Date transportDate;

    @ApiModelProperty("发运人")
    @JsonProperty("transport_person")
    private String transportPerson;

    @ApiModelProperty("发运单运费")
    @JsonProperty("transport_amount")
    private BigDecimal transportAmount;

    @ApiModelProperty("物流公司编码")
    @JsonProperty("transport_company_code")
    private String transportCompanyCode;

    @ApiModelProperty("物流公司名称")
    @JsonProperty("transport_company_name")
    private String transportCompanyName;

    @ApiModelProperty("物流单号")
    @JsonProperty("transport_code")
    private String transportCode;

    @ApiModelProperty(value = "发货仓库名称")
    @JsonProperty("transport_center_name")
    private String transportCenterName;

    @ApiModelProperty(value = "发货仓库编码")
    @JsonProperty("transport_center_code")
    private String transportCenterCode;

    @ApiModelProperty("发运单明细")
    @JsonProperty("detail_list")
    private List<DeliveryDetailRequest> detailList;
}

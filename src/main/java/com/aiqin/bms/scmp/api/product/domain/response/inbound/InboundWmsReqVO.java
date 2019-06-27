package com.aiqin.bms.scmp.api.product.domain.response.inbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Classname: InboundWmsReqVO
 * 描述:
 * @Author: Kt.w
 * @Date: 2019/3/7
 * @Version 1.0
 * @Since 1.0
 */
@Data
@ApiModel("WMS传入入库单实体")
public class InboundWmsReqVO {

    @ApiModelProperty("公司编码")
    private String companyCode;

    @ApiModelProperty("公司名称")
    private String companyName;

    @ApiModelProperty("入库类型编码")
    private Integer inboundTypeCode;

    @ApiModelProperty("库房名称")
    @JsonProperty("belongWarehouse")
    private String warehouseName;

    @ApiModelProperty("预计到货时间")
    @JsonProperty("estimatedDate")
    private Date preArrivalTime;

    @ApiModelProperty("发货人")
    @JsonProperty("consignor")
    private String shipper;

    @ApiModelProperty("发货人电话")
    @JsonProperty("consignorCellphone")
    private String shipperNumber;


    @ApiModelProperty(value = "国家")
    private String country ="中国";

    @ApiModelProperty("省编码")
    @JsonProperty("province")
    private String provinceName;

    @ApiModelProperty("市名称")
    @JsonProperty("city")
    private String cityName;

    @ApiModelProperty("区名称")
    @JsonProperty("county")
    private String countyName;

    @ApiModelProperty("详细地址")
    @JsonProperty("street")
    private String detailedAddress;

    @ApiModelProperty(value = "更新标志 必填 1-新增；2、更新")
    private String addorupdate ="1";

    @ApiModelProperty(value = "明细信息 必填")
    private List<InboundProductWmsReqVO>  list;

    @ApiModelProperty(value = "sku批次 ")
    private List<InboundBatchWmsReqVO> inboundBatchWmsReqVOs;

    @ApiModelProperty(value = "业务模式 必填 1、自提出库；2、运输出库")
    private String businessType ="2";
}

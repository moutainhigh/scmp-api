package com.aiqin.bms.scmp.api.product.domain.response.outbound;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述:
 *
 * @Author: Kt.w
 * @Date: 2019/1/11
 * @Version 1.0
 * @since 1.0
 */
@Data
public class OutboundProductResVo {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("出库单号")
    private String outboundOderCode;

    @ApiModelProperty("sku编号")
    private String skuCode;

    @ApiModelProperty("sku名称")
    private String skuName;

    @ApiModelProperty("图片地址")
    private String pictureUrl;

    @ApiModelProperty("规格")
    private String norms;

    @ApiModelProperty("颜色名称")
    private String colorName;

    @ApiModelProperty("颜色编码")
    private String colorCode;

    @ApiModelProperty("型号")
    private String model;

    @ApiModelProperty("出库单位编码")
    private String unitCode;

    @ApiModelProperty("出库单位名称")
    private String unitName;

    @ApiModelProperty("出库规格")
    private String outboundNorms;

    @ApiModelProperty("出库拆零系数")
    private String outboundBaseUnit;

    @ApiModelProperty("出库基商品含量")
    private String outboundBaseContent;

    @ApiModelProperty("预计出库数量")
    private Long preOutboundNum;

    @ApiModelProperty("预计出库主数量")
    private Long preOutboundMainNum;

    @ApiModelProperty("预计含税进价")
    private BigDecimal preTaxPurchaseAmount;

    @ApiModelProperty("预计含税总价")
    private BigDecimal preTaxAmount;

    @ApiModelProperty("实际出库数量")
    private Long praOutboundNum;

    @ApiModelProperty("实际出库主数量")
    private Long praOutboundMainNum;

    @ApiModelProperty("实际含税进价")
    private BigDecimal praTaxPurchaseAmount;

    @ApiModelProperty("实际含税总价")
    private BigDecimal praTaxAmount;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("行号")
    private Long linenum;

    @ApiModelProperty("税率")
    private BigDecimal tax;

    @ApiModelProperty(value = "实际零数 展示用")
    private Long praSingleCount;

    @ApiModelProperty(value = "预计零数 展示用")
    private Long preSingleCount;
}

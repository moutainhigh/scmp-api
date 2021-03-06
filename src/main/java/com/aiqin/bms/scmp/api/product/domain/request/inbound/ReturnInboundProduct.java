package com.aiqin.bms.scmp.api.product.domain.request.inbound;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Classname: ReturnInboundProduct
 * 描述:  回调查询进项税率
 * @Author: Kt.w
 * @Date: 2019/3/26
 * @Version 1.0
 * @Since 1.0
 */
@Data
@ApiModel("回调查询进项税率")
public class ReturnInboundProduct {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("入库单号")
    private String inboundOderCode;

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

    @ApiModelProperty("入库单位编码")
    private String unitCode;

    @ApiModelProperty("入库单位名称")
    private String unitName;

    @ApiModelProperty("入库规格")
    private String inboundNorms;

    @ApiModelProperty("入库基商品含量")
    private String inboundBaseContent;

    @ApiModelProperty("预计入库数量")
    private Long preInboundNum;

    @ApiModelProperty("预计入库主数量")
    private Long preInboundMainNum;

    @ApiModelProperty("预计含税进价")
    private BigDecimal preTaxPurchaseAmount;

    @ApiModelProperty("预计含税总价")
    private BigDecimal preTaxAmount;

    @ApiModelProperty("实际入库数量")
    private Long praInboundNum;

    @ApiModelProperty("实际入库主数量")
    private Long praInboundMainNum;

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

    @ApiModelProperty("进项税率")
    private BigDecimal tax;

    @ApiModelProperty("入库拆零系数")
    private String inboundBaseUnit;
}

package com.aiqin.bms.scmp.api.supplier.domain.response.allocation;


import com.aiqin.bms.scmp.api.supplier.domain.response.LogData;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 描述:订购单返回详情
 *
 * @Author: Kt.w
 * @Date: 2019/1/10
 * @Version 1.0
 * @since 1.0
 */
@Data
@ApiModel("订购单返回详情")
public class AllocationResVo {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("调拨单编号")
    private String allocationCode;

    @ApiModelProperty("调出仓库(物流中心)编号")
    private String callOutLogisticsCenterCode;

    @ApiModelProperty("调出仓库(物流中心)名称")
    private String callOutLogisticsCenterName;

    @ApiModelProperty("调出库房编码")
    private String callOutWarehouseCode;

    @ApiModelProperty("调出库房名称")
    private String callOutWarehouseName;

    @ApiModelProperty("仓库类型编码")
    private String inventoryAttributesCode;

    @ApiModelProperty("仓库类型名称")
    private String inventoryAttributesName;

    @ApiModelProperty("采购组编码")
    private String purchaseGroupCode;

    @ApiModelProperty("采购组名称")
    private String purchaseGroupName;

    @ApiModelProperty("调入仓库(物流中心)编码")
    private String callInLogisticsCenterCode;

    @ApiModelProperty("调入仓库(物流中心)名称")
    private String callInLogisticsCenterName;

    @ApiModelProperty("调入库房编码")
    private String callInWarehouseCode;

    @ApiModelProperty("调入库房名称")
    private String callInWarehouseName;

    @ApiModelProperty("调拨类型编码")
    private Byte allocationTypeCode;

    @ApiModelProperty("调拨类型名称")
    private String allocationTypeName;

    @ApiModelProperty("负责人")
    private String principal;

    @ApiModelProperty("数量")
    private Long quantity;

    @ApiModelProperty("含税调拨金额")
    private Long taxRefundAmount;

    @ApiModelProperty("出库单号")
    private String outboundOderCode;

    @ApiModelProperty("入库单号")
    private String inboundOderCode;

    @ApiModelProperty("状态编码")
    private Byte allocationStatusCode;

    @ApiModelProperty("状态名称")
    private String allocationStatusName;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @ApiModelProperty("修改时间")
    private Date updateTime;

    @ApiModelProperty("修改人")
    private String updateBy;

    @ApiModelProperty("sku")
    private List<AllocationProductResVo> skuList;


    @ApiModelProperty("操作日志列表")
    private List<LogData> logDataList;

    @ApiModelProperty("公司编码")
    private String companyCode;

    @ApiModelProperty("公司名称")
    private String companyName;
}

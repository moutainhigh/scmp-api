package com.aiqin.bms.scmp.api.supplier.domain.response.warehouse;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 描述:列表展示返回实体
 *
 * @Author: Kt.w
 * @Date: 2018/12/25
 * @Version 1.0
 * @since 1.0
 */
@ApiModel("列表展示返回实体")
@Data
public class QueryWarehouseResVo {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("库房编码")
    private String warehouseCode;

    @ApiModelProperty("库房名称")
    private String warehouseName;

    @ApiModelProperty("物流中心名称")
    private String logisticsCenterName;

    @ApiModelProperty("启用禁用状态")
    private Byte enable;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("修改时间")
    private Date updateTime;

    @ApiModelProperty("修改人")
    private String updateBy;

    @ApiModelProperty("排序")
    private Integer warehouseOrder;

    @ApiModelProperty("仓库类型名称")
    private String warehouseTypeName;

    @ApiModelProperty("仓库类型编码")
    private Byte warehouseTypeCode;
}

package com.aiqin.bms.scmp.api.product.domain.request.sku.config;

import com.aiqin.bms.scmp.api.supplier.domain.request.approvalfile.ApprovalFileInfoReqVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

/**
 * @author knight.xie
 * @version 1.0
 * @className ApplySkuConfigReqVo
 * @date 2019/5/25 11:29
 */
@ApiModel("提交SKU配置申请VO")
@Data
public class ApplySkuConfigReqVo {

    @ApiModelProperty("是否使用生效时间(0:是1:否)")
    private Byte selectionEffectiveTime;

    @ApiModelProperty("生效开始时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date selectionEffectiveStartTime;

    @ApiModelProperty("直属上级编码")
    @NotEmpty(message = "直属上级编码不能为空！")
    private String directSupervisorCode;

    @ApiModelProperty("直属上级名称")
    @NotEmpty(message = "直属上级名称不能为空！")
    private String directSupervisorName;

    @ApiModelProperty("需要提交申请的编码集合")
    @NotEmpty(message = "编码不能为空！")
    List<String> skuConfigs;

//    @ApiModelProperty("需要提交申请的编码集合")
//    @NotEmpty(message = "编码不能为空！")
//    List<Long> supplierId;

    @ApiModelProperty("备注")
    private String approvalRemark;

    @ApiModelProperty("审批名称")
    private String approvalName;

    @ApiModelProperty("附件信息")
    private List<ApprovalFileInfoReqVo> approvalFileInfos;

    @ApiModelProperty("职位编码")
    private String positionCode;

    @ApiModelProperty("职位名称")
    private String positionName;
}

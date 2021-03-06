package com.aiqin.bms.scmp.api.supplier.domain.request.applycontract.dto;

import com.aiqin.bms.scmp.api.common.CommonBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述: 申请合同关联进货额数据库交互实体
 *
 * @Author: Kt.w
 * @Date: 2018/12/13
 * @Version 1.0
 * @since 1.0
 */
@Data
@ApiModel("申请合同关联进货额数据库交互实体")
public class ApplyContractPurchaseVolumeDTO extends CommonBean {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("所属申请合同code")
    private String applyContractCode;

    @ApiModelProperty("0:无税1:有税")
    private Byte tax;

    @ApiModelProperty("金额(以分为单位)")
    private BigDecimal amountMoney;

    @ApiModelProperty("按比例")
    private BigDecimal proportion;

    @ApiModelProperty("或者金额(以分为单位)")
    private BigDecimal orAmountMoney;

    @ApiModelProperty("返利类型1月2季3半年4年")
    private Byte planType;

}

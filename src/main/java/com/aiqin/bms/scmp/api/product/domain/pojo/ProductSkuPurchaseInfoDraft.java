package com.aiqin.bms.scmp.api.product.domain.pojo;

import com.aiqin.bms.scmp.api.base.PropertyMsg;
import com.aiqin.bms.scmp.api.common.CommonBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("采购信息")
@Data
public class ProductSkuPurchaseInfoDraft extends CommonBean {
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("商品编号")
    private String productCode;

    @ApiModelProperty("规格")
    @PropertyMsg("规格")
    private String spec;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品code")
    private String unitCode;

    @ApiModelProperty("单位名称")
    @PropertyMsg("单位")
    private String unitName;

    @ApiModelProperty("进货码")
    @PropertyMsg("条形码")
    private String purchaseCode;

    @ApiModelProperty("基商品含量")
    @PropertyMsg("单位含量")
    private Integer baseProductContent;

    @ApiModelProperty("商品sku 编码")
    private String productSkuCode;

    @ApiModelProperty("商品sku 名称")
    private String productSkuName;

    @ApiModelProperty("拆零系数")
    private BigDecimal zeroRemovalCoefficient;

}
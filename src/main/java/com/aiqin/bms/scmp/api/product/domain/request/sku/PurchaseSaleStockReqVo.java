package com.aiqin.bms.scmp.api.product.domain.request.sku;

import com.aiqin.bms.scmp.api.common.CommonBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author knight.xie
 * @version 1.0
 * @className PurchaseSaleStockReqVo
 * @date 2019/5/7 10:34
 */
@Data
@ApiModel("进销存")
public class PurchaseSaleStockReqVo extends CommonBean {
    @ApiModelProperty("商品sku 编码")
    private String productSkuCode;

    @ApiModelProperty("商品sku 名称")
    private String productSkuName;

    @ApiModelProperty("商品编号")
    private String productCode;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("类型(0:库存,1:采购, 2:销售, 3:门店销售)")
    private Byte type;

    @ApiModelProperty("规格")
    private String spec;

    @ApiModelProperty("单位code")
    private String unitCode;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("条形码")
    private String barCode;

    @ApiModelProperty("基商品含量")
    private Integer baseProductContent;

    @ApiModelProperty("最大订购数-销售信息独有")
    private Integer maxOrderNum;

    @ApiModelProperty("描述-门店销售独有")
    private String description;

    @ApiModelProperty("是否缺省(0:否,1：是)-门店销售独有")
    private Byte isDefault;

    @ApiModelProperty("拆零系数")
    private Long zeroRemovalCoefficient;

    @ApiModelProperty(value = "进货码", hidden = true)
    private String purchaseCode;

    @ApiModelProperty(value = "配送码", hidden = true)
    private String deliveryCode;

    @ApiModelProperty(value = "销售码", hidden = true)
    private String salesCode;

    @ApiModelProperty(value = "门店销售-单位名称", hidden = true)
    private String smallUnit;
    public void setBarCode(String barCode) {
        this.barCode = barCode;
        this.purchaseCode = barCode;
        this.deliveryCode = barCode;
        this.salesCode = barCode;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
        this.smallUnit = unitName;
    }
}

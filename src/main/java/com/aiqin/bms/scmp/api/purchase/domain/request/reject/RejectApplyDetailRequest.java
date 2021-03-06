package com.aiqin.bms.scmp.api.purchase.domain.request.reject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: zhao shuai
 * @create: 2020-05-13
 **/
@Data
@ApiModel("退供申请单查询请求类")
public class RejectApplyDetailRequest {

    @ApiModelProperty(value = "sku编码")
    @JsonProperty("sku_code")
    private String skuCode;

    @ApiModelProperty(value = "sku名称")
    @JsonProperty("sku_name")
    private String skuName;

    @ApiModelProperty(value = "供应商编码")
    @JsonProperty("supplier_code")
    private String supplierCode;

    @ApiModelProperty(value = "供应商名称")
    @JsonProperty("supplier_name")
    private String supplierName;

    @ApiModelProperty(value = "仓编码(物流中心编码)")
    @JsonProperty("transport_center_code")
    private String transportCenterCode;

    @ApiModelProperty(value = "仓名称(物流中心名称)")
    @JsonProperty("transport_center_name")
    private String transportCenterName;

    @ApiModelProperty(value = "库房编码")
    @JsonProperty("warehouse_code")
    private String warehouseCode;

    @ApiModelProperty(value = "库房名称")
    @JsonProperty("warehouse_name")
    private String warehouseName;

    @ApiModelProperty(value="单位")
    @JsonProperty("unit_code")
    private String unitCode;

    @ApiModelProperty(value="单位")
    @JsonProperty("unit_name")
    private String unitName;

    @ApiModelProperty(value = "可用库存数")
    @JsonProperty("available_count")
    private Long availableCount;

    @ApiModelProperty(value = "库存数量")
    @JsonProperty("stock_count")
    private Long stockCount;

    @ApiModelProperty(value = "含税成本")
    @JsonProperty("product_cost")
    private BigDecimal productCost;

    @ApiModelProperty(value = "最新采购价")
    @JsonProperty("new_purchase_price")
    private BigDecimal newPurchasePrice;

    @ApiModelProperty(value = "最高采购价")
    @JsonProperty("purchase_max")
    private BigDecimal purchaseMax;

    @ApiModelProperty(value = "品类编码")
    @JsonProperty("category_code")
    private String categoryId;

    @ApiModelProperty(value = "品类名称")
    @JsonProperty("category_name")
    private String categoryName;

    @ApiModelProperty(value = "品牌编码")
    @JsonProperty("brand_id")
    private String brandId;

    @ApiModelProperty(value = "品牌名称")
    @JsonProperty("brand_name")
    private String brandName;

    @ApiModelProperty(value = "规格")
    @JsonProperty("product_spec")
    private String productSpec;

    @ApiModelProperty(value="颜色编码")
    @JsonProperty("color_code")
    private String colorCode;

    @ApiModelProperty(value="颜色")
    @JsonProperty("color_name")
    private String colorName;

    @ApiModelProperty(value="型号")
    @JsonProperty("model_number")
    private String modelNumber;

    @ApiModelProperty(value = "商品类型 0商品 1赠品 2实物返")
    @JsonProperty("product_type")
    private Integer productType;

    @ApiModelProperty(value = "所属部门编码")
    @JsonProperty("product_sort_code")
    private String productSortCode;

    @ApiModelProperty(value = "所属部门名称")
    @JsonProperty("product_sort_name")
    private String productSortName;

    @ApiModelProperty(value = "税率")
    @JsonProperty("tax_rate")
    private BigDecimal taxRate;

    @ApiModelProperty(value="条形码")
    @JsonProperty("barcode")
    private String barcode;

    @ApiModelProperty(value = "采购组编码")
    @JsonProperty("purchase_group_code")
    private String purchaseGroupCode;

    @ApiModelProperty(value = "采购组名称")
    @JsonProperty("purchase_group_name")
    private String purchaseGroupName;

    @ApiModelProperty(value = "商品结算方式编码")
    @JsonProperty("settlement_method_code")
    private String settlementMethodCode;

    @ApiModelProperty(value = "商品结算")
    @JsonProperty("settlement_method_name")
    private String settlementMethodName;

    @ApiModelProperty(value="厂商sku")
    @JsonProperty("factory_sku_code")
    private String factorySkuCode;

    @ApiModelProperty(value = "spu编码")
    @JsonProperty("spu_code")
    private String spuCode;

    @ApiModelProperty(value = "spu名称")
    @JsonProperty("spu_name")
    private String spuName;

    @ApiModelProperty(value = "最小单位数量")
    @JsonProperty("total_count")
    private Long totalCount;

    @ApiModelProperty(value = "含税单价")
    @JsonProperty("product_amount")
    private BigDecimal productAmount;

    @ApiModelProperty(value = "含税总价")
    @JsonProperty("product_total_amount")
    private BigDecimal productTotalAmount;

    @ApiModelProperty(value = "批次号")
    @JsonProperty("batch_code")
    private String batchCode;

    @ApiModelProperty(value = "批次含税采购价")
    @JsonProperty("purchase_price")
    private BigDecimal purchasePrice;

    @ApiModelProperty(value="批次编号")
    @JsonProperty("batch_info_code")
    private String batchInfoCode;

    @ApiModelProperty(value="批次备注")
    @JsonProperty("batch_remark")
    private String batchRemark;

    @ApiModelProperty(value="生产日期")
    @JsonProperty("product_date")
    private String productDate;

    @ApiModelProperty(value="过期日期")
    @JsonProperty("be_overdue_date")
    private String beOverdueDate;

    @ApiModelProperty(value="库位号")
    @JsonProperty("location_code")
    private String locationCode;

    @ApiModelProperty(value="供应商集团编码")
    @JsonProperty("supplier_company_code")
    private String supplierCompanyCode;

    @ApiModelProperty(value="供应商集团名称")
    @JsonProperty("supplier_company_name")
    private String supplierCompanyName;

}

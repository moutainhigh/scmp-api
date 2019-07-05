package com.aiqin.bms.scmp.api.product.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @className QueryStockSkuRespVo
 * @date 2019/6/28 11:06
 * @description 库房管理新增调拨,移库,报废列表商品sku返回VO
 * @version 1.0
 */
@ApiModel("查询库房管理商品sku返回VO")
@Data
public class QueryStockSkuListRespVo implements Serializable {

    @ApiModelProperty("唯一id")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty("库房")
    @JsonProperty("warehouse_code")
    private String warehouseCode;

    @ApiModelProperty("sku编码")
    @JsonProperty("sku_code")
    private String skuCode;

    @ApiModelProperty("sku名称")
    @JsonProperty("sku_name")
    private String skuName;

    @ApiModelProperty("品类code")
    @JsonProperty("product_category_code")
    private String productCategoryCode;

    @ApiModelProperty("品类名称")
    @JsonProperty("product_category_name")
    private String productCategoryName;

    @ApiModelProperty("品牌编码")
    @JsonProperty("product_brand_code")
    private String productBrandCode;

    @ApiModelProperty("品牌名称")
    @JsonProperty("product_brand_name")
    private String productBrandName;

    @ApiModelProperty("类型")
    @JsonProperty("goods_gifts")
    private Integer goodsGifts;

    @ApiModelProperty("规格")
    @JsonProperty("spec")
    private String spec;

    @ApiModelProperty("颜色编码")
    @JsonProperty("color_code")
    private String colorCode;

    @ApiModelProperty("颜色名称")
    @JsonProperty("color_name")
    private String colorName;

    @ApiModelProperty("型号")
    @JsonProperty("model_number")
    private String modelNumber;

    @ApiModelProperty("单位编码")
    @JsonProperty("unit_code")
    private String unitCode;

    @ApiModelProperty("单位名称")
    @JsonProperty("unit_name")
    private String unitName;

    @ApiModelProperty("库存(可用库存)")
    @JsonProperty("available_num")
    private Long availableNum;

    @ApiModelProperty("税率")
    @JsonProperty("tax_rate")
    private Long taxRate;

    @ApiModelProperty("昨天含税成本")
    @JsonProperty("tax_cost")
    private Long taxCost;

    @ApiModelProperty("批次号集合")
    @JsonProperty(value = "batch_code_list")
    private List<String> batchCodeList;

    @ApiModelProperty("批次号")
    @JsonProperty(value = "batch_code")
    private String batchCode;

    @ApiModelProperty("生产日期")
    @JsonProperty(value = "production_date")
    private String productionDate;

}

package com.aiqin.bms.scmp.api.product.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description:
 * sku号 采购组 类型	所属部门	品类编码	SKU名称	SKU简称	SKU助记码	所属SPU	SPU简称	SPU助记码	款号	货号	品牌	商品属性	颜色	型号	保质天数	供货渠道类别	适用起始月龄	是否季节商品	仓位类型	结构性商品	使用时长	季节波段	唯一码管理	等级	特征	通货等级	覆盖渠道	商品标签	商品备注	商品介绍	厂商指导价	爱亲渠道价	萌贝树渠道价	小红马渠道价	爱亲分销价	萌贝树分销价	小红马分销价	售价	会员价	供应商	联营扣点	返点	供应商SKU编号	供货渠道类别	库存规格	库存单位	库存长(MM)	库存宽(MM)	库存高(MM)	库存毛重(KG)	库存净重(KG)	采购规格	采购单位	采购长(MM)	采购宽(MM)	采购高(MM)	采购毛重(KG)	采购净重(KG)	采购单位含量	采购条形码	分销交易倍数	最大订购数量	销售条形码	销售描述	结算方式	进项税率	销项税率	积分系数	物流费奖励比例	华北仓状态	华东仓状态	华南仓状态	西南仓状态	华中仓状态	生产厂家	生产厂家SKU编号	保修地址
 * @author: NullPointException
 * @date: 2019-07-21
 * @time: 14:24
 */

@Data
public class SkuInfoImportReally {
    @ApiModelProperty("sku编号")
    @ExcelProperty(index = 0, value = "SKU号")
    private String skuCode;

    @ApiModelProperty("采购组名称")
    @ExcelProperty(index = 1, value = "采购组")
    private String procurementSectionName;

    @ApiModelProperty("商品/赠品(0:商品，1:赠品 2:组合商品)")
    @ExcelProperty(index = 2, value = "类型")
    private String goodsGiftsDesc;

    @ApiModelProperty("商品类别(所属部门)名称")
    @ExcelProperty(index = 3, value = "所属部门")
    private String productSortName;

    @ApiModelProperty("商品品类名称")
    @ExcelProperty(index = 4, value = "品类编码")
    private String productCategoryName;

    @ApiModelProperty("sku名称")
    @ExcelProperty(index = 5, value = "SKU名称")
    private String skuName;

    @ApiModelProperty("sku简称")
    @ExcelProperty(index = 6, value = "SKU简称")
    private String skuAbbreviation;

    @ApiModelProperty("sku助记码")
    @ExcelProperty(index = 7, value = "SKU助记码")
    private String mnemonicCode;

    @ApiModelProperty("商品名称")
    @ExcelProperty(index = 8, value = "所属SPU")
    private String productName;

    @ApiModelProperty("SPU简称")
    @ExcelProperty(index = 9, value = "SPU简称")
    private String spuAbbreviation;

    @ApiModelProperty("SPU助记码")
    @ExcelProperty(index = 10, value = "SPU助记码")
    private String spuMnemonicCode;

    @ApiModelProperty("款号")
    @ExcelProperty(index = 11, value = "款号")
    private String styleNumber;

    @ApiModelProperty("货号")
    @ExcelProperty(index = 12, value = "货号")
    private String itemNumber;

    @ApiModelProperty("商品品牌")
    @ExcelProperty(index = 13, value = "品牌")
    private String productBrandName;

    @ApiModelProperty("商品属性名称")
    @ExcelProperty(index = 14, value = "商品属性")
    private String productPropertyName;

    @ApiModelProperty("颜色名称")
    @ExcelProperty(index = 15, value = "颜色")
    private String colorName;

    @ApiModelProperty("型号")
    @ExcelProperty(index = 16, value = "型号")
    private String modelNumber;

    @ApiModelProperty("保质数量")
    @ExcelProperty(index = 17, value = "保质天数")
    private String qualityDate;

    @ApiModelProperty("适用起始月龄")
    @ExcelProperty(index = 18, value = "适用起始月龄")
    private String applicableMonthAge;

    @ApiModelProperty("是否季节性商品(0:是 1:否)")
    @ExcelProperty(index = 19, value = "是否季节商品")
    private String seasonalGoodsDesc;

    @ApiModelProperty("仓位类型名称--商品数据字典")
    @ExcelProperty(index = 20, value = "仓位类型")
    private String warehouseTypeName;

    @ApiModelProperty("是否结构性商品(0:是 1:否)")
    @ExcelProperty(index = 21, value = "结构性商品")
    private String structuralGoodsDesc;

    @ApiModelProperty("使用时长")
    @ExcelProperty(index = 22, value = "使用时长")
    private String useTime;

    @ApiModelProperty("季节波段")
    @ExcelProperty(index = 23, value = "季节波段")
    private String seasonBand;

    @ApiModelProperty("唯一码管理(0:是 1:否)")
    @ExcelProperty(index = 24, value = "唯一码管理")
    private String uniqueCodeDesc;

    @ApiModelProperty("等级")
    @ExcelProperty(index = 25, value = "等级")
    private String level;

    @ApiModelProperty("特征名称")
    @ExcelProperty(index = 26, value = "特征")
    private String featureName;

    @ApiModelProperty("通货等级")
    @ExcelProperty(index = 27, value = "通货等级")
    private String currencyLevelName;

    @ApiModelProperty("价格渠道名称")
    @ExcelProperty(index = 28, value = "覆盖渠道")
    private String priceChannelName;

    @ApiModelProperty("标签名称")
    @ExcelProperty(index = 29, value = "商品标签")
    private String tagName;

    @ApiModelProperty("备注")
    @ExcelProperty(index = 30, value = "商品备注")
    private String remark;

    @ApiModelProperty("商品介绍")
    @ExcelProperty(index = 31, value = "商品介绍")
    private String productDesc;

    @ApiModelProperty("厂商指导价")
    @ExcelProperty(index = 32, value = "厂商指导价")
    private String manufacturerGuidePrice;

    @ApiModelProperty(value ="爱亲渠道价")
    @ExcelProperty(index = 33, value = "爱亲渠道价")
    private String readyCol67;

    @ApiModelProperty(value ="萌贝树渠道价")
    @ExcelProperty(index = 34, value = "萌贝树渠道价")
    private String readyCol68;

    @ApiModelProperty(value ="小红马渠道价")
    @ExcelProperty(index = 35, value = "小红马渠道价")
    private String readyCol69;

    @ApiModelProperty(value ="爱亲分销价")
    @ExcelProperty(index = 37, value = "爱亲分销价")
    private String readyCol70;

    @ApiModelProperty(value ="萌贝树分销价")
    @ExcelProperty(index = 38, value = "萌贝树分销价")
    private String readyCol71;

    @ApiModelProperty(value ="小红马分销价")
    @ExcelProperty(index = 39, value = "小红马分销价")
    private String readyCol72;

    @ApiModelProperty(value ="售价")
    @ExcelProperty(index = 40, value = "售价")
    private String readyCol74;

    @ApiModelProperty(value ="会员价")
    @ExcelProperty(index = 41, value = "会员价")
    private String readyCol75;

    @ApiModelProperty("供货单位名称")
    @ExcelProperty(index = 42, value = "供应商")
    private String supplyUnitName;

    @ApiModelProperty("联营扣点")
    @ExcelProperty(index = 43, value = "联营扣点")
    private String jointFranchiseRate;

    @ApiModelProperty("返点")
    @ExcelProperty(index = 44, value = "返点")
    private String point;

    @ApiModelProperty("厂商SKU编码")
    @ExcelProperty(index = 45, value = "供应商SKU编号")
    private String factorySkuCode;

    @ApiModelProperty("含税采购价")
    @ExcelProperty(index = 46, value = "含税采购价")
    private BigDecimal taxIncludedPrice;

    @ApiModelProperty(value ="供应商供货渠道类别")
    @ExcelProperty(index = 47, value = "供应商供货渠道类别")
    private String supplyCategoriesSupplyChannelsName;

    @ApiModelProperty("规格")
    @ExcelProperty(index = 48, value = "库存规格")
    private String stockSpec;

    @ApiModelProperty("单位名称")
    @ExcelProperty(index = 49, value = "库存单位")
    private String stockUnitName;

    @ApiModelProperty("包装箱子长度")
    @ExcelProperty(index = 50, value = "库存长")
    private String stockBoxLength;

    @ApiModelProperty("宽度（mm）")
    @ExcelProperty(index = 51, value = "库存宽")
    private String stockBoxWidth;

    @ApiModelProperty("箱子高度")
    @ExcelProperty(index = 52, value = "库存高")
    private String stockBoxHeight;

    @ApiModelProperty("库存体积")
    @ExcelProperty(index = 53, value = "库存体积")
    private String stockBoxVolume;

    @ApiModelProperty("毛重")
    @ExcelProperty(index = 54, value = "库存毛重")
    private String stockBoxGrossWeight;

    @ApiModelProperty("库存净重")
    @ExcelProperty(index = 55, value = "库存净重")
    private String stockNetWeight;

    @ApiModelProperty("规格")
    @ExcelProperty(index = 56, value = "采购规格")
    private String purchaseSpec;

    @ApiModelProperty("单位名称")
    @ExcelProperty(index = 57, value = "采购单位")
    private String purchaseUnitName;

    @ApiModelProperty("包装箱子长度")
    @ExcelProperty(index = 58, value = "采购长")
    private String purchaseBoxLength;

    @ApiModelProperty("宽度（mm）")
    @ExcelProperty(index = 59, value = "采购宽")
    private String purchaseBoxWidth;

    @ApiModelProperty("箱子高度")
    @ExcelProperty(index = 60, value = "采购高")
    private String purchaseBoxHeight;

    @ApiModelProperty("采购体积")
    @ExcelProperty(index = 61, value = "采购体积")
    private String purchaseBoxVolume;

    @ApiModelProperty("毛重")
    @ExcelProperty(index = 62, value = "采购毛重")
    private String purchaseBoxGrossWeight;

    @ApiModelProperty("库存净重")
    @ExcelProperty(index = 63, value = "采购净重")
    private String purchaseNetWeight;

    @ApiModelProperty("基商品含量")
    @ExcelProperty(index = 64, value = "采购单位含量")
    private String purchaseBaseProductContent;

    @ApiModelProperty("条形码")
    @ExcelProperty(index = 65, value = "采购条形码")
    private String purchaseBarCode;

    @ApiModelProperty("拆零系数")
    @ExcelProperty(index = 66, value = "分销交易倍数")
    private String distributionZeroRemovalCoefficient;

    @ApiModelProperty("最大订购数-销售信息独有")
    @ExcelProperty(index = 67, value = "最大订购数")
    private String maxOrderNum;

    @ApiModelProperty("条形码")
    @ExcelProperty(index = 68, value = "销售条形码")
    private String saleBarCode;

    @ApiModelProperty("描述-门店销售独有")
    @ExcelProperty(index = 69, value = "销售描述")
    private String description;

    @ApiModelProperty("结算方式名称")
    @ExcelProperty(index = 70, value = "结算方式")
    private String settlementMethodName;

    @ApiModelProperty("进项税率")
    @ExcelProperty(index = 71, value = "进项税率")
    private String inputTaxRate;

    @ApiModelProperty("销项税率")
    @ExcelProperty(index = 72, value = "销项税率")
    private String outputTaxRate;

    @ApiModelProperty("积分系数")
    @ExcelProperty(index = 73, value = "积分系数")
    private String integralCoefficient;

    @ApiModelProperty("物流费奖励比例")
    @ExcelProperty(index = 74, value = "物流费奖励比例")
    private String logisticsFeeAwardRatio;

    @ApiModelProperty(value ="华北仓状态")
    @ExcelProperty(index = 75, value = "华北仓状态")
    private String readyCol76;

    @ApiModelProperty(value ="华东仓状态")
    @ExcelProperty(index = 76, value = "华东仓状态")
    private String readyCol77;

    @ApiModelProperty(value ="华南仓状态")
    @ExcelProperty(index = 77, value = "华南仓状态")
    private String readyCol78;

    @ApiModelProperty(value ="西南仓状态")
    @ExcelProperty(index = 78, value = "西南仓状态")
    private String readyCol79;

    @ApiModelProperty(value ="华中仓状态")
    @ExcelProperty(index = 79, value = "华中仓状态")
    private String readyCol80;

    @ApiModelProperty("生产厂家")
    @ExcelProperty(index = 80, value = "生产厂家")
    private String manufacturerName;

    @ApiModelProperty("厂方商品编号")
    @ExcelProperty(index = 81, value = "厂方商品编号")
    private String factoryProductNumber;

    @ApiModelProperty("保修地址")
    @ExcelProperty(index = 82, value = "保修地址")
    private String address;

    @ApiModelProperty("错误原因")
    private String error;
}

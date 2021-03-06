package com.aiqin.bms.scmp.api.supplier.domain.excel.im;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:
 *
 * @author: NullPointException
 * @date: 2019-07-13
 * @time: 16:34
 */
@Data

public class SupplierImportNew extends BaseRowModel{
    /**
     * 表头
     */
    public static final String HEDE = "SupplierImportNew(applySupplyName=供应商名称, applySupplyType=供应商类型, supplierName=所属集团, applyAbbreviation=简称, phone=电话, fax=传真, provinceName=省, cityName=市, districtName=县, address=详细地址, zipCode=邮编, email=邮箱, companyWebsite=公司网址, taxId=税号, registeredCapital=注册资金(万元), corporateRepresentative=法人代表, contactName=联系人姓名, mobilePhone=手机号, minOrderAmount=最低订货金额, maxOrderAmount=最高订货金额, sendProvinceName=发货省, sendCityName=发货市, sendDistrictName=发货县, sendingAddress=发货详细地址, returnProvinceName=退货省, returnCityName=退货市, returnDistrictName=退货县, returningAddress=退货详细地址, bankAccount=开户银行, account=银行账号, accountName=户名, maxPaymentAmount=最高付款金额, brand=品牌, paymentMethod=结款方式, deliveryArea=供货区域, remark=备注, approvalName=审批名称, property=属性)";

    @ApiModelProperty("供应商名称")
    @ExcelProperty(index = 0 , value = "供应商名称")
    private String applySupplyName;

    @ApiModelProperty("供应商类型")
    @ExcelProperty(index = 1 , value = "供应商类型")
    private String applySupplyType;

    @ApiModelProperty("所属集团")
    @ExcelProperty(index = 2 , value = "所属集团")
    private String supplierName;

    @ApiModelProperty("简称")
    @ExcelProperty(index = 3 , value = "简称")
    private String applyAbbreviation;

    @ApiModelProperty("电话")
    @ExcelProperty(index = 4 , value = "简称")
    private String phone;

    @ApiModelProperty("传真")
    @ExcelProperty(index = 5 , value = "传真")
    private String fax;

    @ApiModelProperty("省")
    @ExcelProperty(index = 6 , value = "省")
    private String provinceName;

    @ApiModelProperty("市")
    @ExcelProperty(index = 7 , value = "市")
    private String cityName;

    @ApiModelProperty("县")
    @ExcelProperty(index = 8 , value = "县")
    private String districtName;

    @ApiModelProperty("详细地址")
    @ExcelProperty(index = 9 , value = "详细地址")
    private String address;

    @ApiModelProperty("邮编")
    @ExcelProperty(index = 10 , value = "邮编")
    private String zipCode;

    @ApiModelProperty("邮箱")
    @ExcelProperty(index = 11 , value = "邮箱")
    private String email;

    @ApiModelProperty("公司网址")
    @ExcelProperty(index = 12 , value = "公司网址")
    private String companyWebsite;

    @ApiModelProperty("税号")
    @ExcelProperty(index = 13 , value = "税号")
    private String taxId;

    @ApiModelProperty("注册资金(万元)")
    @ExcelProperty(index = 14 , value = "注册资金(万元)")
    private String registeredCapital;

    @ApiModelProperty("法人代表")
    @ExcelProperty(index = 15 , value = "法人代表")
    private String corporateRepresentative;

    @ApiModelProperty("联系姓名")
    @ExcelProperty(index = 16 , value = "联系姓名")
    private String contactName;

    @ApiModelProperty("手机号")
    @ExcelProperty(index = 17 , value = "手机号")
    private String mobilePhone;

//    @ApiModelProperty("采购组名称")
//    @ExcelProperty(index = 18 , value = "采购组名称")
//    private String purchasingGroupName;

    @ApiModelProperty("最低订货金额")
    @ExcelProperty(index = 18 , value = "最低订货金额")
    private String minOrderAmount;

    @ApiModelProperty("最高订货金额")
    @ExcelProperty(index = 19 , value = "最高订货金额")
    private String maxOrderAmount;

    @ApiModelProperty("发货省")
    @ExcelProperty(index = 20 , value = "发货省")
    private String sendProvinceName;

    @ApiModelProperty("发货市")
    @ExcelProperty(index = 21 , value = "发货市")
    private String sendCityName;

    @ApiModelProperty("发货县")
    @ExcelProperty(index = 22 , value = "发货县")
    private String sendDistrictName;

    @ApiModelProperty("发货详细地址")
    @ExcelProperty(index = 23 , value = "发货详细地址")
    private String sendingAddress;

//    @ApiModelProperty("发送至")
//    @ExcelProperty(index = 24 , value = "发送至")
//    private String sendTo;

//    @ApiModelProperty("发送送货天数")
//    @ExcelProperty(index = 25 , value = "发送送货天数")
//    private String deliveryDays;

    @ApiModelProperty("退货省")
    @ExcelProperty(index = 24 , value = "退货省")
    private String returnProvinceName;

    @ApiModelProperty("退货市")
    @ExcelProperty(index = 25 , value = "退货市")
    private String returnCityName;

    @ApiModelProperty("退货县")
    @ExcelProperty(index = 26 , value = "退货县")
    private String returnDistrictName;

    @ApiModelProperty("退货详细地址")
    @ExcelProperty(index = 27 , value = "退货详细地址")
    private String returningAddress;

    @ApiModelProperty("开户银行")
    @ExcelProperty(index = 28 , value = "开户银行")
    private String bankAccount;

    @ApiModelProperty("银行账号")
    @ExcelProperty(index = 29 , value = "银行账号")
    private String account;

    @ApiModelProperty("户名")
    @ExcelProperty(index = 30 , value = "户名")
    private String accountName;

    @ApiModelProperty("最高付款金额")
    @ExcelProperty(index = 31 , value = "最高付款金额")
    private String maxPaymentAmount;

//    @ApiModelProperty("退送至")
//    @ExcelProperty(index = 30 , value = "退送至")
//    private String returnTo;

//    @ApiModelProperty("退送送货天数")
//    @ExcelProperty(index = 31 , value = "退送送货天数")
//    private String returnDays;

//    @ApiModelProperty("直属上级")
//    @ExcelProperty(index = 33 , value = "直属上级")
//    private String directSupervisorName;

    @ApiModelProperty("品牌")
    @ExcelProperty(index = 32 , value = "品牌")
    private String brand;

    @ApiModelProperty("结款方式")
    @ExcelProperty(index = 33 , value = "结款方式")
    private String paymentMethod;

    @ApiModelProperty("供货区域")
    @ExcelProperty(index = 34 , value = "供货区域")
    private String deliveryArea;

    @ApiModelProperty("备注")
    @ExcelProperty(index = 35 , value = "备注")
    private String remark;

    @ApiModelProperty("审批名称")
    @ExcelProperty(index = 36 , value = "审批名称")
    private String approvalName;

    @ApiModelProperty("属性")
    @ExcelProperty(index = 37 , value = "属性")
    private String property;
}

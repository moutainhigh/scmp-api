package com.aiqin.bms.scmp.api.constant;

/**
 * Description:
 *
 * @author: NullPointException
 * @date: 2019-05-20
 * @time: 17:45
 */
public interface CommonConstant {
    //新增
    Integer ADD = 1;
    //提交
    Integer SUBMIT = 2;
    //-------------变价类型--------------
    //采购变价
    String PURCHASE_CHANGE_PRICE = "1";
    //销售变价
    String SALE_CHANGE_PRICE = "2";
    //临时变价
    String TEMPORARY_CHANGE_PRICE = "3";
    //批发价
    String SALE_WHOLE_PRICE = "4";
    //销售区域变价
//    String SALE_AREA_CHANGE_PRICE = "4";
    //临时区域变价
    String TEMPORARY_AREA_CHANGE_PRICE = "5";
    //销售价
    String SALE_PRICE = "2345";
    //临时价
    String TEMP_PRICE = "35";
    //永久价
    String FOREVER_PRICE = "24";
    //----------审核状态-------------
    //待提交
    Integer PENDING_SUBMISSION = 0;
    //审核中
    Integer UNDER_REVIEW = 1;
    //审核通过
    Integer EXAMINATION_PASSED = 2;
    //审核驳回
    Integer REVIEWR_EJECTION = 3;
    //取消审核
    Integer CANCEL = 4;
    //供货渠道类型 配送
    Integer SUPPLY_CHANNEL_TYPE_DELIVERY = 1;
    //供货渠道类型 直送
    Integer SUPPLY_CHANNEL_TYPE_DIRECT_DELIVERY = 2;
    //订单
    String SYSTEM_AUTO = "系统自动";
    String SYSTEM_AUTO_CODE = "-1";
    Integer NEW_PRODUCT = 1;
    Integer DEFECTIVE = 2;
    String CREATE_OUTBOUND_FAILED = "生成出库单失败，请重试";

    /**
     * 订单支付状态
     */
    Integer TO_BE_PAID = 1;

    Integer PAID = 2;
}

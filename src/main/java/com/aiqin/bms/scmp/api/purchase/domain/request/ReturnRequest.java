package com.aiqin.bms.scmp.api.purchase.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * ━━━━━━神兽出没━━━━━━
 * 　　┏┓　　　┏┓+ +
 * 　┏┛┻━━━┛┻┓ + +
 * 　┃　　　　　　　┃
 * 　┃　　　━　　　┃ ++ + + +
 * ████━████ ┃+
 * 　┃　　　　　　　┃ +
 * 　┃　　　┻　　　┃
 * 　┃　　　　　　　┃
 * 　┗━┓　　　┏━┛
 * 　　　┃　　　┃                  神兽保佑, 永无BUG!
 * 　　　┃　　　┃
 * 　　　┃　　　┃     Code is far away from bug with the animal protecting
 * 　　　┃　 　　┗━━━┓
 * 　　　┃ 　　　　　　　┣┓
 * 　　　┃ 　　　　　　　┏┛
 * 　　　┗┓┓┏━┳┓┏┛
 * 　　　　┃┫┫　┃┫┫
 * 　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 * <p>
 * <p>
 * 思维方式*热情*能力
 */
@Data
public class ReturnRequest {

    @ApiModelProperty("退货订单编码")
    private String returnOrderCode;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("退货类型： 售后退货")
    private String returnOrderType = "售后退货";

    @ApiModelProperty("类型：直送、配送、首单、首单赠送")
    private String orderType ;

    @ApiModelProperty("订单状态,完成")
    private Integer orderStatus = 12;

    @ApiModelProperty("支付状态 0未支付1已支付")
    private Integer paymentStatus = 1;

    @ApiModelProperty("是否锁定(0否1是）")
    private Integer beLock = 0;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("仓库编码")
    private String warehouseCode ;

    @ApiModelProperty("物流中心名称")
    private String transportCenterName;

    @ApiModelProperty("物流中心编码")
    private String transportCenterCode;

    @ApiModelProperty("供应商名称")
    private String supplierName;

    @ApiModelProperty("供应商编码")
    private String supplierCode;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("客户编码")
    private String customerCode;

    @ApiModelProperty("收货人")
    private String consignee;

    @ApiModelProperty("收货人手机号")
    private String consigneePhone;

    @ApiModelProperty("省编码")
    private String provinceCode;

    @ApiModelProperty("省名称")
    private String provinceName;

    @ApiModelProperty("市编码")
    private String cityCode;

    @ApiModelProperty("市名称")
    private String cityName;

    @ApiModelProperty("区编码")
    private String districtCode;

    @ApiModelProperty("区名称")
    private String districtName;

    @ApiModelProperty("详细地址")
    private String detailAddress;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("支付方式")
    private String paymentType = "转账";

    @ApiModelProperty("商品数量")
    private Long productNum;

    @ApiModelProperty("退货金额")
    private BigDecimal returnOrderAmount;

    @ApiModelProperty("发货时间")
    private Date deliveryTime;

    @ApiModelProperty("收货时间")
    private Date receivingTime;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("操作人编码")
    private String operatorCode;

    @ApiModelProperty("操作时间")
    private Date operatorTime;

    @ApiModelProperty("退货原因描述")
    private String returnReasonContent = "退货退款";

    @ApiModelProperty("商品详情")
    private List<ReturnDetailRequest> detailRequestList;

    @ApiModelProperty(value="回单确认时间")
    private String receiptTime;

    @ApiModelProperty(value="创建时间")
    private String createTime ;

    @ApiModelProperty(value = "创建人名称")
    private String createByName;

    @ApiModelProperty(value = "创建人Id")
    private String createById;

    @ApiModelProperty(value = "渠道")
    private String deptName;

    @ApiModelProperty(value = "预计退货数量")
    private Long preProductNum;
}

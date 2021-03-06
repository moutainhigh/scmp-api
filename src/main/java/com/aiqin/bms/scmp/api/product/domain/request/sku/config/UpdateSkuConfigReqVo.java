package com.aiqin.bms.scmp.api.product.domain.request.sku.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author knight.xie
 * @version 1.0
 * @className SaveSkuConfigReqVo
 * @date 2019/5/25 10:03
 */
@ApiModel("修改SKU配置信息VO")
@Data
public class UpdateSkuConfigReqVo {

    @ApiModelProperty("商品配置编号")
    private String configCode;

    @ApiModelProperty("物流中心(仓库)编码")
    private String transportCenterCode;

    @ApiModelProperty("物流中心(仓库)名称")
    private String transportCenterName;

    @ApiModelProperty("配置状态(0:再用 1:停止进货 2:停止配送 3:停止销售)")
    private Byte configStatus;

    @ApiModelProperty("到货周期")
    private Integer arrivalCycle;

    @ApiModelProperty("订货周期")
    private Integer orderCycle;

    @ApiModelProperty("基本库存天数")
    private Integer basicInventoryDay;

    @ApiModelProperty("大库存预警天数")
    private Integer largeInventoryWarnDay;

    @ApiModelProperty("大效期预警天数")
    private Integer bigEffectPeriodWarnDay;

    @ApiModelProperty("到货后周转期")
    private Integer turnoverPeriodAfterArrival;

    @ApiModelProperty(value ="申请类型 1：新增，2：修改")
    private Byte applyType;

    @ApiModelProperty("备用仓库")
    private List<SpareWarehouseReqVo> spareWarehouses;

    // 这个时候会有两个对象：this表示的当前对象，另外一个是传入对象
    public boolean compare(UpdateSkuConfigReqVo updateSkuConfigReqVo) {
        if(updateSkuConfigReqVo == this) {
            return true;
        }
        if(updateSkuConfigReqVo == null) {
            return false;
        }
        //重新进行对象属性的比较
        //可以直接利用"对象.属性"进行访问
        if(this.configCode.equals(updateSkuConfigReqVo.configCode) &&
                this.transportCenterCode.equals(updateSkuConfigReqVo.transportCenterCode) &&
            this.transportCenterName.equals(updateSkuConfigReqVo.transportCenterName) &&
                    this.configStatus.equals(updateSkuConfigReqVo.configStatus) &&
                    this.arrivalCycle==(updateSkuConfigReqVo.arrivalCycle) &&
                    this.orderCycle==(updateSkuConfigReqVo.orderCycle) &&
                    this.basicInventoryDay==(updateSkuConfigReqVo.basicInventoryDay) &&
                    this.largeInventoryWarnDay==(updateSkuConfigReqVo.largeInventoryWarnDay) &&
                    this.bigEffectPeriodWarnDay==(updateSkuConfigReqVo.bigEffectPeriodWarnDay) &&
                    this.turnoverPeriodAfterArrival==(updateSkuConfigReqVo.turnoverPeriodAfterArrival) ) {
            return true;
        }
        return false;
    }
}

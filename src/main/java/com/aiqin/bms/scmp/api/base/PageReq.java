package com.aiqin.bms.scmp.api.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @功能说明: 分页信息
 * @author: wangxu
 * @date: 2018/12/3 0003 19:54
 */

@EqualsAndHashCode(callSuper = true)
@ApiModel("分页传输对象")
@Data
public class PageReq extends PagesRequest {
    @ApiModelProperty("当前页,默认1")
    @Builder.Default
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数,默认20")
    @Builder.Default
    private Integer pageSize = 50;
}

package com.aiqin.bms.scmp.api.supplier.web.supplier;

import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.supplier.domain.request.apply.DetailApplyReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.apply.QueryApplyReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.request.apply.RequsetParamReqVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.apply.ApplyListRespVo;
import com.aiqin.bms.scmp.api.supplier.domain.response.apply.DetailRequestRespVo;
import com.aiqin.bms.scmp.api.supplier.service.ApplyService;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author knight.xie
 * @version 1.0
 * @className ApplyController
 * @date 2019/4/4 15:48

 */


@RestController
@RequestMapping("/apply")
@Api(description = "改供应商申请管理")
@Slf4j
public class ApplyController {

    @Autowired
    private ApplyService applyService;

    @PostMapping("/list")
    @ApiOperation("查询供应商申请列表")
    public HttpResponse<BasePage<ApplyListRespVo>> list(@RequestBody @Validated QueryApplyReqVo querySupplierReqVO){
        return HttpResponse.success(applyService.selectApplyList(querySupplierReqVO));
    }

    @PostMapping("/detail")
    @ApiOperation("查看供应商申请详情")
    public HttpResponse detail(@RequestBody @Validated  DetailApplyReqVo detailApplyReqVo){
        return applyService.detail(detailApplyReqVo);
    }

    @PostMapping("/cancel")
    @ApiOperation("查看供应商申请详情")
    public HttpResponse cancel(@RequestBody @Validated  DetailApplyReqVo detailApplyReqVo){
        try {
            applyService.cancel(detailApplyReqVo);
            return HttpResponse.success("撤销成功");
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.CANCEL_ERROR);
        }
    }

    @PostMapping("/getInfoByFormNo")
    @ApiOperation("根据formNo获取情接口请求")
    public HttpResponse<DetailRequestRespVo> getInfoByFormNo(@RequestBody @Validated RequsetParamReqVo requsetParamReqVo){
        return HttpResponse.successGenerics(applyService.getRequsetParam(requsetParamReqVo));
    }
}

package com.aiqin.bms.scmp.api.supplier.web.supplier.applysupplycom;

import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.supplier.domain.request.QueryApplySupplyListComReqVO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.vo.ApplySupplyCompanyReqVO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.vo.CancelApplySupplyComReqVO;
import com.aiqin.bms.scmp.api.supplier.domain.request.supplier.vo.QueryApplySupplyComReqVO;
import com.aiqin.bms.scmp.api.supplier.domain.response.ApplyComDetailRespVO;
import com.aiqin.bms.scmp.api.supplier.domain.response.ApplySupplyComApplyListRespVO;
import com.aiqin.bms.scmp.api.supplier.domain.response.supplier.ApplySupplyComDetailRespVO;
import com.aiqin.bms.scmp.api.supplier.domain.response.supplier.ApplySupplyComListRespVO;
import com.aiqin.bms.scmp.api.supplier.service.ApplySupplyComService;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @功能说明:
 * @author: wangxu
 * @date: 2018/12/3 0003 15:20
 */
@RestController
@RequestMapping("/supplyCom/apply")
@Api(description = "供货单位申请管理")
@Slf4j
public class ApplySupplyComController {
    @Autowired
    private ApplySupplyComService applySupplyComService;

    @PostMapping("/list")
    @ApiOperation("供货单位申请管理")
    public HttpResponse<BasePage<ApplySupplyComListRespVO>> listApplySupplyCompany(@RequestBody @Validated QueryApplySupplyComReqVO queryApplySupplyComReqVO){
        try {
            BasePage<ApplySupplyComListRespVO> result = applySupplyComService.getApplyList(queryApplySupplyComReqVO);
            return HttpResponse.success(result);
        } catch (Exception e) {
            return HttpResponse.failure(ResultCode.NO_HAVE_INFO_ERROR);
        }
    }

    @PostMapping("/editApply")
    @ApiOperation("修改供应商")
    public HttpResponse<Boolean> editApply(@RequestBody @Validated ApplySupplyCompanyReqVO applySupplyCompanyReqVO){
        try {
            return HttpResponse.success(applySupplyComService.editApply(applySupplyCompanyReqVO));
        } catch (BizException ex) {
            return HttpResponse.failure(ex.getMessageId());
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.ADD_ERROR);
        }
    }

    @PostMapping("/applyList")
    @ApiOperation("供货单位申请管理")
    public HttpResponse<BasePage<ApplySupplyComApplyListRespVO>> applyList(@RequestBody @Validated QueryApplySupplyListComReqVO queryApplySupplyComReqVO){
        try {
            BasePage<ApplySupplyComApplyListRespVO> result = applySupplyComService.applyList(queryApplySupplyComReqVO);
            return HttpResponse.success(result);
        } catch (Exception e) {
            return HttpResponse.failure(ResultCode.NO_HAVE_INFO_ERROR);
        }
    }

    @GetMapping("/applyView")
    public HttpResponse<ApplyComDetailRespVO> applyView(@RequestParam Long id){
        try {
            String statusTypeCode = "1";
            ApplyComDetailRespVO result = applySupplyComService.applyView(id,statusTypeCode);
            return HttpResponse.success(result);
        } catch (Exception e) {
            return HttpResponse.failure(ResultCode.NO_HAVE_INFO_ERROR);
        }
    }

    @GetMapping("/detail")
    @ApiOperation(value = "查看详情")
    public HttpResponse<ApplySupplyComDetailRespVO> getApplySupplyComDetail(@RequestParam @ApiParam("供货单位申请编码,必传") String applyCode){
        try {
            ApplySupplyComDetailRespVO applySupplyComDetailRespVO = applySupplyComService.getApplySupplyComDetail(applyCode);
            return HttpResponse.success(applySupplyComDetailRespVO);
        } catch (Exception e) {
            return HttpResponse.failure(ResultCode.NO_HAVE_INFO_ERROR);
        }
    }

    @PutMapping("/cancel")
    @ApiOperation("撤销")
    public HttpResponse cancelApplySupplyCom(@RequestBody @Validated CancelApplySupplyComReqVO cancelApplySupplyComReqVO){
        try {
            applySupplyComService.cancelApply(cancelApplySupplyComReqVO);
            return HttpResponse.success();
        } catch (Exception e) {
            return HttpResponse.failure(ResultCode.CANCEL_ERROR);
        }
    }

    @DeleteMapping("/deleteApply")
    @ApiOperation("待申请删除")
    public HttpResponse deleteApply(@RequestParam Long id){
        log.error("待申请删除 - /deleteApply 参数：[{}]",id);
        try {
            return HttpResponse.success(applySupplyComService.deleteApply(id));
        } catch (Exception e) {
            return HttpResponse.failure(ResultCode.CANCEL_ERROR);
        }
    }
}

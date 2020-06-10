package com.aiqin.bms.scmp.api.purchase.web.order;

import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.base.PageResData;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.product.domain.request.ReturnDLReq;
import com.aiqin.bms.scmp.api.product.domain.request.ReturnReq;
import com.aiqin.bms.scmp.api.product.domain.request.returngoods.ReturnReceiptReqVO;
import com.aiqin.bms.scmp.api.purchase.domain.pojo.returngoods.ReturnOrderInfo;
import com.aiqin.bms.scmp.api.purchase.domain.request.returngoods.*;
import com.aiqin.bms.scmp.api.purchase.domain.response.returngoods.*;
import com.aiqin.bms.scmp.api.purchase.service.ReturnGoodsService;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.Project;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Description:
 *
 * @author: NullPointException
 * @date: 2019-06-13
 * @time: 17:24
 */
@RestController
@Slf4j
@Api(description = "退货api")
@RequestMapping("/returnGoods")
public class ReturnGoodsController {
    @Autowired
    private ReturnGoodsService returnGoodsService;

    @ApiOperation("退货单同步")
    @PostMapping("/save")
    public HttpResponse<Boolean> saveReturnOrder(@RequestBody @Valid List<ReturnOrderInfoReqVO> reqVO){
        log.info("ReturnGoodsController---saveReturnOrder---param：[{}]", JSONObject.toJSONString(reqVO));
        try {
            return HttpResponse.success(returnGoodsService.save(reqVO));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("运营中台推送退货单")
    @PostMapping("/record/return")
    public HttpResponse<String> record(@RequestBody ReturnReq reqVO){
        log.info("运营中台推送退货单参数：[{}]", JsonUtil.toJson(reqVO));
        return returnGoodsService.record(reqVO);
    }

    @ApiOperation("DL退货单调用接口")
    @PostMapping("recordDL/return")
    public HttpResponse recordDL(@RequestBody ReturnDLReq reqVO){
        log.info("ReturnGoodsController---saveReturnOrder---param：[{}]", JSONObject.toJSONString(reqVO));
        try {
            return HttpResponse.success(returnGoodsService.recordDL(reqVO));
        } catch (BizException e){
            return HttpResponse.failure(MessageId.create(Project.SCMP_API, -1, e.getMessage()));
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(MessageId.create(Project.SCMP_API, -1, e.getMessage()));
        }
    }

    @ApiOperation("退货单管理")
    @PostMapping("/returnOrderManagement")
    public HttpResponse<BasePage<QueryReturnOrderManagementRespVO>> returnOrderManagement(@RequestBody QueryReturnOrderManagementReqVO reqVO){
        log.info("ReturnGoodsController---returnOrderManagement---param：[{}]", JSONObject.toJSONString(reqVO));
        try {
            return HttpResponse.success(returnGoodsService.returnOrderManagement(reqVO));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("直供退货单管理")
    @PostMapping("/directReturnOrderManagement")
    public HttpResponse<BasePage<QueryReturnOrderManagementRespVO>> directReturnOrderManagement(@RequestBody QueryReturnOrderManagementReqVO reqVO){
        log.info("ReturnGoodsController---returnOrderManagement---param：[{}]", JSONObject.toJSONString(reqVO));
        try {
            return HttpResponse.success(returnGoodsService.directReturnOrderManagement(reqVO));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("退货详情")
    @GetMapping("/returnOrderDetail")
    public HttpResponse<ReturnOrderDetailRespVO> returnOrderDetail(@RequestParam String code){
        log.info("ReturnGoodsController---returnOrderDetail---param：[{}]", code);
        try {
            return HttpResponse.success(returnGoodsService.returnOrderDetail(code));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("更改状态")
    @GetMapping("/changeOrderStatus")
    public HttpResponse<Boolean> changeOrderStatus(@RequestParam String code,@RequestParam Integer status){
        log.info("ReturnGoodsController---changeOrderStatus---param：[{}] param：[{}]", code,status);
        try {
            return HttpResponse.success(returnGoodsService.changeOrderStatus(code,status));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("直送退货详情")
    @GetMapping("/directReturnOrderDetail")
    public HttpResponse<ReturnOrderDetailRespVO> directReturnOrderDetail(@RequestParam String code){
        log.info("ReturnGoodsController---returnOrderDetail---param：[{}]", code);
        try {
            return HttpResponse.success(returnGoodsService.directReturnOrderDetail(code));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("退货收货")
    @PostMapping("/returnReceipt")
    public HttpResponse<Boolean> returnReceipt(@RequestBody List<ReturnReceiptReqVO> reqVO, @RequestParam String code){
        log.info("ReturnGoodsController---returnOrderDetail---param：[{}]", code);
        try {
            return HttpResponse.success(returnGoodsService.returnReceipt(reqVO,code));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("退货列表")
    @PostMapping("/list")
    public HttpResponse<PageResData<ReturnOrderInfo>> returnInspection(@RequestBody ReturnGoodsRequest request){
        log.info("调用退货单列表参数：{}", JsonUtil.toJson(request));
        return returnGoodsService.returnOrderList(request);
    }

    @ApiOperation("验货详情")
    @GetMapping("/inspectionDetail")
    public HttpResponse<InspectionDetailRespVO> inspectionDetail(@RequestParam String code){
        log.info("ReturnGoodsController---inspectionDetail---param：[{}]", code);
        try {
            return HttpResponse.success(returnGoodsService.inspectionDetail(code));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }
    @ApiOperation("验货保存")
    @PostMapping("/inspection/save")
    public HttpResponse<Boolean> saveReturnInspection(@RequestBody ReturnInspectionRequest request){
        log.info("退货验货提交参数：[{}]", JsonUtil.toJson(request));
        return returnGoodsService.saveReturnInspection(request);
    }

    @ApiOperation("验货查看")
    @GetMapping("/inspectionView")
    public HttpResponse<InspectionViewRespVO> inspectionView(@RequestParam String code){
        log.info("ReturnGoodsController---inspectionView---param：[{}]", code);
        try {
            return HttpResponse.success(returnGoodsService.inspectionView(code));
        } catch (BizException e){
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }
}

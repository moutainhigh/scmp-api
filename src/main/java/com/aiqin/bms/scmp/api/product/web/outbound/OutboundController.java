package com.aiqin.bms.scmp.api.product.web.outbound;

import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.product.domain.EnumReqVo;
import com.aiqin.bms.scmp.api.product.domain.pojo.OutboundBatch;
import com.aiqin.bms.scmp.api.product.domain.request.BoundRequest;
import com.aiqin.bms.scmp.api.product.domain.request.order.OrderInfo;
import com.aiqin.bms.scmp.api.product.domain.request.outbound.OutboundCallBackReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.outbound.OutboundReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.outbound.QueryOutboundReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.returnsupply.ReturnSupplyToOutBoundReqVo;
import com.aiqin.bms.scmp.api.product.domain.response.outbound.OutboundResVo;
import com.aiqin.bms.scmp.api.product.domain.response.outbound.OutboundResponse;
import com.aiqin.bms.scmp.api.product.domain.response.outbound.QueryOutboundResVo;
import com.aiqin.bms.scmp.api.product.service.OutboundService;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kt.w
 * @date 2019/1/5
 */
@Slf4j
@RestController
@Api(tags = "库房出库管理")
@RequestMapping("/product/outbound")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    @ApiOperation("查询出库单列表详情")
    @PostMapping("/getOutboundList")
    public HttpResponse<BasePage<QueryOutboundResVo>> getOutboundList(@RequestBody QueryOutboundReqVo vo) {
        return HttpResponse.success(outboundService.getOutboundList(vo));
    }

    @ApiOperation("查询出库信息")
    @PostMapping("/info/by/search")
    public HttpResponse<OutboundResponse> selectOutBoundInfoByBoundSearch(@RequestBody BoundRequest boundRequest) {
        return HttpResponse.success(outboundService.selectOutBoundInfoByBoundSearch(boundRequest));
    }

    @ApiOperation("通过id获取出库单")
    @GetMapping("/view")
    public HttpResponse<OutboundResVo> view(@RequestParam @ApiParam(value = "主键id",required = true) Long id){
        try{
            return HttpResponse.success(outboundService.view(id));
        }catch (Exception e){
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("保存出库单")
    @PostMapping("/save")
    public HttpResponse<Integer>save(@RequestBody OutboundReqVo stockReqVO){
        return HttpResponse.success(outboundService.saveOutBoundInfo(stockReqVO));
    }

    @ApiOperation("查询出库类型")
    @GetMapping("/getOutboundType")
    public HttpResponse<List<EnumReqVo>> getOutboundType(){
        return HttpResponse.success(outboundService.getOutboundType());
    }

    @ApiOperation("退供生成出库单")
    @PostMapping("/returnSupply/save")
    public HttpResponse<Integer> returnSupplySave(@RequestBody ReturnSupplyToOutBoundReqVo req){
        return HttpResponse.success(outboundService.returnSupplySave(req));
    }

    @ApiOperation("订单生成出库单")
    @PostMapping("/order/save")
    public HttpResponse<Integer> orderSave(@RequestBody List<OrderInfo> req){
        return HttpResponse.success(outboundService.orderSave(req));
    }

    @ApiOperation("出库单回调接口")
     @PostMapping("/workFlowCallBack")
    public HttpResponse workFlowCallBack(@RequestBody OutboundCallBackReqVo reqVo) {
        return HttpResponse.success(outboundService.workFlowCallBack(reqVo));
    }

    @ApiOperation("根据出库单号查询出库商品批次详情")
    @GetMapping("/getInfoByOderCode")
    public HttpResponse<OutboundBatch> selectOutboundBatchInfoByOutboundOderCode(@RequestParam(value = "outbound_oder_code")String outboundOderCode,
                                                                  @RequestParam(value = "page_size", required = false)Integer pageSize,
                                                                  @RequestParam(value = "page_no", required = false)Integer pageNo){
        return outboundService.selectOutboundBatchInfoByOutboundOderCode(new OutboundBatch(outboundOderCode, pageSize, pageNo));
    }

    @ApiOperation("pushWms")
    @GetMapping("/pushWms")
    public HttpResponse pushWms(String code) {
        outboundService.pushRejectWms(code);
        return HttpResponse.success();
    }

    @ApiOperation("出库回调根据类型回传给来源单号状态测试")
    @PostMapping("/returnSource/test")
    public HttpResponse returnSource(@RequestBody OutboundCallBackReqVo requestVo) {
        outboundService.returnSource(5980L,requestVo);
        return HttpResponse.success();
    }
}
package com.aiqin.bms.scmp.api.product.web.price;

import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.domain.request.changeprice.*;
import com.aiqin.bms.scmp.api.product.domain.response.changeprice.*;
import com.aiqin.bms.scmp.api.product.service.ProductSkuChangePriceService;
import com.aiqin.bms.scmp.api.util.DateUtils;
import com.aiqin.bms.scmp.api.util.IdSequenceUtils;
import com.aiqin.ground.util.json.JsonUtil;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@Api(description = "变价api")
@RequestMapping("/product/changePrice")
public class ProductSkuChangePriceController {
    @Autowired
    private ProductSkuChangePriceService productSkuChangePriceService;

    @PostMapping("/save")
    @ApiOperation("保存")
    public HttpResponse<Boolean> save(@RequestBody ProductSkuChangePriceReqVO reqVO) {
        log.info("ProductSkuChangePriceController---save---入参：[{}]", JSON.toJSONString(reqVO));
        try {
            return HttpResponse.success(productSkuChangePriceService.save(reqVO));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @GetMapping("/exportChangePriceData")
    @ApiOperation("/导出价格")
    public HttpResponse<Boolean> exportChangePriceData(HttpServletResponse resp, String code){
        log.info("SkuInfoController---exportSku---入参：[{}]",code);
        try {
            return HttpResponse.success(productSkuChangePriceService.exportChangePriceData(resp,code));
        } catch (BizException e) {
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e) {
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @GetMapping("/view")
    @ApiOperation("查看")
    public HttpResponse<ProductSkuChangePriceRespVO> view(@RequestParam String code) {
        log.info("ProductSkuChangePriceController---view---入参：[{}]", code);
        try {
            return HttpResponse.success(productSkuChangePriceService.view(code));
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }


    @GetMapping("/getCodeByFormNo")
    @ApiOperation("根据FormNo获取申请编码")
    public HttpResponse<String> getApplyCodeByFormNo(@RequestParam String formNo) {
        log.info("ProductSkuChangePriceController---getCodeByFormNo---入参：[{}]", formNo);
        try {
            return HttpResponse.success(productSkuChangePriceService.getApplyCodeByFormNo(formNo));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @GetMapping("/editView")
    @ApiOperation("编辑时获取数据")
    public HttpResponse<ProductSkuChangePriceRespVO> editView(@RequestParam String code) {
        log.info("ProductSkuChangePriceController---editView---入参：[{}]", code);
        try {
            return HttpResponse.success(productSkuChangePriceService.editView(code));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }
    @PostMapping("/update")
    @ApiOperation("更新")
    public HttpResponse<ProductSkuChangePriceRespVO> update(@RequestBody ProductSkuChangePriceReqVO reqVO) {
        log.info("ProductSkuChangePriceController---update---入参：[{}]", JSONObject.toJSONString(reqVO));
        try {
            return HttpResponse.success(productSkuChangePriceService.update(reqVO));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @PostMapping("/list")
    @ApiOperation("列表")
    public HttpResponse<BasePage<QueryProductSkuChangePriceRespVO>> list(@RequestBody QueryProductSkuChangePriceReqVO reqVO) {
        log.info("ProductSkuChangePriceController---list---入参：[{}]", JSONObject.toJSONString(reqVO));
        try {
            reqVO.setFlag(true);
            return HttpResponse.success(productSkuChangePriceService.list(reqVO));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @PostMapping("/edit/list")
    @ApiOperation("供应商变价列表")
    public HttpResponse<BasePage<QueryProductSkuChangePriceRespVO>> listForEdit(@RequestBody QueryProductSkuChangePriceReqVO reqVO) {
        log.info("ProductSkuChangePriceController---list---入参：[{}]", JSONObject.toJSONString(reqVO));
        try {
            //默认查状态为5的，5是供应商提交过来的。可以编辑
            reqVO.setApplyStatus(5);
            return HttpResponse.success(productSkuChangePriceService.list(reqVO));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @PostMapping("/querySkuList")
    @ApiOperation("根据条件查询sku列表")
    public HttpResponse<BasePage<QuerySkuInfoRespVO>> querySkuList(@RequestBody @Valid QuerySkuInfoReqVO reqVO){
        log.info("ProductSkuChangePriceController---querySkuList---入参：[{}]", JSON.toJSONString(reqVO));
        try {
            return HttpResponse.success(productSkuChangePriceService.getSkuListByQueryVO(reqVO));
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR,ResultCode.SYSTEM_ERROR.getMessage());
        }
    }

    @PostMapping("/querySkuBatchList")
    @ApiOperation("根据条件查询sku批次列表")
    public HttpResponse<BasePage<QuerySkuInfoRespVO>> querySkuBatchList(@RequestBody @Valid QuerySkuInfoReqVO reqVO){
        log.info("ProductSkuChangePriceController---querySkuBatchList---入参：[{}]", JsonUtil.toJson(reqVO));
        try {
            return HttpResponse.success(productSkuChangePriceService.querySkuBatchList(reqVO));
        } catch (Exception e) {
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR,ResultCode.SYSTEM_ERROR.getMessage());
        }
    }

    @GetMapping("/cancel")
    @ApiOperation("撤销")
    public HttpResponse<Boolean> cancel(@RequestParam String code) {
        log.info("ProductSkuChangePriceController---cancel---入参：[{}]", code);
        try {
            return HttpResponse.success(productSkuChangePriceService.cancelData(code));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("采购价导入")
    @PostMapping("/importForPurchasePrice")
    public HttpResponse<List<QuerySkuInfoRespVOForIm>> importForPurchasePrice(MultipartFile file, String purchaseGroupCode,String changePriceType){
        try {
            return HttpResponse.success(productSkuChangePriceService.importForChangePrice(file,purchaseGroupCode,changePriceType));
        } catch (BizException e) {
            log.error(e.getMessageId().getMessage());
            return HttpResponse.failure(e.getMessageId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }

    }


    @ApiOperation("2年内价格波动数据")
    @GetMapping("/getPriceJog")
    public HttpResponse<List<PriceJog>> importProductSkuChangePrice(@RequestParam String skuCode){
        try {
            return HttpResponse.success(productSkuChangePriceService.getPriceJog(skuCode));
        } catch (BizException e) {
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e){
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("变价测算")
    @PostMapping("/priceMeasurement")
    public HttpResponse<PriceMeasurementRespVO> priceMeasurement(@RequestBody List<PriceMeasurementReqVO> req){
        try {
            //获取上个月份
            return HttpResponse.success(productSkuChangePriceService.priceMeasurement(req, DateUtils.getLastMonthString(new Date())));
        } catch (BizException e) {
            return HttpResponse.failure(e.getMessageId());
        }catch (Exception e){
            log.error(Global.ERROR, e);
            return HttpResponse.failure(ResultCode.SYSTEM_ERROR);
        }
    }

    @ApiOperation("测试生成id")
    @PostMapping("/getid")
    public Long getid(){
        IdSequenceUtils idSequenceUtils = IdSequenceUtils.getInstance();
        return idSequenceUtils.nextId();
    }
}
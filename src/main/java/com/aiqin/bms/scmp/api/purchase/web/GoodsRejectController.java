package com.aiqin.bms.scmp.api.purchase.web;

import com.aiqin.bms.scmp.api.purchase.domain.request.RejectApplyRequest;
import com.aiqin.bms.scmp.api.purchase.service.GoodsRejectService;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
@Api("退供controller")
@RequestMapping("/reject")
@RestController
@SuppressWarnings("checkout")
public class GoodsRejectController {

    @Resource
    private GoodsRejectService goodsRejectService;

    @PostMapping("/apply/list")
    @ApiOperation(value = "退供单列表")
    public HttpResponse<List<RejectApplyRequest>> rejectApplyList(@RequestBody RejectApplyRequest rejectApplyRequest) {
        return goodsRejectService.rejectApplyList(rejectApplyRequest);
    }

}

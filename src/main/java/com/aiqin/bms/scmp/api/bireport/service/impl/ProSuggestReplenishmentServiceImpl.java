package com.aiqin.bms.scmp.api.bireport.service.impl;

import com.aiqin.bms.scmp.api.bireport.dao.ProSuggestReplenishmentDao;
import com.aiqin.bms.scmp.api.bireport.domain.response.editpurchase.ProReplenishmentOutStockRespVo;
import com.aiqin.bms.scmp.api.bireport.service.ProSuggestReplenishmentService;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProSuggestReplenishmentServiceImpl implements ProSuggestReplenishmentService{

    @Autowired
    private ProSuggestReplenishmentDao proSuggestReplenishmentDao;

    /**
     * 查询14大A品建议补货
     * @return
     */
    @Override
    public ProReplenishmentOutStockRespVo selectSuggestReplenishmentByPro() {
        int proStatus = 1;
        List<String> skuCodes = proSuggestReplenishmentDao.selectSuggestReplenishmentByPro(proStatus);
        ProReplenishmentOutStockRespVo proReplenishmentOutStockRespVo = new ProReplenishmentOutStockRespVo();
        proReplenishmentOutStockRespVo.setSkuCode(skuCodes);
        return proReplenishmentOutStockRespVo;
    }

    /**
     * 获取畅销建议补货skuCode
     * @return
     */
    @Override
    public ProReplenishmentOutStockRespVo selectSuggestReplenishmentBySell() {
        int proStatus = 1;
        List<String> skuCodes = proSuggestReplenishmentDao.selectSuggestReplenishmentBySell(proStatus);
        ProReplenishmentOutStockRespVo proReplenishmentOutStockRespVo = new ProReplenishmentOutStockRespVo();
        proReplenishmentOutStockRespVo.setSkuCode(skuCodes);
        return proReplenishmentOutStockRespVo;
    }

    /**
     * 查询14大A品缺货
     * @return
     */
    @Override
    public ProReplenishmentOutStockRespVo selectOutStockByPro() {
        int proStatus = 1;
        int continuousDays = 0;
        List<String> skuCodes = proSuggestReplenishmentDao.selectOutStockByPro(proStatus,continuousDays);
        ProReplenishmentOutStockRespVo proReplenishmentOutStockRespVo = new ProReplenishmentOutStockRespVo();
        proReplenishmentOutStockRespVo.setSkuCode(skuCodes);
        return proReplenishmentOutStockRespVo;
    }

    /**
     * 获取畅销缺货
     * @return
     */
    @Override
    public ProReplenishmentOutStockRespVo selectOutStockBySell() {
        int proStatus = 1;
        int continuousDays = 0;
        List<String> skuCodes = proSuggestReplenishmentDao.selectOutStockBySell(proStatus,continuousDays);
        ProReplenishmentOutStockRespVo proReplenishmentOutStockRespVo = new ProReplenishmentOutStockRespVo();
        proReplenishmentOutStockRespVo.setSkuCode(skuCodes);
        return proReplenishmentOutStockRespVo;
    }
}

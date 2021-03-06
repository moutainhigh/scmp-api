package com.aiqin.bms.scmp.api.product.service.impl;

import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.domain.pojo.Stock;
import com.aiqin.bms.scmp.api.product.service.PriceJobService;
import com.aiqin.bms.scmp.api.product.service.StockService;
import com.aiqin.bms.scmp.api.product.service.TaxCostLogService;
import com.aiqin.bms.scmp.api.util.PriceThreadDo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PriceJobServiceImpl implements PriceJobService {
    @Autowired
    private StockService stockService;
    @Autowired
    private TaxCostLogService taxCostLogService;
    @Override
    public void price(Stock stock) {
        try {
            //根据库房查询sku
           List<Stock> stocks= stockService.selectListByWareHouseCode(stock);
            stocks.forEach(st->{
                taxCostLogService.log(st);
            });
        } catch (Exception e) {
            log.error(Global.ERROR, e);
        }finally {
            PriceThreadDo.subThreadNum();
        }
    }
}

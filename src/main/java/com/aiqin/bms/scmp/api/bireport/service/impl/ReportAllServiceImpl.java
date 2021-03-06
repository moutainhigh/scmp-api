package com.aiqin.bms.scmp.api.bireport.service.impl;

import com.aiqin.bms.scmp.api.base.PageImportResData;
import com.aiqin.bms.scmp.api.bireport.dao.ReportAllDao;
import com.aiqin.bms.scmp.api.bireport.dao.ReportDao;
import com.aiqin.bms.scmp.api.bireport.domain.request.*;
import com.aiqin.bms.scmp.api.bireport.domain.response.*;
import com.aiqin.bms.scmp.api.bireport.service.ReportAllService;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ReportAllServiceImpl implements ReportAllService {

    @Autowired
    private ReportAllDao reportAllDao;

    @Autowired
    private ReportDao reportDao;


    /**
     *  供应商到货率
     * @param supplierArrivalRateReqVo
     * @return
     */
    @Override
    public PageImportResData<SupplierArrivalRateRespVo> selectSupplierArrivalRate(SupplierArrivalRateReqVo supplierArrivalRateReqVo) {
        try {
            List<SupplierArrivalRateRespVo> supplierArrivalRateRespVos = reportAllDao.selectSupplierArrivalRate(supplierArrivalRateReqVo);
            SupplierArrivalRateRespVo supplierArrivalRateRespVo = reportAllDao.sumSupplierArrivalRate(supplierArrivalRateReqVo);
            return new PageImportResData<>(supplierArrivalRateRespVo,supplierArrivalRateRespVos);
        } catch (Exception ex) {
            log.error("查询供应商到货率失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  批次商品进销存
     * @param goodsBuySalesReqVo
     * @return
     */
    @Override
    public List<GoodsBuySalesRespVo> selectGoodsBuySales(GoodsBuySalesReqVo goodsBuySalesReqVo) {
        try {
            List<GoodsBuySalesRespVo> goodsBuySalesRespVos = reportAllDao.selectGoodsBuySales(goodsBuySalesReqVo);
            for (GoodsBuySalesRespVo goodsBuySalesRespVo : goodsBuySalesRespVos) {
                GoodsBuySalesRespVo goodsBuySalesRespVosOne = reportAllDao.selectGoodsSkuNameByCodeOne(goodsBuySalesRespVo.getCategoryCodeOne());
                GoodsBuySalesRespVo goodsBuySalesRespVosTwo = reportAllDao.selectGoodsSkuNameByCodeTwo(goodsBuySalesRespVo.getCategoryCodeTwo());
                GoodsBuySalesRespVo goodsBuySalesRespVosThree = reportAllDao.selectGoodsSkuNameByCodeThree(goodsBuySalesRespVo.getCategoryCodeThree());
                GoodsBuySalesRespVo goodsBuySalesRespVosFour = reportAllDao.selectGoodsSkuNameByCode(goodsBuySalesRespVo.getCategoryCode());
                if(goodsBuySalesRespVosOne != null){ goodsBuySalesRespVo.setCategoryNameOne(goodsBuySalesRespVosOne.getCategoryNameOne()); }
                if(goodsBuySalesRespVosTwo != null){ goodsBuySalesRespVo.setCategoryNameTwo(goodsBuySalesRespVosTwo.getCategoryNameTwo()); }
                if(goodsBuySalesRespVosThree != null){ goodsBuySalesRespVo.setCategoryNameThree(goodsBuySalesRespVosThree.getCategoryNameThree()); }
                if(goodsBuySalesRespVosFour != null){goodsBuySalesRespVo.setCategoryName(goodsBuySalesRespVosFour.getCategoryName());}
            }
            return goodsBuySalesRespVos;
        } catch (Exception ex) {
            log.error("查询批次商品进销存失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  赠品进销存
     * @param giftsBuySalesReqVo
     * @return
     */
    @Override
    public List<GiftsBuySalesRespVo> selectGiftsBuySales(GiftsBuySalesReqVo giftsBuySalesReqVo) {
        try {
            List<GiftsBuySalesRespVo> giftsBuySalesRespVos = reportAllDao.selectGiftsBuySales(giftsBuySalesReqVo);
            for (GiftsBuySalesRespVo giftsBuySalesRespVo : giftsBuySalesRespVos) {
                GiftsBuySalesRespVo giftsBuySalesRespVosOne = reportAllDao.selectGiftsSkuNameByCodeOne(giftsBuySalesRespVo.getCategoryCodeOne());
                GiftsBuySalesRespVo giftsBuySalesRespVosTwo = reportAllDao.selectGiftsSkuNameByCodeTwo(giftsBuySalesRespVo.getCategoryCodeTwo());
                GiftsBuySalesRespVo giftsBuySalesRespVosThree = reportAllDao.selectGiftsSkuNameByCodeThree(giftsBuySalesRespVo.getCategoryCodeThree());
                GiftsBuySalesRespVo giftsBuySalesRespVosFour = reportAllDao.selectGiftsSkuNameByCode(giftsBuySalesRespVo.getCategoryCode());
                if(giftsBuySalesRespVosOne != null){ giftsBuySalesRespVo.setCategoryNameOne(giftsBuySalesRespVosOne.getCategoryNameOne()); }
                if(giftsBuySalesRespVosTwo != null){ giftsBuySalesRespVo.setCategoryNameTwo(giftsBuySalesRespVosTwo.getCategoryNameTwo()); }
                if(giftsBuySalesRespVosThree != null){ giftsBuySalesRespVo.setCategoryNameThree(giftsBuySalesRespVosThree.getCategoryNameThree()); }
                if(giftsBuySalesRespVosFour != null){giftsBuySalesRespVo.setCategoryName(giftsBuySalesRespVosFour.getCategoryName());}
            }
            return giftsBuySalesRespVos;
        } catch (Exception ex) {
            log.error("查询赠品进销存失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  供应商退货(退供)
     * @param supplierReturnReqVo
     * @return
     */
    @Override
    public PageImportResData<SupplierReturnRespVo> selectSupplierReturn(SupplierReturnReqVo supplierReturnReqVo) {
        try {
            List<SupplierReturnRespVo> supplierReturnRespVos = reportAllDao.selectSupplierReturn(supplierReturnReqVo);
            SupplierReturnRespVo supplierReturnRespVoSum = reportDao.sumSupplierReturn(supplierReturnReqVo);
            return new PageImportResData<>(supplierReturnRespVoSum,supplierReturnRespVos);
        } catch (Exception ex) {
            log.error("查询供应商退货失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  新品批次动销率
     * @param newProductBatchMovingRateReqVo
     * @return
     */
    @Override
    public List<NewProductBatchMovingRateRespVo> selectNewProductBatchMovingRate(NewProductBatchMovingRateReqVo newProductBatchMovingRateReqVo) {
        try {
            List<NewProductBatchMovingRateRespVo> newProductBatchMovingRateRespVos = reportAllDao.selectNewProductBatchMovingRate(newProductBatchMovingRateReqVo);
            for (NewProductBatchMovingRateRespVo newProductBatchMovingRateRespVo : newProductBatchMovingRateRespVos) {
                NewProductBatchMovingRateRespVo newProductBatchMovingRateRespVosOne = reportAllDao.selectNewProductSkuNameByCodeOne(newProductBatchMovingRateRespVo.getCategoryCodeOne());
                NewProductBatchMovingRateRespVo newProductBatchMovingRateRespVosTwo = reportAllDao.selectNewProductSkuNameByCodeTwo(newProductBatchMovingRateRespVo.getCategoryCodeTwo());
                NewProductBatchMovingRateRespVo newProductBatchMovingRateRespVosThree = reportAllDao.selectNewProductSkuNameByCodeThree(newProductBatchMovingRateRespVo.getCategoryCodeThree());
                NewProductBatchMovingRateRespVo newProductBatchMovingRateRespVosFour = reportAllDao.selectNewProductSkuNameByCode(newProductBatchMovingRateRespVo.getCategoryCode());
                if(newProductBatchMovingRateRespVosOne != null){ newProductBatchMovingRateRespVo.setCategoryNameOne(newProductBatchMovingRateRespVosOne.getCategoryNameOne()); }
                if(newProductBatchMovingRateRespVosTwo != null){ newProductBatchMovingRateRespVo.setCategoryNameTwo(newProductBatchMovingRateRespVosTwo.getCategoryNameTwo()); }
                if(newProductBatchMovingRateRespVosThree != null){ newProductBatchMovingRateRespVo.setCategoryNameThree(newProductBatchMovingRateRespVosThree.getCategoryNameThree()); }
                if(newProductBatchMovingRateRespVosFour != null){newProductBatchMovingRateRespVo.setCategoryName(newProductBatchMovingRateRespVosFour.getCategoryName());}
            }
            return newProductBatchMovingRateRespVos;
        } catch (Exception ex) {
            log.error("查询新品批次动销率失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  门店复购率
     * @param storeRepurchaseRateReqVo
     * @return
     */
    @Override
    public List<StoreRepurchaseRateRespVo> selectStoreRepurchaseRate(StoreRepurchaseRateReqVo storeRepurchaseRateReqVo) {
        try {
            return reportAllDao.selectStoreRepurchaseRate(storeRepurchaseRateReqVo);
        } catch (Exception ex) {
            log.error("查询门店复购率失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  负毛利
     * @param negativeMarginReqVo
     * @return
     */
    @Override
    public PageImportResData<NegativeMarginRespVo> selectNegativeMargin(NegativeMarginReqVo negativeMarginReqVo) {
        try {
            List<NegativeMarginRespVo> negativeMarginRespVos = reportAllDao.selectNegativeMargin(negativeMarginReqVo);
            NegativeMarginRespVo negativeMarginRespVoSum = reportDao.sumNegativeMargin(negativeMarginReqVo);
            return new PageImportResData<>(negativeMarginRespVoSum,negativeMarginRespVos);
        } catch (Exception ex) {
            log.error("查询负毛利失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  建议补货
     * @param suggestReplenishmentReqVo
     * @return
     */
    @Override
    public List<SuggestReplenishmentRespVo> selectSuggestReplenishment(SuggestReplenishmentReqVo suggestReplenishmentReqVo) {
        try {
            return reportAllDao.selectSuggestReplenishment(suggestReplenishmentReqVo);
        } catch (Exception ex) {
            log.error("查询建议补货失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  低库存
     * @param highLowInventoryReqVo
     * @return
     */
    @Override
    public PageImportResData<LowInventoryRespVo> selectLowInventory(HighLowInventoryReqVo highLowInventoryReqVo) {
        try {
            List<LowInventoryRespVo> lowInventoryRespVos = reportAllDao.selectLowInventory(highLowInventoryReqVo);
            LowInventoryRespVo lowInventoryRespVoSum = reportDao.sumLowInventory(highLowInventoryReqVo);
            return new PageImportResData<>(lowInventoryRespVoSum,lowInventoryRespVos);
        } catch (Exception ex) {
            log.error("查询低库存失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  高库存
     * @param highLowInventoryReqVo
     * @return
     */
    @Override
    public PageImportResData<HighInventoryRespVo> selectHighInventory(HighLowInventoryReqVo highLowInventoryReqVo) {
        try {
            List<HighInventoryRespVo> highInventoryRespVos = reportAllDao.selectHighInventory(highLowInventoryReqVo);
            HighInventoryRespVo highInventoryRespVoSum = reportDao.sumHighInventory(highLowInventoryReqVo);
            return new PageImportResData<>(highInventoryRespVoSum,highInventoryRespVos);
        } catch (Exception ex) {
            log.error("查询高库存失败");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  品牌促销
     * @param brandSaleReqVo
     * @return
     */
    @Override
    public PageImportResData<BrandSaleRespVo> selectBrandSale(CategorySaleReqVo brandSaleReqVo) {
        try {
            List<BrandSaleRespVo> brandSaleRespVos = reportAllDao.selectBrandSale(brandSaleReqVo);
            BrandSaleRespVo brandSaleRespVoSum = reportDao.sumBrandSale(brandSaleReqVo);
            return new PageImportResData<>(brandSaleRespVoSum,brandSaleRespVos);
        } catch (Exception ex) {
            log.error("品牌促销");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  品类促销
     * @param categorySaleReqVo
     * @return
     */
    @Override
    public PageImportResData<CategorySaleRespVo> selectCategorySale(CategorySaleReqVo categorySaleReqVo) {
        try {
            List<CategorySaleRespVo> categorySaleRespVos = reportAllDao.selectCategorySale(categorySaleReqVo);
            CategorySaleRespVo categorySaleRespVoSum = reportDao.sumCategorySale(categorySaleReqVo);
            return new PageImportResData<>(categorySaleRespVoSum,categorySaleRespVos);
        } catch (Exception ex) {
            log.error("品类促销");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  大效期
     * @param bigEffectReqVo
     * @return
     */
    @Override
    public  List<BigEffectRespVo> selectBigEffect(BigEffectReqVo bigEffectReqVo) {
        try {
            return reportAllDao.selectBigEffect(bigEffectReqVo);
        } catch (Exception ex) {
            log.error("大效期");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  月销售情况
     * @param monthlySalesReqVo
     * @return
     */
    @Override
    public  List<MonthlySalesRespVo> selectMonthlySales(MonthlySalesReqVo monthlySalesReqVo) {
        try {
            return reportAllDao.selectMonthlySales(monthlySalesReqVo);
        } catch (Exception ex) {
            log.error("月销售情况");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  月毛利率情况
     * @param monthlyGrossMarginReqVo
     * @return
     */
    @Override
    public List<MonthlyGrossMarginRespVo> selectMonthlyGrossMargin(MonthlySalesReqVo monthlyGrossMarginReqVo) {
        try {
            return reportAllDao.selectMonthlyGrossMargin(monthlyGrossMarginReqVo);
        } catch (Exception ex) {
            log.error("月毛利率情况");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  月销售达成情况
     * @param monthSalesAchievementReqVo
     * @return
     */
    @Override
    public List<MonthSalesAchievementRespVo> selectMonthSalesAchievement(MonthSalesAchievementReqVo monthSalesAchievementReqVo) {
        try {
            return reportAllDao.selectMonthSalesAchievement(monthSalesAchievementReqVo);
        } catch (Exception ex) {
            log.error("月销售达成情况");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  月累计品类销售情况
     * @param monthCumulativeBrandSalesReqVo
     * @return
     */
    @Override
    public List<MonthCumulativeBrandSalesRespVo> selectMonthCumulativeBrandSales(MonthSalesAchievementReqVo monthCumulativeBrandSalesReqVo) {
        try {
            List<MonthCumulativeBrandSalesRespVo> monthCumulativeBrandSalesRespVos = reportAllDao.selectMonthCumulativeBrandSales(monthCumulativeBrandSalesReqVo);
            return monthCumulativeBrandSalesRespVos;
        } catch (Exception ex) {
            log.error("月累计品类销售情况");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

    /**
     *  月累计品类毛利率情况
     * @param monthCumulativeGrossProfitMarginReqVo
     * @return
     */
    @Override
    public List<MonthCumulativeGrossProfitMarginRespVo> selectMonthCumulativeGrossProfitMargin(MonthSalesAchievementReqVo monthCumulativeGrossProfitMarginReqVo) {
        try {
            return reportAllDao.selectMonthCumulativeGrossProfitMargin(monthCumulativeGrossProfitMarginReqVo);
        } catch (Exception ex) {
            log.error("月累计品类毛利率情况");
            ex.printStackTrace();
            throw new GroundRuntimeException(ex.getMessage());
        }
    }

}

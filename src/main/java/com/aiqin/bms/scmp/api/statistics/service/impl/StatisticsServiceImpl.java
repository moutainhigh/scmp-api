package com.aiqin.bms.scmp.api.statistics.service.impl;

import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.statistics.dao.*;
import com.aiqin.bms.scmp.api.statistics.domain.StatComStoreRepurchaseRate;
import com.aiqin.bms.scmp.api.statistics.domain.response.*;
import com.aiqin.bms.scmp.api.statistics.domain.response.negative.*;
import com.aiqin.bms.scmp.api.statistics.service.StatisticsService;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.druid.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author: zhao shuai
 * @create: 2019-08-19
 **/
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Resource
    private StatSupplierArrivalRateMonthlyDao statSupplierArrivalRateMonthlyDao;
    @Resource
    private StatSupplierArrivalRateYearlyDao statSupplierArrivalRateYearlyDao;
    @Resource
    private StatDeptStoreRepurchaseRateDao statDeptStoreRepurchaseRateDao;
    @Resource
    private StatComStoreRepurchaseRateDao statComStoreRepurchaseRateDao;
    @Resource
    private StatComNegativeMarginYearlyDao statComNegativeMarginYearlyDao;
//    @Resource
//    private StatComNegativeMarginQuarterlyDao statComNegativeMarginQuarterlyDao;
    @Resource
    private StatComNegativeMarginMonthlyDao statComNegativeMarginMonthlyDao;
    @Resource
    private StatDeptNegativeMarginYearlyDao statDeptNegativeMarginYearlyDao;
    @Resource
//    private StatDeptNegativeMarginQuarterlyDao statDeptNegativeMarginQuarterlyDao;
//    @Resource
    private StatDeptNegativeMarginMonthlyDao statDeptNegativeMarginMonthlyDao;

    private void currency(Integer type, String date){
        if(type == null && StringUtils.isEmpty(date)){
            type = 0;
            Calendar cale = null;
            cale = Calendar.getInstance();
            date = Integer.toString(cale.get(Calendar.YEAR));
        }
    }

    @Override
    public HttpResponse<SupplierDeliveryResponse> supplierDelivery(Integer type, String date){
       this.currency(type, date);
       // 查询年报信息
       List<SupplierDeliveryResponse> deliveryList;
       List<SupplierDeliveryRateResponse> rateList;
       if(type == 0){
           deliveryList = statSupplierArrivalRateYearlyDao.supplyArrivalYearByGroup(date);
           if(CollectionUtils.isNotEmptyCollection(deliveryList)){
               for(SupplierDeliveryResponse response:deliveryList){
                   rateList = statSupplierArrivalRateYearlyDao.supplyArrivalYearList(date, response.getSupplierCode(), response.getResponsiblePersonCode());
               }
           }
       }else {
           String year = date.substring(0, 4);
           String month = date.substring(5, date.length());
           deliveryList = statSupplierArrivalRateMonthlyDao.supplyArrivalMonthByGroup(year, month);
           if(CollectionUtils.isNotEmptyCollection(deliveryList)){
               for(SupplierDeliveryResponse response:deliveryList){
                   rateList = statSupplierArrivalRateMonthlyDao.supplyArrivalMonthList(year, month, response.getSupplierCode(), response.getResponsiblePersonCode());
               }
           }
       }
       return HttpResponse.success();
    }

    private void supplyArrivalRate(List<SupplierDeliveryRateResponse> list){
        if(CollectionUtils.isNotEmptyCollection(list)){
            SupplierDeliveryResponse delivery = new SupplierDeliveryResponse();
            Long hbSubtotalGoodsCount  = 0L, hbSubtotalGoodsAmount = 0L, hbSubtotalWarehouseCount = 0L, hbSubtotalWarehouseAmount = 0L;
            Long num = 0L;
            BigDecimal big = new BigDecimal(0);
            for(SupplierDeliveryRateResponse response:list){
                Long preInboundNum = response.getPreInboundNum() == null ? num : response.getPreInboundNum();
                Long preTaxAmount = response.getPreTaxAmount() == null ? num : response.getPreTaxAmount();
                Long praInboundNum = response.getPraInboundNum() == null ? num : response.getPraInboundNum();
                Long praTaxAmount = response.getPraTaxAmount() == null ? num : response.getPraTaxAmount();
                BigDecimal amountRate = response.getInboundAmountFillRate() == null ? big : response.getInboundAmountFillRate();
                BigDecimal goodsRate = response.getArrivalRate() == null ? big : response.getArrivalRate();
               if(response.getLogisticsCenterCode().equals(Global.HB_CODE)){
                   delivery.setHbGoodsCount(preInboundNum);
                   delivery.setHbGoodsAmount(preTaxAmount);
                   delivery.setHbAmountRate(amountRate);
                   delivery.setHbWarehouseCount(praInboundNum);
                   delivery.setHbWarehouseAmount(praTaxAmount);
                   delivery.setHbGoodsRate(goodsRate);
                   hbSubtotalGoodsCount += preInboundNum;
                   hbSubtotalGoodsAmount += preTaxAmount;
                   hbSubtotalWarehouseCount += praInboundNum;
                   hbSubtotalWarehouseAmount += praTaxAmount;
               }
            }
            delivery.setHbSubtotalGoodsCount(hbSubtotalGoodsCount);
            delivery.setHbSubtotalGoodsAmount(hbSubtotalGoodsAmount);
            delivery.setHbSubtotalWarehouseCount(hbSubtotalWarehouseCount);
            delivery.setHbSubtotalWarehouseAmount(hbSubtotalWarehouseAmount);
            delivery.setHbSubtotalAmountRate(new BigDecimal(hbSubtotalGoodsAmount).divide(new BigDecimal(hbSubtotalWarehouseAmount)));
        }
    }

    @Override
    public HttpResponse<StoreRepurchaseRateResponse> storeRepurchaseRate(String date, Integer type, String productSortCode){
        if(StringUtils.isEmpty(date) || type == null){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        List<StatComStoreRepurchaseRate> repurchaseRateList;
        StoreRepurchaseRateResponse comRepurchase;
        List<StoreRepurchaseRateSubtotalResponse> deptList = new ArrayList<>();
        StoreRepurchaseRateSubtotalResponse deptRepurchase;
        Long year = Long.valueOf(date.substring(0, 4));
        Long month = Long.valueOf(date.substring(5, date.length()));
        if(type.equals(Global.COMPANY)){
            // 计算公司合计
            comRepurchase = statComStoreRepurchaseRateDao.storeRepurchaseSum(year, month);
            if(comRepurchase != null){
                // 查询该公司部门
                List<StatComStoreRepurchaseRate> sortList = statComStoreRepurchaseRateDao.storeRepurchaseBySort(year, month);
                for(StatComStoreRepurchaseRate sort:sortList){
                    // 计算各部门的小计
                    deptRepurchase = statComStoreRepurchaseRateDao.storeRepurchaseByDeptSum(year, month, sort.getProductSortCode());
                    repurchaseRateList = statComStoreRepurchaseRateDao.storeRepurchaseList(year, month, sort.getProductSortCode());
                    if(CollectionUtils.isNotEmptyCollection(repurchaseRateList)){
                        deptRepurchase.setSubtotalList(repurchaseRateList);
                    }
                    if(deptRepurchase.getRepurchaseNum() == null || deptRepurchase.getRepurchaseNum() == 0){
                        deptRepurchase.setRepurchaseRate(new BigDecimal(0));
                    }else {
                        deptRepurchase.setRepurchaseRate(new BigDecimal(deptRepurchase.getRepurchaseNum()).divide(new BigDecimal(deptRepurchase.getPurchaseNum())));
                    }
                    deptRepurchase.setProductSortCode(sort.getProductSortCode());
                    deptRepurchase.setProductSortName(sort.getProductSortName());
                    deptList.add(deptRepurchase);
                }
                if(comRepurchase.getRepurchaseNum() == null || comRepurchase.getRepurchaseNum() == 0){
                    comRepurchase.setRepurchaseRate(new BigDecimal(0));
                }else {
                    comRepurchase.setRepurchaseRate(new BigDecimal(comRepurchase.getRepurchaseNum()).divide(new BigDecimal(comRepurchase.getPurchaseNum())));
                }
                comRepurchase.setSumList(deptList);
            }
            return HttpResponse.success(comRepurchase);
        }else {
            if(StringUtils.isEmpty(productSortCode)){
                return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
            }
            deptRepurchase = statDeptStoreRepurchaseRateDao.storeRepurchaseSum(year, month, productSortCode);
            if(deptRepurchase != null){
                // 计算部门各省
                repurchaseRateList = statDeptStoreRepurchaseRateDao.storeRepurchaseList(year, month, productSortCode);
                if(CollectionUtils.isNotEmptyCollection(repurchaseRateList)){
                    deptRepurchase.setSubtotalList(repurchaseRateList);
                }
                if(deptRepurchase.getRepurchaseNum() == null || deptRepurchase.getRepurchaseNum() == 0){
                    deptRepurchase.setRepurchaseRate(new BigDecimal(0));
                }else {
                    deptRepurchase.setRepurchaseRate(new BigDecimal(deptRepurchase.getRepurchaseNum()).divide(new BigDecimal(deptRepurchase.getPurchaseNum())));
                }
            }
            return HttpResponse.success(deptRepurchase);
        }
    }

    @Override
    public HttpResponse negativeGross(String date, Integer type, Integer reportType, Integer seasonType, String productSortCode){
        NegativeSumResponse sumResponse = new NegativeSumResponse();
        if(StringUtils.isEmpty(date) || type == null || reportType == null){
            LOGGER.info("参数缺失");
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        // 计算公司的负毛利统计
        if(type.equals(Global.COMPANY)){
            if(reportType.equals(Global.ANNUAL_REPORT)){
                Long year = Long.valueOf(date);
                // 计算公司的负毛利统计- 年报
                sumResponse = this.companyYear(year, sumResponse);
            }else if(reportType.equals(Global.QUARTERLY_REPORT)){
                // 计算公司的负毛利统计- 季报
                if(seasonType == null){
                    return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
                }

            }else if(reportType.equals(Global.MONTHLY_REPORT)){
                // 计算公司的负毛利统计- 月报
                Long year = Long.valueOf(date.substring(0, 4));
                Long month = Long.valueOf(date.substring(5, date.length()));
                sumResponse = this.companyMonth(year, month, sumResponse);
            }
            return HttpResponse.success(sumResponse);
        }else {
            if(StringUtils.isEmpty(productSortCode) ){
                return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
            }
            // 计算部门的负毛利统计
            if(reportType.equals(Global.ANNUAL_REPORT)){
                // 计算部门的负毛利统计- 年报

            }else if(reportType.equals(Global.QUARTERLY_REPORT)){
                // 计算部门的负毛利统计- 季报
                if(seasonType == null){
                    return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
                }

            }else if(reportType.equals(Global.MONTHLY_REPORT)){
                // 计算部门的负毛利统计- 月报

            }
            return HttpResponse.success();
        }
    }

    private NegativeSumResponse companyYear(Long year, NegativeSumResponse sumResponse){
        List<NegativeDeptResponse> deptList = new ArrayList<>();
        List<CompanyAndDeptResponse> companys;
        List<CompanyAndDeptResponse> departments;
        List<NegativeCompanyResponse> companyList;
        NegativeDeptResponse deptResponse;
        NegativeCompanyResponse companyResponse;
        List<NegativeCategoryResponse> categoryList;
        NegativeRateResponse rateResponse;
        sumResponse = statComNegativeMarginYearlyDao.negativeSum(year);
        if(sumResponse != null){
            departments = statComNegativeMarginYearlyDao.negativeByDept(year);
            if(CollectionUtils.isNotEmptyCollection(departments)){
                for(CompanyAndDeptResponse dept:departments){
                    deptResponse = statComNegativeMarginYearlyDao.negativeDeptSum(year, dept.getProductSortCode());
                    companys = statComNegativeMarginYearlyDao.negativeByCompany(year, dept.getProductSortCode());
                    if(CollectionUtils.isNotEmptyCollection(companys)){
                        companyList = new ArrayList<>();
                        for (CompanyAndDeptResponse company:companys){
                            companyResponse = statComNegativeMarginYearlyDao.negativeCompanySum(year, company.getPriceChannelCode(),
                                    company.getProductSortCode());
                            if(companyResponse != null){
                                categoryList = statComNegativeMarginYearlyDao.negativeCategoryList(year, company.getPriceChannelCode(),
                                        company.getProductSortCode());
                                companyResponse.setCategoryList(categoryList);
                                rateResponse = new NegativeRateResponse();
                                BeanUtils.copyProperties(companyResponse, rateResponse);
                                NegativeRateResponse rate = this.negativeRate(rateResponse);
                                BeanUtils.copyProperties(rate, companyResponse);
                                companyList.add(companyResponse);
                            }
                        }
                        rateResponse = new NegativeRateResponse();
                        BeanUtils.copyProperties(deptResponse, rateResponse);
                        NegativeRateResponse rate = this.negativeRate(rateResponse);
                        BeanUtils.copyProperties(rate, deptResponse);
                        deptResponse.setCompanyList(companyList);
                        deptList.add(deptResponse);
                    }
                }
                rateResponse = new NegativeRateResponse();
                BeanUtils.copyProperties(sumResponse, rateResponse);
                NegativeRateResponse rate = this.negativeRate(rateResponse);
                BeanUtils.copyProperties(rate, sumResponse);
                sumResponse.setDeptList(deptList);
            }
        }
        return sumResponse;
    }

    private NegativeSumResponse companyMonth(Long year, Long month, NegativeSumResponse sumResponse){
        List<NegativeDeptResponse> deptList = new ArrayList<>();
        List<CompanyAndDeptResponse> companys;
        List<CompanyAndDeptResponse> departments;
        List<NegativeCompanyResponse> companyList;
        NegativeDeptResponse deptResponse;
        NegativeCompanyResponse companyResponse;
        List<NegativeCategoryResponse> categoryList;
        NegativeRateResponse rateResponse;
        sumResponse = statComNegativeMarginMonthlyDao.negativeSum(year, month);
        if(sumResponse != null){
            departments = statComNegativeMarginMonthlyDao.negativeByDept(year, month);
            if(CollectionUtils.isNotEmptyCollection(departments)){
                for(CompanyAndDeptResponse dept:departments){
                    deptResponse = statComNegativeMarginMonthlyDao.negativeDeptSum(year, month, dept.getProductSortCode());
                    companys = statComNegativeMarginMonthlyDao.negativeByCompany(year, month, dept.getProductSortCode());
                    if(CollectionUtils.isNotEmptyCollection(companys)){
                        companyList = new ArrayList<>();
                        for (CompanyAndDeptResponse company:companys){
                            companyResponse = statComNegativeMarginMonthlyDao.negativeCompanySum(year, month, company.getPriceChannelCode(),
                                    company.getProductSortCode());
                            if(companyResponse != null){
                                categoryList = statComNegativeMarginMonthlyDao.negativeCategoryList(year, month, company.getPriceChannelCode(),
                                        company.getProductSortCode());
                                companyResponse.setCategoryList(categoryList);
                                rateResponse = new NegativeRateResponse();
                                BeanUtils.copyProperties(companyResponse, rateResponse);
                                NegativeRateResponse rate = this.negativeRate(rateResponse);
                                BeanUtils.copyProperties(rate, companyResponse);
                                companyList.add(companyResponse);
                            }
                        }
                        rateResponse = new NegativeRateResponse();
                        BeanUtils.copyProperties(deptResponse, rateResponse);
                        NegativeRateResponse rate = this.negativeRate(rateResponse);
                        BeanUtils.copyProperties(rate, deptResponse);
                        deptResponse.setCompanyList(companyList);
                        deptList.add(deptResponse);
                    }
                }
                rateResponse = new NegativeRateResponse();
                BeanUtils.copyProperties(sumResponse, rateResponse);
                NegativeRateResponse rate = this.negativeRate(rateResponse);
                BeanUtils.copyProperties(rate, sumResponse);
                sumResponse.setDeptList(deptList);
            }
        }
        return sumResponse;
    }

    private NegativeRateResponse negativeRate(NegativeRateResponse rateResponse){
        // 渠道销售金额同比
        if(rateResponse.getChannelSalesAmount() == null || rateResponse.getChannelSalesAmount() == 0 ||
                rateResponse.getPreChannelSalesAmount() == null){
            rateResponse.setChannelSalesAmountYearonyear(new BigDecimal(0));
        }else {
            rateResponse.setChannelSalesAmountYearonyear(new BigDecimal(rateResponse.getChannelSalesAmount()).
                    divide(new BigDecimal(rateResponse.getPreChannelSalesAmount())));
        }
        // 渠道毛利同比
        if(rateResponse.getChannelMargin() == null || rateResponse.getChannelMargin() == 0 ||
                rateResponse.getPreChannelMargin() == null){
            rateResponse.setChannelMarginYearonyear(new BigDecimal(0));
        }else {
            rateResponse.setChannelMarginYearonyear(new BigDecimal(rateResponse.getChannelMargin()).
                    divide(new BigDecimal(rateResponse.getPreChannelMargin())));
        }

        // 分销销售额同比
        if(rateResponse.getDistributionSalesAmount() == null || rateResponse.getDistributionSalesAmount() == 0 ||
                rateResponse.getPreDistributionSalesAmount() == null){
            rateResponse.setDistributionSalesAmountYearonyear(new BigDecimal(0));
        }else {
            rateResponse.setDistributionSalesAmountYearonyear(new BigDecimal(rateResponse.getDistributionSalesAmount()).
                    divide(new BigDecimal(rateResponse.getPreDistributionSalesAmount())));
        }

        // 分销毛利同比
        if(rateResponse.getDistributionMargin() == null || rateResponse.getDistributionMargin() == 0 ||
                rateResponse.getPreDistributionMargin() == null){
            rateResponse.setDistributionMarginYearonyear(new BigDecimal(0));
        }else {
            rateResponse.setDistributionMarginYearonyear(new BigDecimal(rateResponse.getDistributionMargin()).
                    divide(new BigDecimal(rateResponse.getPreDistributionMargin())));
        }
        return rateResponse;
    }

}

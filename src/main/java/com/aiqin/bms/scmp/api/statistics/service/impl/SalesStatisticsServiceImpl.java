package com.aiqin.bms.scmp.api.statistics.service.impl;

import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.statistics.dao.*;
import com.aiqin.bms.scmp.api.statistics.domain.request.SaleRequest;
import com.aiqin.bms.scmp.api.statistics.domain.response.CompanyAndDeptResponse;
import com.aiqin.bms.scmp.api.statistics.domain.response.sale.*;
import com.aiqin.bms.scmp.api.statistics.service.SalesStatisticsService;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: zhao shuai
 * @create: 2019-09-03
 **/
@Service
public class SalesStatisticsServiceImpl implements SalesStatisticsService {

    @Resource
    private StatComSalesYearlyDao statComSalesYearlyDao;
    @Resource
    private StatComSalesMonthlyDao statComSalesMonthlyDao;
    @Resource
    private StatDeptSalesYearlyDao statDeptSalesYearlyDao;
    @Resource
    private StatDeptSalesMonthlyDao statDeptSalesMonthlyDao;
    @Resource
    private StatComMonthAccSalesDao statComMonthAccSalesDao;
    @Resource
    private StatDeptMonthAccSalesDao statDeptMonthAccSalesDao;

    private final static int YEAR = 0;
    private final static int MONTH = 2;

    @Override
    public HttpResponse<SaleSumResponse> saleInfo(SaleRequest saleRequest){
        if(saleRequest == null || StringUtils.isBlank(saleRequest.getDate()) || saleRequest.getType() == null ||
                saleRequest.getReportType() == null){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        if(saleRequest.getType().equals(Global.COMPANY)){
            SaleSumResponse sumResponse = new SaleSumResponse();
            // 销售统计 - 公司
            if(saleRequest.getReportType().equals(Global.ANNUAL_REPORT)){
                // 公司 - 年报
                Long year = Long.valueOf(saleRequest.getDate());
                saleRequest.setYear(year);
                sumResponse = this.companySale(saleRequest, YEAR);
            }else if(saleRequest.getReportType().equals(Global.MONTHLY_REPORT)){
                // 公司 - 月报
                Long year = Long.valueOf(saleRequest.getDate().substring(0, 4));
                Long month = Long.valueOf(saleRequest.getDate().substring(5));
                saleRequest.setYear(year);
                saleRequest.setMonth(month);
                sumResponse = this.companySale(saleRequest, MONTH);
            }
            return HttpResponse.success(sumResponse);
        }else if(saleRequest.getType().equals(Global.DEPARTMENT)){
            if(StringUtils.isBlank(saleRequest.getProductSortCode())){
                return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
            }
            SaleDeptResponse deptResponse = new SaleDeptResponse();
            // 销售统计 - 部门
            if(saleRequest.getReportType().equals(Global.ANNUAL_REPORT)){
                // 部门 - 年报
                Long year = Long.valueOf(saleRequest.getDate());
                saleRequest.setYear(year);
                deptResponse = this.deptSale(saleRequest, YEAR);
            }else if(saleRequest.getReportType().equals(Global.MONTHLY_REPORT)){
                // 部门 - 月报
                Long year = Long.valueOf(saleRequest.getDate().substring(0, 4));
                Long month = Long.valueOf(saleRequest.getDate().substring(5));
                saleRequest.setYear(year);
                saleRequest.setMonth(month);
                deptResponse = this.deptSale(saleRequest, MONTH);
            }
            return HttpResponse.success(deptResponse);
        }
        return HttpResponse.success();
    }

    private SaleSumResponse companySale(SaleRequest saleRequest, int i){
        SaleSumResponse sumResponse;
        List<SaleDeptResponse> deptList = Lists.newArrayList();
        List<CompanyAndDeptResponse> departments;
        List<CompanyAndDeptResponse> companys;
        List<SaleCompanyResponse> companyList;
        List<SaleStoreResponse> saleList;
        SaleDeptResponse deptResponse;
        SaleCompanyResponse companyResponse;
        SaleResponse saleResponse;
        if(i == YEAR){
            sumResponse = statComSalesYearlyDao.saleSum(saleRequest);
        }else {
            sumResponse = statComSalesMonthlyDao.saleSum(saleRequest);
        }
        if(sumResponse != null){
            if(i == YEAR){
                departments = statComSalesYearlyDao.saleByDept(saleRequest);
            }else {
                departments = statComSalesMonthlyDao.saleByDept(saleRequest);
            }
            if(CollectionUtils.isNotEmptyCollection(departments)){
                for(CompanyAndDeptResponse dept:departments){
                    saleRequest.setProductSortCode(dept.getProductSortCode());
                    if(i == YEAR){
                        deptResponse = statComSalesYearlyDao.saleSumDept(saleRequest);
                        companys = statComSalesYearlyDao.saleByCompany(saleRequest);
                    }else {
                        deptResponse = statComSalesMonthlyDao.saleSumDept(saleRequest);
                        companys = statComSalesMonthlyDao.saleByCompany(saleRequest);
                    }
                    if(CollectionUtils.isNotEmptyCollection(companys)){
                       companyList = Lists.newArrayList();
                       for (CompanyAndDeptResponse company:companys){
                           saleRequest.setPriceChannelCode(company.getPriceChannelCode());
                           if(i == YEAR){
                               companyResponse = statComSalesYearlyDao.saleSumCompany(saleRequest);
                           }else {
                               companyResponse = statComSalesMonthlyDao.saleSumCompany(saleRequest);
                           }
                           if(companyResponse != null){
                               if(i == 0){
                                   saleList = statComSalesYearlyDao.saleStoreList(saleRequest);
                               }else {
                                   saleList = statComSalesMonthlyDao.saleStoreList(saleRequest);
                               }
                               saleResponse = new SaleResponse();
                               this.saleList(saleList, saleResponse, companyResponse);
                               companyResponse.setStoreList(saleList);
                               saleResponse = new SaleResponse();
                               BeanUtils.copyProperties(companyResponse, saleResponse);
                               SaleResponse rate = this.saleRate(saleResponse, 0);
                               BeanUtils.copyProperties(rate, companyResponse);
                               companyResponse.setChanneRate(new BigDecimal(companyResponse.getChannelSalesAmount()).
                                       divide(new BigDecimal(deptResponse.getChannelSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                               companyResponse.setDistributionRate(new BigDecimal(companyResponse.getDistributionSalesAmount()).
                                       divide(new BigDecimal(deptResponse.getDistributionSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                               companyList.add(companyResponse);
                           }
                       }
                       deptResponse.setCompanyList(companyList);
                       saleResponse = new SaleResponse();
                       BeanUtils.copyProperties(deptResponse, saleResponse);
                       SaleResponse rate = this.saleRate(saleResponse, 1);
                       BeanUtils.copyProperties(rate, deptResponse);
                       deptResponse.setChanneRate(new BigDecimal(deptResponse.getChannelSalesAmount()).
                               divide(new BigDecimal(sumResponse.getChannelSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                       deptResponse.setDistributionRate(new BigDecimal(deptResponse.getDistributionSalesAmount()).
                               divide(new BigDecimal(sumResponse.getDistributionSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                       deptList.add(deptResponse);
                    }
                }
                saleResponse = new SaleResponse();
                BeanUtils.copyProperties(sumResponse, saleResponse);
                SaleResponse rate = this.saleRate(saleResponse, 1);
                BeanUtils.copyProperties(rate, sumResponse);
                sumResponse.setDeptList(deptList);
                sumResponse.setChanneRate(new BigDecimal(1));
                sumResponse.setDistributionRate(new BigDecimal(1));
            }
        }
        return sumResponse;
    }

    private SaleDeptResponse deptSale(SaleRequest saleRequest, int i) {
        List<CompanyAndDeptResponse> companys;
        List<SaleCompanyResponse> companyList;
        List<SaleStoreResponse> saleList;
        SaleDeptResponse deptResponse;
        SaleCompanyResponse companyResponse;
        SaleResponse saleResponse;
        if(i == YEAR){
            deptResponse = statDeptSalesYearlyDao.saleSumDept(saleRequest);
            companys = statDeptSalesYearlyDao.saleByCompany(saleRequest);
        }else if(i == MONTH){
            deptResponse = statDeptSalesMonthlyDao.saleSumDept(saleRequest);
            companys = statDeptSalesMonthlyDao.saleByCompany(saleRequest);
        }else if(i == 3){
            deptResponse = statComMonthAccSalesDao.saleSumDept(saleRequest);
            companys = statComMonthAccSalesDao.saleByCompany(saleRequest);
        }else {
            deptResponse = null;
            companys = null;
        }
        if (CollectionUtils.isNotEmptyCollection(companys)) {
            companyList = Lists.newArrayList();
            for (CompanyAndDeptResponse company : companys) {
                saleRequest.setPriceChannelCode(company.getPriceChannelCode());
                if(i == YEAR){
                    companyResponse = statDeptSalesYearlyDao.saleSumCompany(saleRequest);
                }else if(i == MONTH){
                    companyResponse = statDeptSalesMonthlyDao.saleSumCompany(saleRequest);
                }else if(i == 3){
                    companyResponse = statComMonthAccSalesDao.saleSumCompany(saleRequest);
                }else {
                    companyResponse = null;
                }
                if (companyResponse != null) {
                    if(i == YEAR){
                        saleList = statDeptSalesYearlyDao.saleStoreList(saleRequest);
                    }else if(i == MONTH){
                        saleList = statDeptSalesMonthlyDao.saleStoreList(saleRequest);
                    }else if(i == 3){
                        saleList = statComMonthAccSalesDao.saleStoreList(saleRequest);
                    }else {
                        saleList = null;
                    }
                    saleResponse = new SaleResponse();
                    this.saleList(saleList, saleResponse, companyResponse);
                    companyResponse.setStoreList(saleList);
                    saleResponse = new SaleResponse();
                    BeanUtils.copyProperties(companyResponse, saleResponse);
                    SaleResponse rate = this.saleRate(saleResponse, 0);
                    BeanUtils.copyProperties(rate, companyResponse);
                    companyResponse.setChanneRate(new BigDecimal(companyResponse.getChannelSalesAmount()).
                            divide(new BigDecimal(deptResponse.getChannelSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                    companyResponse.setDistributionRate(new BigDecimal(companyResponse.getDistributionSalesAmount()).
                            divide(new BigDecimal(deptResponse.getDistributionSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                    companyList.add(companyResponse);
                }
            }
            deptResponse.setCompanyList(companyList);
            saleResponse = new SaleResponse();
            BeanUtils.copyProperties(deptResponse, saleResponse);
            SaleResponse rate = this.saleRate(saleResponse, 1);
            BeanUtils.copyProperties(rate, deptResponse);
            deptResponse.setChanneRate(new BigDecimal(1));
            deptResponse.setDistributionRate(new BigDecimal(1));
        }
        return deptResponse;
    }

    private void saleList(List<SaleStoreResponse> saleList,SaleResponse saleResponse, SaleCompanyResponse companyResponse){
        if (CollectionUtils.isNotEmptyCollection(saleList)) {
            for (SaleStoreResponse sale : saleList) {
                BeanUtils.copyProperties(sale, saleResponse);
                SaleResponse rate = this.saleRate(saleResponse, 0);
                BeanUtils.copyProperties(rate, sale);
                sale.setChanneRate(new BigDecimal(sale.getChannelSalesAmount()).
                        divide(new BigDecimal(companyResponse.getChannelSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                sale.setDistributionRate(new BigDecimal(sale.getDistributionSalesAmount()).
                        divide(new BigDecimal(companyResponse.getDistributionSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
            }
        }
    }

    private SaleResponse saleRate(SaleResponse sale, int i){
        if(sale != null){
            BigDecimal big = new BigDecimal(0);
            // 计算渠道销售金额的同比
            Long amount = 0L;
            Long channelSalesAmount = sale.getChannelSalesAmount() == null ? amount : sale.getChannelSalesAmount();
            Long channelAmountLastYear = sale.getChannelAmountLastYear() == null ? amount : sale.getChannelAmountLastYear();
            if(channelSalesAmount == amount || channelAmountLastYear == amount){
                sale.setChannelSalesAmountYearonyear(big);
            }else {
                sale.setChannelSalesAmountYearonyear(new BigDecimal(channelSalesAmount).
                        divide(new BigDecimal(channelAmountLastYear), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算渠道销售金额的环比
            Long channelAmountLastMonth = sale.getChannelAmountLastMonth() == null ? amount : sale.getChannelAmountLastMonth();
            if(channelSalesAmount == amount || channelAmountLastMonth == amount){
                sale.setChannelSalesAmountLinkRela(big);
            }else {
                sale.setChannelSalesAmountLinkRela(new BigDecimal(channelSalesAmount).
                        divide(new BigDecimal(channelAmountLastMonth), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算渠道毛利率
            Long channelSalesCost = sale.getChannelSalesCost() == null ? amount : sale.getChannelSalesCost();
            if(channelSalesCost == amount || channelSalesAmount == amount){
                sale.setChannelMarginRate(big);
            }else {
                sale.setChannelMarginRate(new BigDecimal(channelSalesAmount - channelSalesCost).
                        divide(new BigDecimal(channelSalesAmount), 4, BigDecimal.ROUND_HALF_UP));
            }

            // 计算渠道毛利同比
            Long channelMargin = sale.getChannelMargin() == null ? amount : sale.getChannelMargin();
            Long channelMarginLastYear = sale.getChannelMarginLastYear() == null ? amount : sale.getChannelMarginLastYear();
            if(channelMargin == amount || channelMarginLastYear == amount){
                sale.setChannelMarginYearonyear(big);
            }else {
                sale.setChannelMarginYearonyear(new BigDecimal(channelMargin).
                        divide(new BigDecimal(channelMarginLastYear), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算渠道毛利环比
            Long channelMarginLastMonth  = sale.getChannelMarginLastMonth() == null ? amount : sale.getChannelMarginLastMonth();
            if(channelMargin == amount || channelMarginLastMonth == amount){
                sale.setChannelMarginLinkRela(big);
            }else {
                sale.setChannelMarginLinkRela(new BigDecimal(channelMargin).
                        divide(new BigDecimal(channelMarginLastMonth), 4, BigDecimal.ROUND_HALF_UP));
            }

            // 计算分销销售额同比
            Long distributionSalesAmount = sale.getDistributionSalesAmount() == null ? amount : sale.getDistributionSalesAmount();
            Long distributionAmountLastYear = sale.getDistributionAmountLastYear() == null ? amount : sale.getDistributionAmountLastYear();
            if(distributionSalesAmount == amount || distributionAmountLastYear == amount){
                sale.setDistributionSalesAmountYearonyear(big);
            }else {
                sale.setDistributionSalesAmountYearonyear(new BigDecimal(distributionSalesAmount).
                        divide(new BigDecimal(distributionAmountLastYear), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算分销销售额环比
            Long distributionAmountLastMonth = sale.getDistributionAmountLastMonth() == null ? amount : sale.getDistributionAmountLastMonth();
            if(distributionSalesAmount == amount || distributionAmountLastMonth == amount){
                sale.setDistributionSalesAmountLinkRela(big);
            }else {
                sale.setDistributionSalesAmountLinkRela(new BigDecimal(distributionSalesAmount).
                        divide(new BigDecimal(distributionAmountLastMonth), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算分销毛利额
            Long distributionSalesCost = sale.getDistributionSalesCost() == null ? amount : sale.getDistributionSalesCost();
            if(distributionSalesCost == amount || distributionSalesAmount == amount){
                sale.setDistributionMarginRate(big);
            }else {
                sale.setDistributionMarginRate(new BigDecimal(distributionSalesAmount - distributionSalesCost).
                        divide(new BigDecimal(distributionSalesAmount), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算分销毛利额同比
            Long distributionMargin = sale.getDistributionMargin() == null ? amount : sale.getDistributionMargin();
            Long distributionMarginLastYear = sale.getDistributionMarginLastYear() == null ? amount : sale.getDistributionMarginLastYear();
            if(distributionMargin == amount || distributionMarginLastYear == amount){
                sale.setDistributionMarginYearonyear(big);
            }else {
                sale.setDistributionMarginYearonyear(new BigDecimal(distributionMargin).
                        divide(new BigDecimal(distributionMarginLastYear), 4, BigDecimal.ROUND_HALF_UP));
            }
            // 计算分销毛利额同比
            Long distributionMarginLastMonth = sale.getDistributionMarginLastMonth() == null ? amount : sale.getDistributionMarginLastMonth();
            if(distributionMargin == amount || distributionMarginLastMonth == amount){
                sale.setDistributionMarginLinkRela(big);
            }else {
                sale.setDistributionMarginLinkRela(new BigDecimal(distributionMargin).
                        divide(new BigDecimal(distributionMarginLastMonth), 4, BigDecimal.ROUND_HALF_UP));
            }
            if(i == 1){
                // 计算渠道的达成率
                Long channelBudget = sale.getChannelBudget() == null ? amount : sale.getChannelBudget();
                Long distributionBudget = sale.getDistributionBudget() == null ? amount : sale.getDistributionBudget();
                if(channelSalesAmount == amount || channelBudget == amount){
                    sale.setChannelAchievement(big);
                }else {
                    sale.setChannelAchievement(new BigDecimal(channelSalesAmount).
                            divide(new BigDecimal(channelBudget), 4, BigDecimal.ROUND_HALF_UP));
                }
                // 计算分销的达成率
                if(distributionSalesAmount == amount || distributionBudget == amount){
                    sale.setDistributionAchievement(big);
                }else {
                    sale.setDistributionAchievement(new BigDecimal(distributionSalesAmount).
                            divide(new BigDecimal(distributionBudget), 4, BigDecimal.ROUND_HALF_UP));
                }
            }
        }
        return sale;
    }

    @Override
    public HttpResponse<SaleSumResponse> monthSaleInfo(SaleRequest saleRequest){
        if(saleRequest == null || StringUtils.isBlank(saleRequest.getDate()) ||  saleRequest.getType() == null){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        Long year = Long.valueOf(saleRequest.getDate().substring(0, 4));
        Long month = Long.valueOf(saleRequest.getDate().substring(5));
        saleRequest.setYear(year);
        saleRequest.setMonth(month);
        if(saleRequest.getType().equals(Global.COMPANY)){
            // 计算月累计销售统计 - 公司
            SaleSumResponse sumResponse = this.monthCompanySale(saleRequest);
            return HttpResponse.success(sumResponse);
        }else {
            // 计算月累计销售统计 - 部门
            if(StringUtils.isBlank(saleRequest.getProductSortCode())){
                return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
            }
            SaleDeptResponse deptResponse = this.deptSale(saleRequest, 3);
            return HttpResponse.success(deptResponse);
        }
    }

    private SaleSumResponse monthCompanySale(SaleRequest saleRequest){
        SaleSumResponse sumResponse;
        List<SaleDeptResponse> deptList = Lists.newArrayList();
        List<CompanyAndDeptResponse> departments;
        List<CompanyAndDeptResponse> companys;
        List<SaleCompanyResponse> companyList;
        List<SaleStoreResponse> saleList;
        SaleDeptResponse deptResponse;
        SaleCompanyResponse companyResponse;
        SaleResponse saleResponse;
        sumResponse = statComMonthAccSalesDao.saleSum(saleRequest);
        if(sumResponse != null){
            departments = statComMonthAccSalesDao.saleByDept(saleRequest);
            if(CollectionUtils.isNotEmptyCollection(departments)){
                for(CompanyAndDeptResponse dept:departments){
                    saleRequest.setProductSortCode(dept.getProductSortCode());
                    deptResponse = statComMonthAccSalesDao.saleSumDept(saleRequest);
                    companys = statComMonthAccSalesDao.saleByCompany(saleRequest);
                    if(CollectionUtils.isNotEmptyCollection(companys)){
                        companyList = Lists.newArrayList();
                        for (CompanyAndDeptResponse company:companys){
                            saleRequest.setPriceChannelCode(company.getPriceChannelCode());
                            companyResponse = statComMonthAccSalesDao.saleSumCompany(saleRequest);
                            if(companyResponse != null){
                                saleList = statComMonthAccSalesDao.saleStoreList(saleRequest);
                                saleResponse = new SaleResponse();
                                this.saleList(saleList, saleResponse, companyResponse);
                                companyResponse.setStoreList(saleList);
                                saleResponse = new SaleResponse();
                                BeanUtils.copyProperties(companyResponse, saleResponse);
                                SaleResponse rate = this.saleRate(saleResponse, 0);
                                BeanUtils.copyProperties(rate, companyResponse);
                                companyResponse.setChanneRate(new BigDecimal(companyResponse.getChannelSalesAmount()).
                                        divide(new BigDecimal(deptResponse.getChannelSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                                companyResponse.setDistributionRate(new BigDecimal(companyResponse.getDistributionSalesAmount()).
                                        divide(new BigDecimal(deptResponse.getDistributionSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                                companyList.add(companyResponse);
                            }
                        }
                        deptResponse.setCompanyList(companyList);
                        saleResponse = new SaleResponse();
                        BeanUtils.copyProperties(deptResponse, saleResponse);
                        SaleResponse rate = this.saleRate(saleResponse, 1);
                        BeanUtils.copyProperties(rate, deptResponse);
                        deptResponse.setChanneRate(new BigDecimal(deptResponse.getChannelSalesAmount()).
                                divide(new BigDecimal(sumResponse.getChannelSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                        deptResponse.setDistributionRate(new BigDecimal(deptResponse.getDistributionSalesAmount()).
                                divide(new BigDecimal(sumResponse.getDistributionSalesAmount()), 4, BigDecimal.ROUND_HALF_UP));
                        deptList.add(deptResponse);
                    }
                }
                saleResponse = new SaleResponse();
                BeanUtils.copyProperties(sumResponse, saleResponse);
                SaleResponse rate = this.saleRate(saleResponse, 1);
                BeanUtils.copyProperties(rate, sumResponse);
                sumResponse.setDeptList(deptList);
                sumResponse.setChanneRate(new BigDecimal(1));
                sumResponse.setDistributionRate(new BigDecimal(1));
            }
        }
        return sumResponse;
    }
}

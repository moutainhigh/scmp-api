package com.aiqin.bms.scmp.api.statistics.dao;

import com.aiqin.bms.scmp.api.statistics.domain.response.CompanyAndDeptResponse;
import com.aiqin.bms.scmp.api.statistics.domain.response.negative.NegativeCategoryResponse;
import com.aiqin.bms.scmp.api.statistics.domain.response.negative.NegativeCompanyResponse;
import com.aiqin.bms.scmp.api.statistics.domain.response.negative.NegativeDeptResponse;
import com.aiqin.bms.scmp.api.statistics.domain.response.negative.NegativeSumResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StatDeptNegativeMarginMonthlyDao {

    NegativeSumResponse negativeSum(@Param("year")Long year, @Param("month")Long month);

    List<CompanyAndDeptResponse> negativeByDept(@Param("year") Long year, @Param("month")Long month);

    List<CompanyAndDeptResponse> negativeByCompany(@Param("year") Long year,
                                                   @Param("month")Long month,
                                                   @Param("productSortCode") String productSortCode);

    NegativeCompanyResponse negativeCompanySum(@Param("year") Long year, @Param("month")Long month,
                                               @Param("priceChannelCode") String priceChannelCode,
                                               @Param("productSortCode") String productSortCode);

    NegativeDeptResponse negativeDeptSum(@Param("year") Long year, @Param("month")Long month,
                                         @Param("productSortCode") String productSortCode);

    List<NegativeCategoryResponse> negativeCategoryList(@Param("year") Long year, @Param("month")Long month,
                                                        @Param("priceChannelCode") String priceChannelCode,
                                                        @Param("productSortCode") String productSortCode);
}
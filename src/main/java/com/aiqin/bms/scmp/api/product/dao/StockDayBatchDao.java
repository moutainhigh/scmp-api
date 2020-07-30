package com.aiqin.bms.scmp.api.product.dao;

import com.aiqin.bms.scmp.api.product.domain.pojo.StockDayBatch;
import com.aiqin.bms.scmp.api.product.domain.pojo.StockMonthBatch;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockDayBatchDao {

    Integer delete(StockMonthBatch deleteBatch);

    Integer insertAll(@Param("list") List<StockMonthBatch> record);

    StockDayBatch stockDayBatchOne(StockDayBatch record);

    Integer update(StockDayBatch record);

    List<StockDayBatch> stockDayBatchList(StockMonthBatch record);

    List<StockMonthBatch> dayBatchByGroup(@Param("synchrTime") Long synchrTime,
                                          @Param("wmsType") Integer wmsType);

    List<StockDayBatch> stockDayByDl(Integer wmsType);

}
package com.aiqin.bms.scmp.api.purchase.dao;

import com.aiqin.bms.scmp.api.abutment.domain.request.purchase.ScmpPurchaseBatch;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseBatch;
import com.aiqin.bms.scmp.api.purchase.domain.request.PurchaseOrderProductRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PurchaseBatchDao {

    Integer insertAll(@Param("list")List<PurchaseBatch> record);

    Integer update(PurchaseBatch record);

    PurchaseBatch purchaseInfo(@Param("batchInfoCode") String batchInfoCode,
                               @Param("purchaseOderCode") String purchaseOderCode,
                               @Param("lineCode") Integer lineCode);

    List<ScmpPurchaseBatch> purchaseBatchListBySap(@Param("skuCode") String skuCode,
                               @Param("purchaseOderCode") String purchaseOderCode,
                               @Param("lineCode") Integer lineCode);

    List<PurchaseBatch> list(PurchaseOrderProductRequest request);

    Integer listCount(PurchaseOrderProductRequest request);

    List<PurchaseBatch> purchaseBatchList(@Param("purchaseOderCode") String purchaseOderCode,
                                              @Param("skuCode") String skuCode,
                                              @Param("lineCode") Integer lineCode);

}
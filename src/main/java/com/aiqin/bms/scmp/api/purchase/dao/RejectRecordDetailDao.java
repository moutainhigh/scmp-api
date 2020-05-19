package com.aiqin.bms.scmp.api.purchase.dao;

import com.aiqin.bms.scmp.api.purchase.domain.PurchaseOrderProduct;
import com.aiqin.bms.scmp.api.purchase.domain.RejectRecordDetail;
import com.aiqin.bms.scmp.api.purchase.domain.request.RejectDetailStockRequest;
import com.aiqin.bms.scmp.api.purchase.domain.response.RejectRecordDetailResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RejectRecordDetailDao {

    List<RejectRecordDetail> list(String rejectRecordCode);

    Integer listCount(String rejectRecordCode);

    Integer insert(RejectRecordDetail record);

    RejectRecordDetail selectByPrimaryKey(Long id);

    Integer update(RejectRecordDetail record);

    Integer insertAll(@Param("list") List<RejectRecordDetail> detailList, @Param("rejectRecordId") String rejectId, @Param("rejectRecordCode") String rejectCode, @Param("createById") String createId, @Param("createByName") String createName);

    List<RejectRecordDetail> selectByRejectId(@Param("rejectRecordId") String rejectRecordId);

    List<RejectRecordDetailResponse> selectProductByRejectId(String rejectRecordId);

    void updateByDetailId(RejectDetailStockRequest detailResponse);

    List<RejectRecordDetail> selectByRejectDetailIdList(List<Long> list);

    List<RejectRecordDetail> listByRejectIds(List<String> list);
}
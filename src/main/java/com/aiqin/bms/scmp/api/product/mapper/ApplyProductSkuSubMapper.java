package com.aiqin.bms.scmp.api.product.mapper;

import com.aiqin.bms.scmp.api.product.domain.pojo.ApplyProductSkuSub;

import java.util.List;

public interface ApplyProductSkuSubMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ApplyProductSkuSub record);

    int insertSelective(ApplyProductSkuSub record);

    ApplyProductSkuSub selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ApplyProductSkuSub record);

    int updateByPrimaryKey(ApplyProductSkuSub record);

    int insertBatch(List<ApplyProductSkuSub> applyList);
}
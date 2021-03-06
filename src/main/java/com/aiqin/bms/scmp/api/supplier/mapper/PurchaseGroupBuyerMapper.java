package com.aiqin.bms.scmp.api.supplier.mapper;


import com.aiqin.bms.scmp.api.supplier.domain.pojo.PurchaseGroupBuyer;

import java.util.List;

public interface PurchaseGroupBuyerMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PurchaseGroupBuyer record);

    int insertSelective(PurchaseGroupBuyer record);

    PurchaseGroupBuyer selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PurchaseGroupBuyer record);

    int updateByPrimaryKey(PurchaseGroupBuyer record);

    /**
     * 批量插入
     * @param saveVos
     * @return
     */
    int insertBatch(List<PurchaseGroupBuyer> saveVos);

    int deleteBatch(List<PurchaseGroupBuyer> saveVos);

    /**
     *
     * @param saveVos
     * @return
     */
    int updateStatus(List<PurchaseGroupBuyer> saveVos);

    /**
     * 获取当前用户的采购组编码
     * @param personId
     * @return
     */
    List<String> loadCodesByPersonId(String personId);


    /**
     * 获取当前用户的采购组
     * @param personId
     * @return
     */
    List<String> loadNamesByPersonId(String personId);
}
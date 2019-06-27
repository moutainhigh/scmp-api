package com.aiqin.bms.scmp.api.supplier.mapper;

import com.aiqin.bms.scmp.api.supplier.domain.pojo.ApplyUseTagRecord;

import java.util.List;

public interface ApplyUseTagRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ApplyUseTagRecord record);

    int insertSelective(ApplyUseTagRecord record);

    ApplyUseTagRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ApplyUseTagRecord record);

    int updateByPrimaryKey(ApplyUseTagRecord record);

    /**
     * 批量新增
     * @param applyUseTagRecords
     * @return
     */
    int saveBatch(List<ApplyUseTagRecord> applyUseTagRecords);

    /**
     * 删除
     * @param appUseObjectCode
     * @return
     */
    int delete(String appUseObjectCode);

    /**
     * 批量删除
     * @param appUseObjectCodes
     * @return
     */
    int deletes(List<String> appUseObjectCodes);

    /**
     * 批量修改
     * @param applyUseTagRecords
     * @return
     */
    int updateBatch(List<ApplyUseTagRecord> applyUseTagRecords);

    /**
     * 根据申请使用者编号查询
     * @param appUseObjectCode
     * @return
     */
    List<ApplyUseTagRecord> getApplyUseTagRecordByAppUseObjectCode(String appUseObjectCode);

    /**
     * 根据申请使用者编号List查询
     * @param appUseObjectCode
     * @return
     */
    List<ApplyUseTagRecord> getApplyUseTagRecordByAppUseObjectCodes(List<String> appUseObjectCode);
}
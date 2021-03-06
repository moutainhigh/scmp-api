package com.aiqin.bms.scmp.api.product.dao;

import com.aiqin.bms.scmp.api.product.domain.pojo.ApplyProductSku;
import com.aiqin.bms.scmp.api.product.domain.pojo.ApplyProductSkuManufacturer;
import com.aiqin.bms.scmp.api.product.domain.pojo.ProductSkuManufacturer;
import com.aiqin.bms.scmp.api.product.domain.pojo.ProductSkuManufacturerDraft;
import com.aiqin.bms.scmp.api.product.domain.request.sku.ProductSkuManufacturerReqVO;
import com.aiqin.bms.scmp.api.product.domain.response.sku.ProductSkuManufacturerRespVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @功能说明:
 * @author: wangxu
 * @date: 2019/1/29 0029 10:18
 */
public interface ProductSkuManufacturerDao {
    /**
     * 批量新增草稿
     * @param productSkuManufacturerDrafts
     * @return
     */
    int insertDraftList(List<ProductSkuManufacturerDraft> productSkuManufacturerDrafts);

    int insertList(List<ProductSkuManufacturer> productSkuManufacturers);

    int insertApplyList(List<ApplyProductSkuManufacturer> applyProductSkuManufacturers);

    List<ProductSkuManufacturerRespVo> getDraft(String skuCode);

    List<ProductSkuManufacturerRespVo> getRespVo(String skuCode);

    List<ProductSkuManufacturer> getInfo(String skuCode);

    List<ProductSkuManufacturerDraft> getDrafts(@Param("productSkus") List<ApplyProductSku> productSkus);

    int deleteDrafts(@Param("productSkus") List<ApplyProductSku> productSkus);

    int deleteList(String skuCode);

    List<ApplyProductSkuManufacturer> getApply(@Param("skuCode") String skuCode, @Param("applyCode") String applyCode);

    List<ProductSkuManufacturerRespVo> getApplys(@Param("skuCode") String skuCode, @Param("applyCode") String applyCode);

    /**
     * 根据条件获取正式数据,商品供应商管理
     * @param productSkuManufacturerReqVO
     * @return
     */
    List<ProductSkuManufacturerRespVo> getPageList(ProductSkuManufacturerReqVO productSkuManufacturerReqVO);

    void deleteById(@Param("id") Long id);
}

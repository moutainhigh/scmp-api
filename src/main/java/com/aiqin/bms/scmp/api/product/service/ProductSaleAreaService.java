package com.aiqin.bms.scmp.api.product.service;

import com.aiqin.bms.scmp.api.base.AreaBasic;
import com.aiqin.bms.scmp.api.base.BasePage;
import com.aiqin.bms.scmp.api.product.domain.dto.salearea.ApplyProductSkuSaleAreaMainDTO;
import com.aiqin.bms.scmp.api.product.domain.dto.salearea.ProductSkuSaleAreaMainDraftDTO;
import com.aiqin.bms.scmp.api.product.domain.excel.SkuSaleAreaImport;
import com.aiqin.bms.scmp.api.product.domain.pojo.*;
import com.aiqin.bms.scmp.api.product.domain.product.apply.ProductApplyInfoRespVO;
import com.aiqin.bms.scmp.api.product.domain.product.apply.ProductSaleAreaApplyVO;
import com.aiqin.bms.scmp.api.product.domain.request.product.apply.QueryProductApplyRespVO;
import com.aiqin.bms.scmp.api.product.domain.request.salearea.*;
import com.aiqin.bms.scmp.api.product.domain.response.product.apply.QueryProductApplyReqVO;
import com.aiqin.bms.scmp.api.product.domain.response.salearea.*;
import com.aiqin.bms.scmp.api.supplier.domain.response.apply.DetailRequestRespVo;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowCallbackVO;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * Description:
 *
 * @author: NullPointException
 * @date: 2019-05-09
 * @time: 17:07
 */
public interface ProductSaleAreaService {
    /**
     * 新增销售区域草稿
     * @author NullPointException
     * @date 2019/5/9
     * @param request
     * @return java.lang.Boolean
     */
    Boolean addSaleArea(ProductSkuSaleAreaMainReqVO request) throws Exception;

    /**
     * 保存新增销售区域草稿
     * @author NullPointException
     * @date 2019/5/9
     * @param request
     * @return java.lang.Boolean
     */
    Boolean saveData(ProductSkuSaleAreaMainReqVO request) throws Exception;
    /**
     * 保存数据
     * @author NullPointException
     * @date 2019/6/4
     * @param copy
     * @param productSkuSaleAreaInfoDrafts
     * @param channelDrafts
     * @return void
     */
    void saveDraftData(ProductSkuSaleAreaMainDraft copy, List<ProductSkuSaleAreaInfoDraft> productSkuSaleAreaInfoDrafts, List<ProductSkuSaleAreaChannelDraft> channelDrafts);

    /**
     * 分页获取销售区域正式数据
     * @author NullPointException
     * @date 2019/5/10
     * @param request
     * @return com.github.pagehelper.PageInfo<com.aiqin.mgs.product.api.domain.response.salearea.QueryProductSaleAreaRespVO>
     */
    BasePage<QueryProductSaleAreaMainRespVO> queryListForOfficial(QueryProductSaleAreaMainReqVO request);
    /**
     * 获取销售区域草稿数据
     * @author NullPointException
     * @date 2019/5/10
     * @param companyCode 公司编码
     * @return java.util.List<com.aiqin.mgs.product.api.domain.response.salearea.QueryProductSaleAreaRespVO>
     */
    List<QueryProductSaleAreaMainRespVO> queryListForDraft(String companyCode);
    /**
     * 保存提交数据
     * @author NullPointException
     * @date 2019/5/10
     * @param reqVO 请求参数
     * @return java.lang.Boolean
     */
    Boolean addSaleAreaApply(ApplySaleAreaReqVO reqVO);
    /**
     * 保存申请数据
     * @author NullPointException
     * @date 2019/6/4
     * @param temps 主表数据
     * @param areaInfoList 区域数据
     * @param channelInfoList 渠道数据
     * @param skuInfoList sku数据
     * @return void
     */
    void saveApplyData(List<ApplyProductSkuSaleAreaMain> temps, List<ApplyProductSkuSaleAreaInfo> areaInfoList, List<ApplyProductSkuSaleAreaChannel> channelInfoList, List<ApplyProductSkuSaleArea> skuInfoList);

    /**
     * 删除数据
     * @author NullPointException
     * @date 2019/6/4
     * @param dto 主表数据
     * @param skuList sku信息
     * @param areaList 区域信息
     * @param channelList  渠道信息
     * @return void
     */
    void deleteDraftBatchByCodes(List<ProductSkuSaleAreaMainDraftDTO> dto, List<ProductSkuSaleAreaDraft> skuList, List<ProductSkuSaleAreaInfoDraft> areaList, List<ProductSkuSaleAreaChannelDraft> channelList);

    /**
     * 调用审批流
     * @author NullPointException
     * @date 2019/5/10
     * @param formNo 表单号
     * @param applyCode 申请编码
     * @param userName 登录人
     * @return void
     */
    void workFlow(String formNo, String applyCode, String userName,String directSupervisorCode, String positionCode);
    /**
     * 处理保存或修改正式数据
     * @author NullPointException
     * @date 2019/5/13
     * @param newVO
     * @param list
     * @return void
     */
    void saveOfficial(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> list) throws Exception;
    /**
     * 批量更新正式表数据
     * @author NullPointException
     * @date 2019/5/13
     * @param newVO
     * @param updateList
     * @return void
     */
    void updateBatchForOfficial(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> updateList) throws Exception;
    /**
     * 通过编码集合删除附表数据
     * @author NullPointException
     * @date 2019/6/5
     * @param codes
     * @param oldSkuList
     * @param oldAreaList
     * @param oldChannelList
     * @return void
     */
    void deleteOfficialSubByCodes(Set<String> codes, List<ProductSkuSaleArea> oldSkuList, List<ProductSkuSaleAreaInfo> oldAreaList, List<ProductSkuSaleAreaChannel> oldChannelList);

    /**
     * 批量新增正式数据
     * @author NullPointException
     * @date 2019/5/13
     * @param newVO
     * @param addList
     * @return void
     */
    void saveBatchForOfficial(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> addList) throws Exception;
    /**
     * 批量插入附表数据
     * @author NullPointException
     * @date 2019/6/5
     * @param skuList
     * @param areaList
     * @param channelList
     * @return void
     */
    void saveOfficeSubData(List<ProductSkuSaleArea> skuList, List<ProductSkuSaleAreaInfo> areaList, List<ProductSkuSaleAreaChannel> channelList);

    /**
     * 审批取消
     * @author NullPointException
     * @date 2019/5/13
     * @param newVO
     * @param list
     * @return void
     */
    void cancel(WorkFlowCallbackVO newVO, List<ApplyProductSkuSaleAreaMainDTO> list);
    /**
     * 通过formNo更新数据
     * @author NullPointException
     * @date 2019/5/13
     * @param req
     * @return void
     */
    Integer updateApplyInfoByVO(ApplyProductSkuSaleAreaInfoReq req);

    /**
     * 审批驳回
     * @author NullPointException
     * @date 2019/5/13
     * @param vo
     * @param list
     * @return void
     */
    void approvalRejection(WorkFlowCallbackVO vo, List<ApplyProductSkuSaleAreaMainDTO> list);
    /**
     * 查询销售区域申请列表
     * @author NullPointException
     * @date 2019/5/14
     * @param reqVo
     * @return java.util.List<com.aiqin.mgs.product.api.domain.request.product.apply.QueryProductApplyRespVO>
     */
    List<QueryProductApplyRespVO> queryApplyList(QueryProductApplyReqVO reqVo);
    /**
     * 销售区域信息
     * @author NullPointException
     * @date 2019/5/15
     * @param code
     * @return com.aiqin.mgs.product.api.domain.product.apply.ProductApplyInfoRespVO
     */
    ProductApplyInfoRespVO<ProductSaleAreaApplyVO> applyView(String code);
    /**
     * 销售区域正式详情
     * @author NullPointException
     * @date 2019/5/15
     * @param code
     */
    ProductSaleAreaForOfficialMainRespVO officialView(String code);
    /**
     * 删除临时表数据
     * @author NullPointException
     * @date 2019/5/16
     * @param code
     * @return java.lang.Boolean
     */
    Boolean deleteDraft(String code);
    /**
     * 取消申请
     * @author NullPointException
     * @date 2019/6/5
     * @param code
     * @return java.lang.Boolean
     */
    Boolean cancelApply(String code);
    /**
     * 编辑时正式数据
     * @author NullPointException
     * @date 2019/6/5
     * @return com.aiqin.mgs.product.api.domain.response.salearea.ProductSaleAreaForOfficialMainRespVO
     */
    Boolean editView (ProductSkuSaleAreaMainReqVO productSkuSaleAreaMainReqVO) throws Exception ;
    /**
     * 销售区域sku列表的查询
     * @author NullPointException
     * @date 2019/6/6
     * @param reqVO
     * @return java.util.List<com.aiqin.mgs.product.api.domain.response.salearea.QueryProductSaleAreaSkuRespVO>
     */
    BasePage<QueryProductSaleAreaSkuRespVO> officialSkuList(QueryProductSaleAreaReqVO reqVO);
    /**
     * 渠道选择sku信息
     * @author NullPointException
     * @date 2019/6/6
     * @param reqVO
     * @return com.aiqin.mgs.product.api.base.BasePage<com.aiqin.mgs.product.api.domain.response.salearea.QueryProductSaleAreaForSkuRespVO>
     */
    BasePage<QueryProductSaleAreaForSkuRespVO> skuList(QueryProductSaleAreaForSkuReqVO reqVO);
    /**
     * 模糊搜索
     * @author NullPointException
     * @date 2019/6/13
     * @param name
     * @return java.util.List<ProductSaleAreaFuzzySearchRespVO>
     */
    List<ProductSaleAreaFuzzySearchRespVO> fuzzySearch(String name);
    /**
     * 区域列表
     * @author NullPointException
     * @date 2019/6/13
     * @param req
     * @return com.aiqin.bms.scmp.api.base.BasePage<com.aiqin.bms.scmp.api.base.AreaBasic>
     */
    BasePage<AreaBasic> areaList(QueryAreaReqVO req);
    /**
     * 门店列表
     * @author NullPointException
     * @date 2019/6/13
     * @param req
     * @return com.aiqin.bms.scmp.api.base.BasePage<StoreInfo>
     */
    BasePage<StoreInfo> storeList(QueryStoreReqVO req);

    DetailRequestRespVo getInfoByForm(String formNo);

    HttpResponse skuAreaSale(String skuCode, String provinceCode, String storeCode);

   QueryProductSaleAreaSkuRespVO skuDetail(QueryProductDetailReqVO id);

    Boolean skuEdit(QueryProductDetailReqVO reqVO);


    BasePage<AreaBasic> officialSkuList2(QueryProductSaleAreaReqVO2 reqVO);

    List<SkuSaleAreaImport>  importData(MultipartFile file);

    HttpResponse exportSkuBysaleCode(HttpServletResponse resp, String saleCode);

    Boolean importDataConfirm(  List<SkuSaleAreaImport> list);
}

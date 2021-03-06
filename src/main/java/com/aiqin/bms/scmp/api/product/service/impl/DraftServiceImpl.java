package com.aiqin.bms.scmp.api.product.service.impl;

import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.common.ApprovalTypeEnum;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.config.AuthenticationInterceptor;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.domain.pojo.ProductSkuDraft;
import com.aiqin.bms.scmp.api.product.domain.request.draft.DetailReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.draft.SaveReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.salearea.ApplySaleAreaReqVO;
import com.aiqin.bms.scmp.api.product.domain.request.sku.SaveSkuApplyInfoReqVO;
import com.aiqin.bms.scmp.api.product.domain.request.sku.config.ApplySkuConfigReqVo;
import com.aiqin.bms.scmp.api.product.domain.response.draft.ProductSkuDraftRespVo;
import com.aiqin.bms.scmp.api.product.domain.response.salearea.QueryProductSaleAreaMainRespVO;
import com.aiqin.bms.scmp.api.product.domain.response.sku.config.DetailConfigSupplierRespVo;
import com.aiqin.bms.scmp.api.product.mapper.ProductSkuConfigDraftMapper;
import com.aiqin.bms.scmp.api.product.mapper.ProductSkuDraftMapper;
import com.aiqin.bms.scmp.api.product.mapper.ProductSkuSupplyUnitDraftMapper;
import com.aiqin.bms.scmp.api.product.service.*;
import com.aiqin.bms.scmp.api.util.AuthToken;
import com.aiqin.bms.scmp.api.util.BeanCopyUtils;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author knight.xie
 * @version 1.0
 * @className DraftServiceImpl
 * @date 2019/5/14 11:24
 */
@Service
@Slf4j
public class DraftServiceImpl implements DraftService {

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private ApplyProductDraftService applyProductDraftService;

    @Autowired
    private ProductSaleAreaService productSaleAreaService;

    @Autowired
    private ProductSkuConfigService productSkuConfigService;
    @Autowired
    private ProductSkuDraftMapper productSkuDraftMapper;
    @Autowired
    private ProductSkuSupplyUnitService productSkuSupplyUnitService;
    @Autowired
    private ProductSkuSupplyUnitDraftMapper productSkuSupplyUnitDraftMapper;

    @Autowired
    private ProductSkuConfigDraftMapper draftMapper;

    /**
     * 根据审批类型获取审批单数据
     *
     * @param approvalType
     * @return
     */
    @Override
    public HttpResponse list(Integer approvalType) {
        ApprovalTypeEnum approvalTypeEnum = ApprovalTypeEnum.getByType(approvalType);
        if(null == approvalTypeEnum){
            throw new BizException(ResultCode.OBJECT_NOT_FOUND);
        }
        String companyCode = "";
        String personId = "";
        AuthToken authToken = AuthenticationInterceptor.getCurrentAuthToken();
        if(null != authToken){
            companyCode = authToken.getCompanyCode();
            personId = authToken.getPersonId();
        }
        log.info("获取的公司信息编码[{}]",companyCode);
        HttpResponse httpResponse = null;
        if (Objects.equals(ApprovalTypeEnum.PRODUCT_SKU,approvalTypeEnum)) {
            log.info("获取商品信息");
            List<ProductSkuDraftRespVo> productSkuDraftRespVos = skuInfoService.getProductSkuDraftsByCompanyCode(companyCode,personId);
            log.info("获取商品信息,结果{}", JSON.toJSON(productSkuDraftRespVos));
            httpResponse = HttpResponse.success(productSkuDraftRespVos);
        }else if (Objects.equals(ApprovalTypeEnum.PRODUCT_CONFIG,approvalTypeEnum)) {
            log.info("获取商品配置信息数据");
            DetailConfigSupplierRespVo configsRepsVo = productSkuConfigService.findDraftList(companyCode);
            log.info("获取商品配置信息,结果{}", JSON.toJSON(configsRepsVo));
            httpResponse = HttpResponse.success(configsRepsVo);
        }else if (Objects.equals(ApprovalTypeEnum.SALES_AREA,approvalTypeEnum)) {
            log.info("获取销售区域信息数据");
            List<QueryProductSaleAreaMainRespVO> saleAreaRespVOS = productSaleAreaService.queryListForDraft(companyCode);
            log.info("获取销售区域信息,结果{}", JSON.toJSON(saleAreaRespVOS));
            httpResponse = HttpResponse.success(saleAreaRespVOS);
        }
        return httpResponse;
    }

    /**
     * 获取商品申请单详情
     *
     * @param reqVo
     * @return
     */
    @Override
    public HttpResponse detail(DetailReqVo reqVo) {
        ApprovalTypeEnum approvalTypeEnum = ApprovalTypeEnum.getByType(reqVo.getApprovalType());
        if(null == approvalTypeEnum){
            throw new BizException(ResultCode.OBJECT_NOT_FOUND);
        }
        HttpResponse httpResponse = null;
        if (Objects.equals(ApprovalTypeEnum.PRODUCT_SKU,approvalTypeEnum)) {
            httpResponse = HttpResponse.success(skuInfoService.getSkuDraftInfo(reqVo.getCode()));
        }else if (Objects.equals(ApprovalTypeEnum.PRODUCT_CONFIG,approvalTypeEnum)) {

        }else if (Objects.equals(ApprovalTypeEnum.SALES_AREA,approvalTypeEnum)) {

        }
        return httpResponse;
    }

    /**
     * 删除商品申请单明细
     *
     * @param reqVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse<Integer> delete(DetailReqVo reqVo) {
        ApprovalTypeEnum approvalTypeEnum = ApprovalTypeEnum.getByType(reqVo.getApprovalType());
        if(null == approvalTypeEnum){
            throw new BizException(ResultCode.OBJECT_NOT_FOUND);
        }
        if (Objects.equals(ApprovalTypeEnum.PRODUCT_SKU,approvalTypeEnum)) {
            if(Objects.nonNull(reqVo.getCode())){
                List<String> skuCodes = Lists.newArrayList();
                skuCodes.add(reqVo.getCode());
                skuInfoService.deleteProductSkuDraft(skuCodes);
            }
        }else if (Objects.equals(ApprovalTypeEnum.PRODUCT_CONFIG,approvalTypeEnum)) {
            if(Objects.nonNull(reqVo.getId())){
                if(Objects.equals(reqVo.getConfigType(),DetailReqVo.DEL_CONFIG)){
                    productSkuConfigService.deleteDraftById(reqVo.getId());
                } else if (Objects.equals(reqVo.getConfigType(),DetailReqVo.DEL_SUPPLIER)) {
                    productSkuSupplyUnitService.deleteDraftById(reqVo.getId());
                }
            }
        }else if (Objects.equals(ApprovalTypeEnum.SALES_AREA,approvalTypeEnum)) {
            if(Objects.nonNull(reqVo.getCode())){
                productSaleAreaService.deleteDraft(reqVo.getCode());
            }
        }
        return HttpResponse.success(1);
    }

    /**
     * 保存申请单
     *
     * @param reqVo
     * @return
     */
    @Override
    public HttpResponse<Integer> save(SaveReqVo reqVo) {
        ApprovalTypeEnum approvalTypeEnum = ApprovalTypeEnum.getByType(reqVo.getApprovalType());
        if(null == approvalTypeEnum){
            throw new BizException(ResultCode.OBJECT_NOT_FOUND);
        }
        if(null == reqVo.getData()){
            throw new BizException(ResultCode.APPLY_DATA_EMPTY);
        }
        String s = "";
        if (Objects.equals(ApprovalTypeEnum.PRODUCT_SKU,approvalTypeEnum)) {
            SaveSkuApplyInfoReqVO saveSkuApplyInfoReqVO = new SaveSkuApplyInfoReqVO();
            BeanCopyUtils.copy(reqVo,saveSkuApplyInfoReqVO);
            List<String> skuCodes = (List<String>) reqVo.getData();
            saveSkuApplyInfoReqVO.setSkuCodes(skuCodes);
            s = skuInfoService.saveSkuApplyInfo(saveSkuApplyInfoReqVO, reqVo.getApprovalName(), reqVo.getApprovalRemark());
           //进行配置增加
        }else if (Objects.equals(ApprovalTypeEnum.PRODUCT_CONFIG,approvalTypeEnum)) {
            ApplySkuConfigReqVo applySkuConfigReqVo = new ApplySkuConfigReqVo();
            BeanCopyUtils.copy(reqVo,applySkuConfigReqVo);
            applySkuConfigReqVo.setSkuConfigs(((List<String>) reqVo.getData()));
            productSkuConfigService.insertApplyList(applySkuConfigReqVo);
        }else if (Objects.equals(ApprovalTypeEnum.SALES_AREA,approvalTypeEnum)) {
            ApplySaleAreaReqVO saleAreaReqVO = new ApplySaleAreaReqVO();
            BeanCopyUtils.copy(reqVo,saleAreaReqVO);
            List<String> areaCodes = (List<String>) reqVo.getData();
            saleAreaReqVO.setAreaCodes(areaCodes);
            productSaleAreaService.addSaleAreaApply(saleAreaReqVO);
        }
        return HttpResponse.success(s);
    }


    /**
     * 保存申请单
     *
     * @param reqVo
     * @return
     */
    @Override
    public HttpResponse<Integer> saves(SaveReqVo reqVo) {
        String s = "";
        List<String> configCodes=draftMapper.loadAllConfigCode();
            ApplySkuConfigReqVo applySkuConfigReqVo = new ApplySkuConfigReqVo();
            BeanCopyUtils.copy(reqVo,applySkuConfigReqVo);
            applySkuConfigReqVo.setSkuConfigs((configCodes));
            productSkuConfigService.insertApplyList(applySkuConfigReqVo);
        return HttpResponse.success(s);
    }


    @Override
    public Map<String, ProductSkuDraft> selectBySkuCode(Set<String> skuNameList, String companyCode) {
        return productSkuDraftMapper.selectBySkuCode(skuNameList,companyCode);
    }

    @Override
    public Integer deleteSupply(Long id) {
        return productSkuSupplyUnitDraftMapper.deleteDraftById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse deleteIds(List<Long> ids) {
        if(null == ids){
            throw new BizException(ResultCode.APPLY_DATA_EMPTY);
        }
        if(CollectionUtils.isNotEmptyCollection(ids)){
            for (Long id:
            ids  ) {
                productSkuConfigService.deleteDraftById(id);
            }
        }
        return HttpResponse.success(ids.size());

    }
}

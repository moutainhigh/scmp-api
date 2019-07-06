package com.aiqin.bms.scmp.api.purchase.service.impl;

import com.aiqin.bms.scmp.api.base.EncodingRuleType;
import com.aiqin.bms.scmp.api.base.PageResData;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.product.dao.StockDao;
import com.aiqin.bms.scmp.api.product.domain.pojo.ProductSkuConfig;
import com.aiqin.bms.scmp.api.product.mapper.ProductSkuConfigMapper;
import com.aiqin.bms.scmp.api.product.mapper.ProductSkuPriceInfoMapper;
import com.aiqin.bms.scmp.api.purchase.dao.PurchaseApplyDao;
import com.aiqin.bms.scmp.api.purchase.dao.PurchaseApplyProductDao;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseApply;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseApplyProduct;
import com.aiqin.bms.scmp.api.purchase.domain.request.PurchaseApplyProductRequest;
import com.aiqin.bms.scmp.api.purchase.domain.request.PurchaseApplyRequest;
import com.aiqin.bms.scmp.api.purchase.domain.response.PurchaseApplyDetailResponse;
import com.aiqin.bms.scmp.api.purchase.domain.response.PurchaseApplyProductInfoResponse;
import com.aiqin.bms.scmp.api.purchase.domain.response.PurchaseApplyResponse;
import com.aiqin.bms.scmp.api.purchase.domain.response.PurchaseImportResponse;
import com.aiqin.bms.scmp.api.purchase.service.PurchaseApplyService;
import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.dao.logisticscenter.LogisticsCenterDao;
import com.aiqin.bms.scmp.api.supplier.dao.supplier.SupplierDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.EncodingRule;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.LogisticsCenter;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.Supplier;
import com.aiqin.bms.scmp.api.util.CollectionUtils;
import com.aiqin.bms.scmp.api.util.FileReaderUtil;
import com.aiqin.ground.util.id.IdUtil;
import com.aiqin.ground.util.protocol.MessageId;
import com.aiqin.ground.util.protocol.Project;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhao shuai
 * @create: 2019-06-13
 **/
@Service
public class PurchaseApplyServiceImpl implements PurchaseApplyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseApplyServiceImpl.class);
    private static final String[] importRejectApplyHeaders = new String[]{
            "SKU编号", "SKU名称", "供应商", "仓库", "采购数量", "实物返数量", "含税单价",
    };

    @Resource
    private PurchaseApplyDao purchaseApplyDao;
    @Resource
    private PurchaseApplyProductDao purchaseApplyProductDao;
    @Resource
    private ProductSkuPriceInfoMapper productSkuPriceInfoMapper;
    @Resource
    private EncodingRuleDao encodingRuleDao;
    @Resource
    private StockDao stockDao;
    @Resource
    private LogisticsCenterDao logisticsCenterDao;
    @Resource
    private SupplierDao supplierDao;
    @Resource
    private ProductSkuConfigMapper productSkuConfigDao;

    @Override
    public HttpResponse applyList(PurchaseApplyRequest purchaseApplyRequest){
        PageResData pageResData = new PageResData();
        List<PurchaseApplyResponse> purchases = purchaseApplyDao.applyList(purchaseApplyRequest);
        if(CollectionUtils.isNotEmptyCollection(purchases)){
            for (PurchaseApplyResponse apply:purchases){
                // 计算sku数量 / 单品数量/ 采购含税金额 / 实物返金额
                PurchaseApplyProductInfoResponse info = this.applyProductReckon(apply.getPurchaseApplyId());
                // 计算sku数量
                Integer skuCount = purchaseApplyProductDao.skuCount(apply.getPurchaseApplyId(), null);
                apply.setSkuCount(skuCount);
                apply.setSingleCount(info.getSingleSum());
                apply.setProductTotalAmount(info.getTaxSum());
                apply.setReturnAmount(info.getMatterTaxSum());
                Integer count = purchaseApplyProductDao.skuCount(apply.getPurchaseApplyId(), Global.PURCHASE_APPLY_STATUS_1);
                if(count >= skuCount){
                    apply.setApplyStatus(Global.PURCHASE_APPLY_STATUS_1);
                }else if(count > 0){
                    apply.setApplyStatus(Global.PURCHASE_APPLY_STATUS_2);
                }else {
                    apply.setApplyStatus(Global.PURCHASE_APPLY_STATUS_0);
                }
            }
        }
        Integer count = purchaseApplyDao.applyCount(purchaseApplyRequest);
        pageResData.setDataList(purchases);
        pageResData.setTotalCount(count);
        return HttpResponse.success(pageResData);
    }

    @Override
    public HttpResponse applyProductList(PurchaseApplyRequest purchases){
        if(StringUtils.isBlank(purchases.getPurchaseGroupCode())){
            return HttpResponse.failure(ResultCode.NOT_PURCHASE_GROUP_DATA);
        }
        this.fourProduct(purchases);
        // 新增时的商品信息
        PageResData pageResData = new PageResData();
        if(StringUtils.isBlank(purchases.getPurchaseApplyId())){
            return this.stockProductInfo(purchases, pageResData);
        }
        // 编辑时的商品信息
        List<PurchaseApplyDetailResponse> applyProducts = purchaseApplyProductDao.applyProductList(purchases);
        if(CollectionUtils.isNotEmptyCollection(applyProducts)){
            PurchaseApplyProduct applyProduct = null;
            for(PurchaseApplyDetailResponse product:applyProducts){
                product.setReturnWhole(0);
                product.setReturnSingle(0);
                applyProduct = new PurchaseApplyProduct();
                applyProduct.setSkuCode(product.getSkuCode());
                if(product.getProductType().equals(Global.PRODUCT_TYPE_0)) {
                    applyProduct.setProductType(Global.PRODUCT_TYPE_2);
                    // 查询相同sku的实物返商品
                    PurchaseApplyProduct pro = purchaseApplyProductDao.applyProduct(applyProduct);
                    if(pro != null){
                        product.setReturnWhole(pro.getPurchaseWhole() == null ? 0 : pro.getPurchaseWhole());
                        product.setReturnSingle(pro.getPurchaseSingle() == null ? 0 : pro.getPurchaseSingle());
                    }
                }else if(product.getProductType().equals(Global.PRODUCT_TYPE_2)) {
                    applyProduct.setProductType(Global.PRODUCT_TYPE_0);
                    // 查询相同sku的实物返商品
                    PurchaseApplyProduct pro = purchaseApplyProductDao.applyProduct(applyProduct);
                    if(pro != null){
                        product.setPurchaseWhole(pro.getPurchaseWhole() == null ? 0 : pro.getPurchaseWhole());
                        product.setPurchaseWhole(pro.getPurchaseSingle() == null ? 0 : pro.getPurchaseSingle());
                    }
                }
            }
        }
        Integer count = purchaseApplyProductDao.applyProductCount(purchases);
        pageResData.setDataList(applyProducts);
        pageResData.setTotalCount(count);
        return HttpResponse.success(pageResData);
    }

    // 查询新增采购申请单时候的库存商品信息
    private HttpResponse stockProductInfo(PurchaseApplyRequest purchases, PageResData pageResData) {
        // 查询库存，商品， 供应商等信息
        List<PurchaseApplyDetailResponse> detail = stockDao.purchaseProductList(purchases);
        if (CollectionUtils.isNotEmptyCollection(detail)) {
            for (PurchaseApplyDetailResponse product : detail) {
                // 获取最高采购价(价格管理中供应商的含税价格)
                if (StringUtils.isNotBlank(product.getSkuCode()) && StringUtils.isNotBlank(product.getSupplierCode())) {
                    Long priceTax = productSkuPriceInfoMapper.selectPriceTax(product.getSkuCode(), product.getSupplierCode());
                    product.setPurchaseMax(priceTax == null ? 0 : priceTax.intValue());
                }
                // 报表取数据(预测采购件数， 预测到货时间， 近90天销量 )
                // TODO
            }
        }
        Integer count = stockDao.purchaseProductCount(purchases);
        pageResData.setDataList(detail);
        pageResData.setTotalCount(count);
        return HttpResponse.success(pageResData);
    }

    private PurchaseApplyRequest fourProduct(PurchaseApplyRequest purchases){
        // 查询14大A品建议补货
        if(purchases.getAReplenishType() != null && purchases.getAReplenishType() == 0){
            // TODO
            //purchases.setAReplenish();
        }
        // 查询畅销商品建议补货
        if(purchases.getProductReplenishType() != null && purchases.getProductReplenishType() == 0){

        }
        // 查询14大A品缺货
        if(purchases.getAShortageType() != null && purchases.getAShortageType() == 0){

        }
        // 查询畅销商品缺货
        if(purchases.getProductShortageType() != null && purchases.getProductShortageType() == 0){

        }
        return purchases;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse purchaseApplyForm(PurchaseApplyProductRequest applyProductRequest){
        List<PurchaseApplyProduct> applyProducts = applyProductRequest.getApplyProducts();
        if(CollectionUtils.isEmptyCollection(applyProducts)){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        // 判断此采购申请单的商品之前是否已生成
        String purchaseApplyId = null;
        if(StringUtils.isNotBlank(applyProductRequest.getPurchaseApplyId())){
            purchaseApplyId = applyProductRequest.getPurchaseApplyId();
            purchaseApplyProductDao.delete(purchaseApplyId);
        }else {
            // 生成采购申请单id
            purchaseApplyId = IdUtil.purchaseId();
        }
        // 保存采购申请选中商品
        EncodingRule encodingRule = encodingRuleDao.getNumberingType(EncodingRuleType.PURCHASE_APPLY_CODE);
        for(PurchaseApplyProduct product:applyProducts){
            product.setApplyProductId(IdUtil.purchaseId());
            product.setPurchaseApplyId(purchaseApplyId);
            product.setPurchaseApplyCode("CS" + String.valueOf(encodingRule.getNumberingValue()));
        }
        Integer productCount = purchaseApplyProductDao.insertAll(applyProducts);
        if(productCount > 0){
            // 生成采购申请单
            PurchaseApply purchaseApply = new PurchaseApply();
            purchaseApply.setPurchaseApplyId(purchaseApplyId);
            // 获取采购申请单编码
            purchaseApply.setPurchaseApplyCode("CS" + String.valueOf(encodingRule.getNumberingValue()));
            purchaseApply.setApplyType(Global.PURCHASE_APPLY_TYPE_0);
            purchaseApply.setApplyStatus(Global.PURCHASE_APPLY_STATUS_0);
            purchaseApply.setPurchaseGroupCode(applyProducts.get(0).getPurchaseGroupCode());
            purchaseApply.setPurchaseGroupName(applyProducts.get(0).getPurchaseGroupName());
            purchaseApply.setCreateById(applyProducts.get(0).getCreateById());
            purchaseApply.setCreateByName(applyProducts.get(0).getCreateByName());
            Integer count = purchaseApplyDao.insert(purchaseApply);
            if(count > 0){
                encodingRuleDao.updateNumberValue(encodingRule.getNumberingValue(), encodingRule.getId());
            }
        }
        return HttpResponse.success();
    }

    @Override
    public HttpResponse searchApplyProduct(String applyProductId){
        if(StringUtils.isBlank(applyProductId)){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        PurchaseApplyProduct product = new PurchaseApplyProduct();
        product.setApplyProductId(applyProductId);
        PurchaseApplyProduct purchaseApplyProduct = purchaseApplyProductDao.applyProduct(product);
        if(purchaseApplyProduct == null){
            LOGGER.info("查询采购申请商品的信息失败...{}:" +applyProductId);
            return HttpResponse.failure(ResultCode.SEARCH_ERROR);
        }
        return HttpResponse.success(purchaseApplyProduct);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse deleteApplyProduct(String applyProductId){
        if(StringUtils.isBlank(applyProductId)){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        PurchaseApplyProduct purchaseApplyProduct = new PurchaseApplyProduct();
        purchaseApplyProduct.setApplyProductId(applyProductId);
        purchaseApplyProduct.setApplyProductStatus(Global.USER_OFF);
        Integer count = purchaseApplyProductDao.update(purchaseApplyProduct);
        if(count == 0){
            LOGGER.info("删除采购申请单的商品信息失败...{}" + applyProductId);
            return HttpResponse.failure(ResultCode.DELETE_ERROR);
        }
        return HttpResponse.success();
    }

    @Override
    public HttpResponse applyProductBasic(String purchaseApplyId){
        if(StringUtils.isBlank(purchaseApplyId)){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        PurchaseApplyProductInfoResponse info = this.applyProductReckon(purchaseApplyId);
        return HttpResponse.success(info);
    }

    private PurchaseApplyProductInfoResponse applyProductReckon(String purchaseApplyId){
        // 计算商品（实物返，赠品）采购件数 、 单品总数 、含税总金额
        List<PurchaseApplyDetailResponse> products = purchaseApplyProductDao.productListByDetail(purchaseApplyId);
        PurchaseApplyProductInfoResponse info = new PurchaseApplyProductInfoResponse();
        Integer productPieceSum = 0, matterPieceSum = 0, giftPieceSum = 0;
        Integer productSingleSum= 0, matterSingleSum = 0, giftSingleSum = 0;
        Integer productTaxSum = 0, matterTaxSum = 0;
        if(CollectionUtils.isNotEmptyCollection(products)) {
            for (PurchaseApplyDetailResponse product : products) {
                // 商品采购件数量
                Integer purchaseWhole = product.getPurchaseWhole() == null ? 0 : product.getPurchaseWhole();
                Integer purchaseSingle = product.getPurchaseSingle() == null ? 0 : product.getPurchaseSingle();
                // 包装数量
                Integer packNumber = product.getBaseProductContent() == null ? 0 : product.getBaseProductContent();
                // 计算商品采购件数量
                Integer singleCount = purchaseWhole * packNumber + purchaseSingle;
                // 计算采购含税总价
                Integer productPurchaseAmount = product.getProductPurchaseAmount() == null ? 0 : product.getProductPurchaseAmount();
                Integer productPurchaseSum = productPurchaseAmount * singleCount;
                product.setProductPurchaseSum(productPurchaseSum);
                if(product.getProductType() != null){
                    if(product.getProductType() == 0){
                        productPieceSum += purchaseWhole;
                        productSingleSum += singleCount;
                        productTaxSum += productPurchaseSum;
                    }else if(product.getProductType() == 1){
                        matterPieceSum += purchaseWhole;
                        matterSingleSum += singleCount;
                        matterTaxSum += productPurchaseSum;
                    }else if(product.getProductType() == 2){
                        giftSingleSum += purchaseWhole;
                        giftSingleSum += singleCount;
                    }
                }
            }
        }
        info.setProductPieceSum(productPieceSum);
        info.setProductSingleSum(productSingleSum);
        info.setProductTaxSum(productTaxSum);
        info.setMatterPieceSum(matterPieceSum);
        info.setMatterSingleSum(matterSingleSum);
        info.setMatterTaxSum(matterTaxSum);
        info.setGiftPieceSum(giftPieceSum);
        info.setGiftSingleSum(giftSingleSum);
        info.setGiftTaxSum(0);
        info.setPieceSum(productPieceSum + matterPieceSum + giftPieceSum);
        info.setSingleSum(productSingleSum + matterSingleSum + giftSingleSum);
        info.setTaxSum(productTaxSum);
        return info;
    }

    @Override
    public HttpResponse applySelectionProduct(String purchaseApplyId){
        if(StringUtils.isBlank(purchaseApplyId)){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        List<PurchaseApplyDetailResponse> products = purchaseApplyProductDao.productListByDetail(purchaseApplyId);
        if(CollectionUtils.isNotEmptyCollection(products)){
            for(PurchaseApplyDetailResponse product:products){
                // 计算单品总数
                Integer purchaseWhole = product.getPurchaseWhole() == null ? 0 : product.getPurchaseWhole();
                Integer purchaseSingle = product.getPurchaseSingle() == null ? 0 : product.getPurchaseSingle();
                // 查询采购 对应的sku基商品含量
                Integer packNumber = product.getBaseProductContent() == null ? 0 : product.getBaseProductContent();
                Integer singleCount = purchaseWhole * packNumber + purchaseSingle;
                product.setSingleCount(singleCount);
                // 计算采购含税总价
                Integer productPurchaseAmount = product.getProductPurchaseAmount() == null ? 0 : product.getProductPurchaseAmount();
                Integer productPurchaseSum = productPurchaseAmount * singleCount;
                product.setProductPurchaseSum(productPurchaseSum);
            }
        }
        return HttpResponse.success(products);
    }

    @Override
    public HttpResponse purchaseApplyImport(MultipartFile file, String purchaseGroupCode){
        if (file == null) {
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        try {
            String[][] result = FileReaderUtil.readExcel(file, importRejectApplyHeaders.length);
            if(result.length<2){
                return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
            }
            List<PurchaseImportResponse> list = new ArrayList<>();
            Integer errorCount = 0;
            if (result != null) {
                String validResult = FileReaderUtil.validStoreValue(result, importRejectApplyHeaders);
                if (StringUtils.isNotBlank(validResult)) {
                    return HttpResponse.failure(MessageId.create(Project.SCMP_API, 88888, validResult));
                }
                String[] record;
                Supplier supplier;
                LogisticsCenter logisticsCenter;
                PurchaseImportResponse response ;
                PurchaseApplyProduct applyProduct;
                for (int i = 1; i <= result.length - 1; i++) {
                    record = result[i];
                    response = new PurchaseImportResponse();
                    if (StringUtils.isBlank(record[0]) || StringUtils.isBlank(record[1]) || StringUtils.isBlank(record[2]) ||
                            StringUtils.isBlank(record[3]) || StringUtils.isBlank(record[4]) || StringUtils.isBlank(record[5]) || StringUtils.isBlank(record[6])) {
                        HandleResponse(response, record,"导入的数据不全");
                        errorCount++;
                        continue;
                    }
                    supplier = supplierDao.selectBySupplierName(record[2]);
                    if(supplier==null){
                        HandleResponse(response, record,"未查询到供应商信息");
                        errorCount++;
                        continue;
                    }
//                    logisticsCenter = logisticsCenterDao.selectByCenterName(record[3]);
//                    if(logisticsCenter==null){
//                        HandleResponse(response, record,"未查询到仓库信息");
//                        errorCount++;
//                        continue;
//                    }
                    applyProduct = stockDao.purchaseBySkuStock(purchaseGroupCode, record[0], supplier.getSupplierCode(), "1028");
                    if(applyProduct != null){
                         // 报表取缺货影响的销售额， 缺货天数， 预测订货件数, 库存周转期
                        // TODO
                        // 获取到货后周转期
                       // ProductSkuConfig cycleInfo = productSkuConfigDao.getCycleInfo(applyProduct.getSkuCode(), applyProduct.getTransportCenterCode());
//                        if(cycleInfo != null){
//                            applyProduct.setReceiptTurnover(cycleInfo.getTurnoverPeriodAfterArrival());
//                        }
                        // 获取最高采购价(价格管理中供应商的含税价格)
                        if (StringUtils.isNotBlank(applyProduct.getSkuCode()) && StringUtils.isNotBlank(applyProduct.getSupplierCode())) {
                            Long priceTax = productSkuPriceInfoMapper.selectPriceTax(applyProduct.getSkuCode(), applyProduct.getSupplierCode());
                            applyProduct.setPurchaseMax(priceTax == null ? 0 : priceTax.intValue());
                        }
                        Integer singCount = 0, productTotalAmount = 0;
                        Integer content  = applyProduct.getBaseProductContent() == null ? 0 : applyProduct.getBaseProductContent();
                         if(record[4] != null){
                             int index = record[4].indexOf("零");
                             int length = record[4].length();
                             Integer whole = Integer.valueOf(record[4].substring(0, index));
                             Integer single = Integer.valueOf(record[4].substring(index,length -1));
                             // 计算单品数量  含税采购价
                             singCount = whole * content + single;
                             productTotalAmount = singCount * singCount;
                             response.setSingleCount(singCount);
                             response.setProductTotalAmount(productTotalAmount);
                             BeanUtils.copyProperties(applyProduct, response);
                             list.add(response);
                         }
                         if(record[5] != null){
                             int index = record[5].indexOf("零");
                             int length = record[5].length();
                             Integer whole = Integer.valueOf(record[5].substring(0, index));
                             Integer single = Integer.valueOf(record[5].substring(index,length -1));
                             // 计算单品数量  含税采购价
                             singCount = whole * content + single;
                             productTotalAmount = singCount * singCount;
                             response.setSingleCount(singCount);
                             response.setProductTotalAmount(productTotalAmount);
                             BeanUtils.copyProperties(applyProduct, response);
                             list.add(response);
                         }
                    }else{
                        HandleResponse(response, record,"未查询到对应的商品");
                        errorCount++;
                    }
                }
            }
            return HttpResponse.success(new PageResData(errorCount,list));
        } catch (Exception e) {
            LOGGER.error("采购申请单导入异常:{}", e.getMessage());
            return HttpResponse.failure(ResultCode.IMPORT_PURCHASE_APPLY_ERROR);
        }
    }

    private void HandleResponse(PurchaseImportResponse response, String[] record,String errorReason) {
        response.setSkuCode(record[0]);
        response.setSkuName(record[1]);
        response.setSupplierName(record[2]);
        response.setTransportCenterName(record[3]);
        response.setPurchaseCount(record[4]);
        response.setReturnCount(record[5]);
        response.setProductPurchaseAmount(Integer.valueOf(record[6]));
        response.setErrorInfo(errorReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse purchaseApplyStatus(PurchaseApply purchaseApply){
        if(purchaseApply == null || StringUtils.isBlank(purchaseApply.getPurchaseApplyId())){
            return HttpResponse.failure(ResultCode.REQUIRED_PARAMETER);
        }
        Integer count = purchaseApplyDao.update(purchaseApply);
        if(count == 0){
            LOGGER.error("修改采购申请单是状态信息失败");
            return HttpResponse.failure(ResultCode.UPDATE_ERROR);
        }
        return HttpResponse.success();
    }
}

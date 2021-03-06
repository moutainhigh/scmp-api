package com.aiqin.bms.scmp.api.product.service;

import com.aiqin.bms.scmp.api.product.domain.pojo.ProductSkuFile;
import com.aiqin.bms.scmp.api.product.domain.response.sku.ProductSkuFileReqVo;
import com.aiqin.bms.scmp.api.product.domain.response.sku.file.ProductSkuFileRespVo;
import com.aiqin.ground.util.protocol.http.HttpResponse;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;

/**
 * @Auther: mamingze
 * @Date: 2019-10-28 17:04
 * @Description:
 */
public interface ProductFileService {
    /**
     * 获取商品的属性 以及相应的文件列表
     * @param
     * @return
     */
    HttpResponse loadFileProduct(String skuCode) throws ParseException;

    /**
     * 对商品内的文件进行修改或者新增
     * @param productSkuFile
     * @return
     */
    HttpResponse updateOrAdd(ProductSkuFileReqVo productSkuFile) throws ParseException;
    /**
     * 对商品文件管理内的文件进行删除
     * @param
     * @param skuCode
     * @return
     */
    HttpResponse deleteFile(Long id, String skuCode);




    HttpResponse<String> uploadFiles(MultipartFile[] multipartFiles, String skuCode);
}

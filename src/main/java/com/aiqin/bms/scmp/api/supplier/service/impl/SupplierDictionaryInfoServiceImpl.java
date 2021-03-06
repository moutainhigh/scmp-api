package com.aiqin.bms.scmp.api.supplier.service.impl;

import com.aiqin.bms.scmp.api.supplier.dao.dictionary.SupplierDictionaryInfoDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.SupplierDictionaryInfo;
import com.aiqin.bms.scmp.api.supplier.domain.response.dictionary.DictionaryInfoResponseVO;
import com.aiqin.bms.scmp.api.supplier.service.SupplierDictionaryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierDictionaryInfoServiceImpl implements SupplierDictionaryInfoService {

    @Autowired
    private SupplierDictionaryInfoDao supplierDictionaryInfoDao;

    @Override
    public List<SupplierDictionaryInfo> getList(String code) {
        return   supplierDictionaryInfoDao.dictionaryCode(code);
    }

    @Override
    public List<DictionaryInfoResponseVO> selectPriceTypeAndCategory(Integer status) {
        //        状态（1.类型，2.大类（属性））
        List<DictionaryInfoResponseVO> dis = supplierDictionaryInfoDao.selectPriceTypeAndCategory(status);
        return dis;
    }

}

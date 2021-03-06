package com.aiqin.bms.scmp.api.supplier.service.impl;

import com.aiqin.bms.scmp.api.supplier.dao.EncodingRuleDao;
import com.aiqin.bms.scmp.api.supplier.domain.pojo.EncodingRule;
import com.aiqin.bms.scmp.api.supplier.mapper.EncodingRuleMapper;
import com.aiqin.bms.scmp.api.supplier.service.EncodingRuleService;
import com.aiqin.bms.scmp.api.util.CodeUtils;
import com.aiqin.ground.util.exception.GroundRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class EncodingRuleServiceImpl implements EncodingRuleService {
    @Autowired
    private EncodingRuleDao encodingRuleDao;
    @Autowired
    private EncodingRuleMapper encodingRuleMapper;

    @Resource
    private CodeUtils codeUtils;
    @Override
    public Long getNumberValue(Long id) {
        return encodingRuleDao.getNumberValue(id);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public int updateNumberValue(Long versionValue, Long id) {
        int k=encodingRuleDao.updateNumberValue(versionValue,id);
        if(k>0){
            return k;
        }else {
            throw new GroundRuntimeException("修改编码失败");
        }
    }

    @Override
    public String getNumberingType(String numberingType) {
        String redisCode = codeUtils.getRedisCode(numberingType);
        return redisCode;
    }
}

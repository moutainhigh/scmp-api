package com.aiqin.bms.scmp.api.product.mapper;

import com.aiqin.bms.scmp.api.product.domain.pojo.SupervisoryWarehouseOrderProduct;

public interface SupervisoryWarehouseOrderProductMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SupervisoryWarehouseOrderProduct record);

    int insertSelective(SupervisoryWarehouseOrderProduct record);

    SupervisoryWarehouseOrderProduct selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SupervisoryWarehouseOrderProduct record);

    int updateByPrimaryKey(SupervisoryWarehouseOrderProduct record);
}
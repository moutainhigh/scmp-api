package com.aiqin.bms.scmp.api.product.mapper;


import com.aiqin.bms.scmp.api.product.domain.request.allocation.ManualChoseProductReq;
import com.aiqin.bms.scmp.api.product.domain.dto.allocation.AllocationDTO;
import com.aiqin.bms.scmp.api.product.domain.pojo.Allocation;
import com.aiqin.bms.scmp.api.product.domain.pojo.StockBatch;
import com.aiqin.bms.scmp.api.product.domain.request.allocation.QueryAllocationReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.movement.QueryMovementReqVo;
import com.aiqin.bms.scmp.api.product.domain.request.scrap.QueryScrapReqVo;
import com.aiqin.bms.scmp.api.product.domain.response.allocation.AllocationResVo;
import com.aiqin.bms.scmp.api.product.domain.response.allocation.ManualChoseProductRespVo;
import com.aiqin.bms.scmp.api.product.domain.response.allocation.QueryAllocationResVo;
import com.aiqin.bms.scmp.api.product.domain.response.movement.MovementResVo;
import com.aiqin.bms.scmp.api.product.domain.response.movement.QueryMovementResVo;
import com.aiqin.bms.scmp.api.product.domain.response.scrap.QueryScrapResVo;
import com.aiqin.bms.scmp.api.product.domain.response.scrap.ScrapResVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface AllocationMapper {


    /**
     * 列表展示以及搜索
     * @param vo
     * @return
     */
    List<QueryAllocationResVo> getList(QueryAllocationReqVo vo);

    int deleteByPrimaryKey(Long id);


    int insert(Allocation record);

    /**
     * 有选择的插入
     * @param record
     * @return
     */
    Long insertSelective(Allocation record);


    /**
     * 查看调拨单主体
     * @param id
     * @return
     */
    Allocation selectByPrimaryKey(Long id);


    AllocationResVo getAllocationDetailById(Long id);

    MovementResVo getMoveDetailById(Long id);

    ScrapResVo getScrapDetailById(Long id);

    /**
     * 有选择的更新
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(Allocation record);

    int updateByPrimaryKey(Allocation record);

    /**
     * 根据流程变成查询申请合同
     * @param FormNo
     * @return
     */
    Allocation selectByFormNO(String FormNo);

    /**
     * 根据流程变成查询申请合同
     * @param FormNo
     * @return
     */
    AllocationDTO selectByFormNO1(String FormNo);

    Long findIdByFormNo(String FormNo);

    Long findIdByAllocationCode(String allocationCode);

    /**
     * 根据编码查询调拨单主体
     * @param allocationCode
     * @return
     */
    Allocation selectByCode(String allocationCode);

    Allocation selectByOutOrderCode(String outboundOderCode);

    Allocation selectByInOrderCode(String inboundOderCode);


    /**
     *
     * 功能描述: 移库列表查询
     *
     * @param vo
     * @return
     * @auther knight.xie
     * @date 2019/7/2 21:50
     */
    List<QueryMovementResVo> getMoveList(QueryMovementReqVo vo);

    /**
     *
     * 功能描述: 报废列表查询
     *
     * @param vo
     * @return
     * @auther knight.xie
     * @date 2019/7/2 22:27
     */
    List<QueryScrapResVo> getScrapList(QueryScrapReqVo vo);

    AllocationDTO selectById(Long id);

    List<Allocation> listByInboundCodes(List<String> list);

    List<Allocation> listByOutboundCodes(List<String> list);

    StockBatch selectNumberByBatchAndSkuCode(@Param("skuCode") String skuCode, @Param("batchInfoCode") String batchInfoCode);

    List<ManualChoseProductRespVo> getManualChoseProduct(ManualChoseProductReq m);

    Long getManualChoseProductCount(ManualChoseProductReq m);
}
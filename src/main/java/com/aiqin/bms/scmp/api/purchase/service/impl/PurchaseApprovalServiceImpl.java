package com.aiqin.bms.scmp.api.purchase.service.impl;

import com.aiqin.bms.scmp.api.base.ApplyStatus;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.base.WorkFlowBaseUrl;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.common.PurchaseOrderLogEnum;
import com.aiqin.bms.scmp.api.common.WorkFlowReturn;
import com.aiqin.bms.scmp.api.constant.Global;
import com.aiqin.bms.scmp.api.purchase.dao.OperationLogDao;
import com.aiqin.bms.scmp.api.purchase.dao.PurchaseOrderDao;
import com.aiqin.bms.scmp.api.purchase.domain.OperationLog;
import com.aiqin.bms.scmp.api.purchase.domain.PurchaseOrder;
import com.aiqin.bms.scmp.api.purchase.service.PurchaseApprovalService;
import com.aiqin.bms.scmp.api.workflow.annotation.WorkFlowAnnotation;
import com.aiqin.bms.scmp.api.workflow.enumerate.WorkFlow;
import com.aiqin.bms.scmp.api.workflow.helper.WorkFlowHelper;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowCallbackVO;
import com.aiqin.bms.scmp.api.workflow.vo.request.WorkFlowVO;
import com.aiqin.bms.scmp.api.workflow.vo.response.WorkFlowRespVO;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@WorkFlowAnnotation(WorkFlow.APPLY_PURCHASE)
public class PurchaseApprovalServiceImpl extends BaseServiceImpl implements PurchaseApprovalService, WorkFlowHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseApprovalServiceImpl.class);

    @Resource
    private WorkFlowBaseUrl workFlowBaseUrl;
    @Resource
    private PurchaseOrderDao purchaseOrderDao;
    @Resource
    private OperationLogDao operationLogDao;

    /**
     * 审核回调接口
     *
     */
    @Override
    public String workFlowCallback(WorkFlowCallbackVO vo1) {
        try {
            WorkFlowCallbackVO vo = updateSupStatus(vo1);
            PurchaseOrder purchaseOrder = new PurchaseOrder();
            purchaseOrder.setPurchaseOrderCode(vo1.getFormNo());
            PurchaseOrder order = purchaseOrderDao.purchaseOrderInfo(purchaseOrder);
            if(order == null){
                LOGGER.info("采购单为空");
                return WorkFlowReturn.FALSE;
            }else if(!order.getPurchaseOrderStatus().equals(Global.PURCHASE_ORDER_0)){
                // 采购单不是待审核状态
                return WorkFlowReturn.SUCCESS;
            }

            if (Objects.equals(vo.getApplyStatus(), ApplyStatus.APPROVAL_FAILED.getNumber()) || Objects.equals(vo.getApplyStatus(), ApplyStatus.REVOKED.getNumber())) {
                //审批失败或者撤销
                order.setPurchaseOrderStatus(Global.PURCHASE_ORDER_10);
                Integer count = purchaseOrderDao.update(order);
                LOGGER.info("影响条数:{}",count);
                // 添加审批不通过操作日志
                log(vo1.getFormNo(), vo1.getApprovalUserCode(), vo1.getApprovalUserName(),
                        PurchaseOrderLogEnum.CHECKOUT_NOT.getCode(), PurchaseOrderLogEnum.CHECKOUT_NOT.getName(), null);
            } else if (Objects.equals(vo.getApplyStatus(), ApplyStatus.APPROVAL.getNumber())) {
                // 审批中
                order.setPurchaseOrderStatus(Global.PURCHASE_ORDER_1);
                Integer count = purchaseOrderDao.update(order);
                LOGGER.info("影响条数:{}",count);
                // 添加审批中操作日志
                log(vo1.getFormNo(), vo1.getApprovalUserCode(), vo1.getApprovalUserName(),
                        PurchaseOrderLogEnum.CHECKOUT.getCode(), PurchaseOrderLogEnum.CHECKOUT.getName(), null);
            } else if (Objects.equals(vo.getApplyStatus(), ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
                //审批成功
                order.setPurchaseOrderStatus(Global.PURCHASE_ORDER_2);
                Integer count = purchaseOrderDao.update(order);
                // 添加审批通过操作日志
                log(vo1.getFormNo(), vo1.getApprovalUserCode(), vo1.getApprovalUserName(),
                        PurchaseOrderLogEnum.CHECKOUT_ADOPT.getCode(), PurchaseOrderLogEnum.CHECKOUT_ADOPT.getName(), null);
            }
            return WorkFlowReturn.SUCCESS;
        }catch  (Exception e) {
            LOGGER.error("审批回调异常:{}", e.getMessage());
            return WorkFlowReturn.SUCCESS;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void workFlow(String formNo, String userName, String directSupervisorCode) {
        WorkFlowVO workFlowVO = new WorkFlowVO();
        //在审批中看到的页面
        workFlowVO.setFormUrl(workFlowBaseUrl.applyPurchase + "?purchase_order_code=" + formNo + "&" + workFlowBaseUrl.authority);
        workFlowVO.setHost(workFlowBaseUrl.supplierHost);
        workFlowVO.setUpdateUrl(workFlowBaseUrl.callBackBaseUrl + WorkFlow.APPLY_PURCHASE.getNum());
        workFlowVO.setFormNo(formNo);
        workFlowVO.setTitle(userName + "创建采购单审批");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("auditPersonId", directSupervisorCode);
        workFlowVO.setVariables(jsonObject.toString());
        WorkFlowRespVO workFlowRespVO = callWorkFlowApi(workFlowVO, WorkFlow.APPLY_PURCHASE);
        //判断是否成功
        if (workFlowRespVO.getSuccess()) {
            LOGGER.info("创建采购单审批成功:{}",workFlowRespVO.toString());
        } else {
            throw new BizException(ResultCode.PURCHASE_ERROR);
        }
    }

    private void log(String purchaseOrderId, String createById, String createByName, Integer code, String name, String remark){
        OperationLog log = new OperationLog();
        log.setOperationId(purchaseOrderId);
        log.setCreateById(createById);
        log.setCreateByName(createByName);
        log.setOperationType(code);
        log.setOperationContent(name);
        log.setRemark(remark);
        Integer count = operationLogDao.insert(log);
        LOGGER.info("操作日志", count);
    }

}

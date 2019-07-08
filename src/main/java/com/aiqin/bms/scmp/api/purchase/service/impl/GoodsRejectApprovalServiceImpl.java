package com.aiqin.bms.scmp.api.purchase.service.impl;

import com.aiqin.bms.scmp.api.base.ApplyStatus;
import com.aiqin.bms.scmp.api.base.ResultCode;
import com.aiqin.bms.scmp.api.base.UrlConfig;
import com.aiqin.bms.scmp.api.base.WorkFlowBaseUrl;
import com.aiqin.bms.scmp.api.base.service.impl.BaseServiceImpl;
import com.aiqin.bms.scmp.api.common.BizException;
import com.aiqin.bms.scmp.api.common.WorkFlowReturn;
import com.aiqin.bms.scmp.api.constant.RejectRecordStatus;
import com.aiqin.bms.scmp.api.purchase.dao.RejectRecordDao;
import com.aiqin.bms.scmp.api.purchase.domain.RejectRecord;
import com.aiqin.bms.scmp.api.purchase.service.GoodsRejectApprovalService;
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

/**
 * <p>
 * ━━━━━━神兽出没━━━━━━
 * 　　┏┓　　　┏┓+ +
 * 　┏┛┻━━━┛┻┓ + +
 * 　┃　　　　　　　┃
 * 　┃　　　━　　　┃ ++ + + +
 * ████━████ ┃+
 * 　┃　　　　　　　┃ +
 * 　┃　　　┻　　　┃
 * 　┃　　　　　　　┃
 * 　┗━┓　　　┏━┛
 * 　　　┃　　　┃                  神兽保佑, 永无BUG!
 * 　　　┃　　　┃
 * 　　　┃　　　┃     Code is far away from bug with the animal protecting
 * 　　　┃　 　　┗━━━┓
 * 　　　┃ 　　　　　　　┣┓
 * 　　　┃ 　　　　　　　┏┛
 * 　　　┗┓┓┏━┳┓┏┛
 * 　　　　┃┫┫　┃┫┫
 * 　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 * <p>
 * <p>
 * 思维方式*热情*能力
 */
@Service
@WorkFlowAnnotation(WorkFlow.APPLY_REFUND)
public class GoodsRejectApprovalServiceImpl extends BaseServiceImpl implements GoodsRejectApprovalService, WorkFlowHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsRejectApprovalServiceImpl.class);

    @Resource
    private UrlConfig urlConfig;
    @Resource
    private WorkFlowBaseUrl workFlowBaseUrl;
    @Resource
    private RejectRecordDao rejectRecordDao;

    /**
     * 审核回调接口
     *
     * @param vo1
     * @return
     */
    @Override
    public String workFlowCallback(WorkFlowCallbackVO vo1) {
        try {
            WorkFlowCallbackVO vo = updateSupStatus(vo1);
            //审批驳回
            RejectRecord rejectRecord = new RejectRecord();
            rejectRecord.setRejectRecordCode(vo.getFormNo());
            RejectRecord record = rejectRecordDao.selectByRejectCode(vo.getFormNo());
            if (record == null) {
                //未查询到退供单信息
                return WorkFlowReturn.FALSE;
            } else if (record.getRejectStatus().equals(RejectRecordStatus.REJECT_STATUS_CANCEL)) {
                //退供单是取消状态,不进行操作
                return WorkFlowReturn.SUCCESS;
            }
            if (Objects.equals(vo.getApplyStatus(), ApplyStatus.APPROVAL.getNumber())) {
                //审批中状态
                rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_AUDITTING);
                Integer count = rejectRecordDao.updateStatus(rejectRecord);
            } else if (Objects.equals(vo.getApplyStatus(), ApplyStatus.APPROVAL_FAILED.getNumber())||Objects.equals(vo.getApplyStatus(), ApplyStatus.REVOKED.getNumber())) {
                //审批失败或者撤销
                rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_NO);
                Integer count = rejectRecordDao.updateStatus(rejectRecord);
            }else if (Objects.equals(vo.getApplyStatus(), ApplyStatus.APPROVAL_SUCCESS.getNumber())) {
                //审批通过 状态为待供应商确认
                rejectRecord.setRejectStatus(RejectRecordStatus.REJECT_STATUS_DEFINE);
                Integer count = rejectRecordDao.updateStatus(rejectRecord);
            }
            return WorkFlowReturn.SUCCESS;
        } catch (Exception e) {
            LOGGER.error("审批回调异常:{}", e.getMessage());
            return WorkFlowReturn.SUCCESS;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void workFlow(String formNo, String userName, String directSupervisorCode) {
        WorkFlowVO workFlowVO = new WorkFlowVO();
        //在审批中看到的页面
        workFlowVO.setFormUrl(workFlowBaseUrl.applyRefund + "?code=" + formNo + "&" + workFlowBaseUrl.authority);
        workFlowVO.setHost(workFlowBaseUrl.supplierHost);
        workFlowVO.setUpdateUrl(workFlowBaseUrl.callBackBaseUrl + WorkFlow.APPLY_REFUND.getNum());
        workFlowVO.setFormNo(formNo);
        workFlowVO.setTitle(userName + "创建退供申请单审批");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("auditPersonId", directSupervisorCode);
        workFlowVO.setVariables(jsonObject.toString());
        WorkFlowRespVO workFlowRespVO = callWorkFlowApi(workFlowVO, WorkFlow.APPLY_REFUND);
        //判断是否成功
        if (workFlowRespVO.getSuccess()) {
        } else {
            throw new BizException(ResultCode.REJECT_RECORD_ERROR);
        }
    }

}

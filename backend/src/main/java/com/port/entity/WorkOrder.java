package com.port.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("work_order")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Integer orderType;
    private Long facilityId;
    private String facilityCode;
    private String facilityName;
    private Integer status;
    private String faultDescription;
    private String faultImages;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime reportTime;
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime assignTime;
    private LocalDateTime acceptTime;
    private String processDescription;
    private String processImages;
    private LocalDateTime submitTime;
    private LocalDateTime verifyTime;
    private Long verifierId;
    private String verifierName;
    private String verifyRemark;
    private LocalDateTime archiveTime;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private String statusName;

    // 状态常量
    public static final int STATUS_PENDING = 0;    // 待派发
    public static final int STATUS_ASSIGNED = 1;   // 已派发
    public static final int STATUS_ACCEPTED = 2;   // 已接单
    public static final int STATUS_SUBMITTED = 3;  // 待验收
    public static final int STATUS_COMPLETED = 4;  // 已完成
    public static final int STATUS_ARCHIVED = 5;   // 已归档

    public static String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case STATUS_PENDING: return "待派发";
            case STATUS_ASSIGNED: return "已派发";
            case STATUS_ACCEPTED: return "已接单";
            case STATUS_SUBMITTED: return "待验收";
            case STATUS_COMPLETED: return "已完成";
            case STATUS_ARCHIVED: return "已归档";
            default: return "未知";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Integer getOrderType() { return orderType; }
    public void setOrderType(Integer orderType) { this.orderType = orderType; }

    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }

    public String getFacilityCode() { return facilityCode; }
    public void setFacilityCode(String facilityCode) { this.facilityCode = facilityCode; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getFaultDescription() { return faultDescription; }
    public void setFaultDescription(String faultDescription) { this.faultDescription = faultDescription; }

    public String getFaultImages() { return faultImages; }
    public void setFaultImages(String faultImages) { this.faultImages = faultImages; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public LocalDateTime getAssignTime() { return assignTime; }
    public void setAssignTime(LocalDateTime assignTime) { this.assignTime = assignTime; }

    public LocalDateTime getAcceptTime() { return acceptTime; }
    public void setAcceptTime(LocalDateTime acceptTime) { this.acceptTime = acceptTime; }

    public String getProcessDescription() { return processDescription; }
    public void setProcessDescription(String processDescription) { this.processDescription = processDescription; }

    public String getProcessImages() { return processImages; }
    public void setProcessImages(String processImages) { this.processImages = processImages; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public LocalDateTime getVerifyTime() { return verifyTime; }
    public void setVerifyTime(LocalDateTime verifyTime) { this.verifyTime = verifyTime; }

    public Long getVerifierId() { return verifierId; }
    public void setVerifierId(Long verifierId) { this.verifierId = verifierId; }

    public String getVerifierName() { return verifierName; }
    public void setVerifierName(String verifierName) { this.verifierName = verifierName; }

    public String getVerifyRemark() { return verifyRemark; }
    public void setVerifyRemark(String verifyRemark) { this.verifyRemark = verifyRemark; }

    public LocalDateTime getArchiveTime() { return archiveTime; }
    public void setArchiveTime(LocalDateTime archiveTime) { this.archiveTime = archiveTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getStatusName() {
        return statusName != null ? statusName : getStatusName(this.status);
    }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}

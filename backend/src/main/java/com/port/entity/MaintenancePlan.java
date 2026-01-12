package com.port.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("maintenance_plan")
public class MaintenancePlan {
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planName;
    private Integer planType;
    private Long facilityId;
    private String facilityCode;
    private String facilityName;
    private Integer cycleDays;
    private String description;
    private Integer status;
    private LocalDate lastGenerateDate;
    private LocalDate nextGenerateDate;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String statusName;

    public String getStatusName() {
        if (status == null) return "";
        return status == STATUS_ENABLED ? "启用" : "停用";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public Integer getPlanType() { return planType; }
    public void setPlanType(Integer planType) { this.planType = planType; }

    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }

    public String getFacilityCode() { return facilityCode; }
    public void setFacilityCode(String facilityCode) { this.facilityCode = facilityCode; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public Integer getCycleDays() { return cycleDays; }
    public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDate getLastGenerateDate() { return lastGenerateDate; }
    public void setLastGenerateDate(LocalDate lastGenerateDate) { this.lastGenerateDate = lastGenerateDate; }

    public LocalDate getNextGenerateDate() { return nextGenerateDate; }
    public void setNextGenerateDate(LocalDate nextGenerateDate) { this.nextGenerateDate = nextGenerateDate; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

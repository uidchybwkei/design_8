package com.port.service;

import com.port.entity.Facility;
import com.port.entity.MaintenanceGenerateLog;
import com.port.entity.MaintenancePlan;
import com.port.entity.WorkOrder;
import com.port.exception.BusinessException;
import com.port.mapper.FacilityMapper;
import com.port.mapper.MaintenanceGenerateLogMapper;
import com.port.mapper.MaintenancePlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenancePlanService {

    @Autowired
    private MaintenancePlanMapper planMapper;

    @Autowired
    private MaintenanceGenerateLogMapper generateLogMapper;

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private WorkOrderService workOrderService;

    public List<MaintenancePlan> getAll() {
        return planMapper.findAll();
    }

    public MaintenancePlan getById(Long id) {
        MaintenancePlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException("保养计划不存在");
        }
        return plan;
    }

    @Transactional
    public MaintenancePlan create(MaintenancePlan plan) {
        Facility facility = facilityMapper.selectById(plan.getFacilityId());
        if (facility == null) {
            throw new BusinessException("设施不存在");
        }

        plan.setFacilityCode(facility.getFacilityCode());
        plan.setFacilityName(facility.getFacilityName());
        plan.setPlanType(1);
        plan.setStatus(MaintenancePlan.STATUS_ENABLED);
        plan.setNextGenerateDate(LocalDate.now().plusDays(plan.getCycleDays()));
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());

        planMapper.insert(plan);
        return plan;
    }

    @Transactional
    public MaintenancePlan update(Long id, MaintenancePlan planData) {
        MaintenancePlan plan = getById(id);

        if (planData.getPlanName() != null) {
            plan.setPlanName(planData.getPlanName());
        }
        if (planData.getCycleDays() != null) {
            plan.setCycleDays(planData.getCycleDays());
            if (plan.getLastGenerateDate() != null) {
                plan.setNextGenerateDate(plan.getLastGenerateDate().plusDays(planData.getCycleDays()));
            } else {
                plan.setNextGenerateDate(LocalDate.now().plusDays(planData.getCycleDays()));
            }
        }
        if (planData.getDescription() != null) {
            plan.setDescription(planData.getDescription());
        }
        if (planData.getFacilityId() != null && !planData.getFacilityId().equals(plan.getFacilityId())) {
            Facility facility = facilityMapper.selectById(planData.getFacilityId());
            if (facility == null) {
                throw new BusinessException("设施不存在");
            }
            plan.setFacilityId(facility.getId());
            plan.setFacilityCode(facility.getFacilityCode());
            plan.setFacilityName(facility.getFacilityName());
        }

        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
        return plan;
    }

    @Transactional
    public void enable(Long id) {
        MaintenancePlan plan = getById(id);
        if (plan.getStatus() == MaintenancePlan.STATUS_ENABLED) {
            throw new BusinessException("计划已是启用状态");
        }
        plan.setStatus(MaintenancePlan.STATUS_ENABLED);
        if (plan.getNextGenerateDate() == null || plan.getNextGenerateDate().isBefore(LocalDate.now())) {
            plan.setNextGenerateDate(LocalDate.now().plusDays(plan.getCycleDays()));
        }
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
    }

    @Transactional
    public void disable(Long id) {
        MaintenancePlan plan = getById(id);
        if (plan.getStatus() == MaintenancePlan.STATUS_DISABLED) {
            throw new BusinessException("计划已是停用状态");
        }
        plan.setStatus(MaintenancePlan.STATUS_DISABLED);
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        planMapper.deleteById(id);
    }

    public List<MaintenancePlan> getDuePlans() {
        return planMapper.findDuePlans(LocalDate.now());
    }

    @Transactional
    public WorkOrder generateWorkOrder(MaintenancePlan plan) {
        LocalDate today = LocalDate.now();

        MaintenanceGenerateLog existLog = generateLogMapper.findByPlanAndDate(plan.getId(), today);
        if (existLog != null) {
            return null;
        }

        Facility facility = facilityMapper.selectById(plan.getFacilityId());
        if (facility == null || facility.getStatus() == 0) {
            return null;
        }

        WorkOrder order = workOrderService.createMaintenanceOrder(
                plan.getFacilityId(),
                plan.getDescription(),
                plan.getId()
        );

        MaintenanceGenerateLog log = new MaintenanceGenerateLog();
        log.setPlanId(plan.getId());
        log.setGenerateDate(today);
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setCreateTime(LocalDateTime.now());
        generateLogMapper.insert(log);

        plan.setLastGenerateDate(today);
        plan.setNextGenerateDate(today.plusDays(plan.getCycleDays()));
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(plan);

        return order;
    }

    @Transactional
    public int executeScheduledGeneration() {
        List<MaintenancePlan> duePlans = getDuePlans();
        int count = 0;
        for (MaintenancePlan plan : duePlans) {
            WorkOrder order = generateWorkOrder(plan);
            if (order != null) {
                count++;
            }
        }
        return count;
    }
}

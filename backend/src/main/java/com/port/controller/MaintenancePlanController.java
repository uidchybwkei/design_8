package com.port.controller;

import com.port.common.Result;
import com.port.entity.MaintenancePlan;
import com.port.entity.WorkOrder;
import com.port.service.MaintenancePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance-plan")
public class MaintenancePlanController {

    @Autowired
    private MaintenancePlanService planService;

    @GetMapping("/list")
    public Result<List<MaintenancePlan>> list() {
        List<MaintenancePlan> plans = planService.getAll();
        return Result.success(plans);
    }

    @GetMapping("/{id}")
    public Result<MaintenancePlan> getById(@PathVariable Long id) {
        MaintenancePlan plan = planService.getById(id);
        return Result.success(plan);
    }

    @PostMapping
    public Result<MaintenancePlan> create(@RequestBody MaintenancePlan plan) {
        MaintenancePlan created = planService.create(plan);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<MaintenancePlan> update(@PathVariable Long id, @RequestBody MaintenancePlan plan) {
        MaintenancePlan updated = planService.update(id, plan);
        return Result.success(updated);
    }

    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        planService.enable(id);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        planService.disable(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return Result.success();
    }

    @PostMapping("/trigger-generate")
    public Result<Integer> triggerGenerate() {
        int count = planService.executeScheduledGeneration();
        return Result.success(count);
    }

    @PostMapping("/{id}/generate-now")
    public Result<WorkOrder> generateNow(@PathVariable Long id) {
        MaintenancePlan plan = planService.getById(id);
        WorkOrder order = planService.generateWorkOrder(plan);
        return Result.success(order);
    }
}

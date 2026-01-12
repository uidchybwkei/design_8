package com.port.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.port.common.PageResult;
import com.port.common.Result;
import com.port.entity.User;
import com.port.entity.WorkOrder;
import com.port.entity.WorkOrderLog;
import com.port.service.UserService;
import com.port.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workorder")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private UserService userService;

    @GetMapping("/page")
    public Result<PageResult<WorkOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) String keyword) {
        IPage<WorkOrder> page = workOrderService.pageList(pageNum, pageSize, status, facilityId, assigneeId, reporterId, keyword);
        PageResult<WorkOrder> result = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<WorkOrder> getById(@PathVariable Long id) {
        WorkOrder order = workOrderService.getById(id);
        return Result.success(order);
    }

    @GetMapping("/{id}/logs")
    public Result<List<WorkOrderLog>> getLogs(@PathVariable Long id) {
        List<WorkOrderLog> logs = workOrderService.getOrderLogs(id);
        return Result.success(logs);
    }

    @GetMapping("/my")
    public Result<List<WorkOrder>> getMyOrders(@RequestAttribute("userId") Long userId) {
        List<WorkOrder> orders = workOrderService.getByAssignee(userId, null);
        return Result.success(orders);
    }

    @GetMapping("/facility/{facilityId}/history")
    public Result<List<WorkOrder>> getFacilityHistory(@PathVariable Long facilityId) {
        List<WorkOrder> history = workOrderService.getFacilityHistory(facilityId);
        return Result.success(history);
    }

    @PostMapping("/report")
    public Result<WorkOrder> reportFault(@RequestBody Map<String, Object> params,
                                         @RequestAttribute("userId") Long userId) {
        Long facilityId = Long.valueOf(params.get("facilityId").toString());
        String faultDescription = (String) params.get("faultDescription");
        String faultImages = (String) params.get("faultImages");

        User user = userService.getUserInfo(userId);
        WorkOrder order = workOrderService.createFaultReport(facilityId, faultDescription, faultImages, userId, user.getRealName());
        return Result.success(order);
    }

    @PostMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id,
                               @RequestBody Map<String, Object> params,
                               @RequestAttribute("userId") Long operatorId) {
        Long assigneeId = Long.valueOf(params.get("assigneeId").toString());
        User operator = userService.getUserInfo(operatorId);
        workOrderService.assign(id, assigneeId, operatorId, operator.getRealName());
        return Result.success();
    }

    @PostMapping("/{id}/reassign")
    public Result<Void> reassign(@PathVariable Long id,
                                 @RequestBody Map<String, Object> params,
                                 @RequestAttribute("userId") Long operatorId) {
        Long newAssigneeId = Long.valueOf(params.get("assigneeId").toString());
        User operator = userService.getUserInfo(operatorId);
        workOrderService.reassign(id, newAssigneeId, operatorId, operator.getRealName());
        return Result.success();
    }

    @PostMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id,
                               @RequestAttribute("userId") Long userId) {
        User user = userService.getUserInfo(userId);
        workOrderService.accept(id, userId, user.getRealName());
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id,
                               @RequestBody Map<String, Object> params,
                               @RequestAttribute("userId") Long userId) {
        String processDescription = (String) params.get("processDescription");
        String processImages = (String) params.get("processImages");

        User user = userService.getUserInfo(userId);
        workOrderService.submit(id, processDescription, processImages, userId, user.getRealName());
        return Result.success();
    }

    @PostMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, Object> params,
                               @RequestAttribute("userId") Long verifierId) {
        String verifyRemark = params != null ? (String) params.get("verifyRemark") : null;
        User verifier = userService.getUserInfo(verifierId);
        workOrderService.verify(id, verifyRemark, verifierId, verifier.getRealName());
        return Result.success();
    }

    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id,
                                @RequestAttribute("userId") Long operatorId) {
        User operator = userService.getUserInfo(operatorId);
        workOrderService.archive(id, operatorId, operator.getRealName());
        return Result.success();
    }

    @GetMapping("/assignable-users")
    public Result<List<User>> getAssignableUsers() {
        List<User> users = workOrderService.getAssignableUsers();
        return Result.success(users);
    }
}

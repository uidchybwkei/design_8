package com.port.controller;

import com.port.common.Result;
import com.port.entity.User;
import com.port.service.StatsService;
import com.port.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计分析接口（只读）
 * 
 * 所有接口均为只读查询，不会修改任何业务数据。
 * 统计口径详见 StatsService 类注释。
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private UserService userService;

    /**
     * 获取工单统计概览
     * 包含：总数、完成数、完成率、待处理数等
     */
    @GetMapping("/workorder/overview")
    public Result<Map<String, Object>> getWorkOrderOverview(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(statsService.getWorkOrderStats(startTime, endTime));
    }

    /**
     * 获取平均处理耗时统计
     * 包含：总体平均、维修平均、保养平均
     */
    @GetMapping("/workorder/duration")
    public Result<Map<String, Object>> getAverageDuration(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(statsService.getAverageDuration(startTime, endTime));
    }

    /**
     * 获取故障高发设施Top N
     * 统计口径：按设施分组统计维修工单数量
     */
    @GetMapping("/facility/top-fault")
    public Result<List<Map<String, Object>>> getTopFaultFacilities(
            @RequestParam(defaultValue = "5") int topN,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(statsService.getTopFaultFacilities(topN, startTime, endTime));
    }

    /**
     * 获取当前用户的个人工作统计
     * 用于微信端"我的"页面展示
     */
    @GetMapping("/user/my")
    public Result<Map<String, Object>> getMyStats(@RequestHeader("Authorization") String token) {
        User user = userService.getCurrentUser(token.replace("Bearer ", ""));
        return Result.success(statsService.getUserStats(user.getId()));
    }
}

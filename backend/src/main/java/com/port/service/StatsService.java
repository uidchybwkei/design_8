package com.port.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.port.entity.WorkOrder;
import com.port.mapper.WorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务（只读）
 * 
 * 统计口径说明：
 * 1. 工单完成率 = 已完成+已归档工单数 / 总工单数 × 100%
 * 2. 平均处理耗时 = Σ(提交时间 - 接单时间) / 已提交工单数（单位：小时）
 * 3. 故障高发设施 = 按设施分组统计维修工单(orderType=1)数量，取Top N
 * 
 * 数据范围：仅统计未删除的工单
 * 时间筛选：基于工单创建时间(createTime)
 */
@Service
public class StatsService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    /**
     * 获取工单统计概览
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     */
    public Map<String, Object> getWorkOrderStats(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getDeleted, 0);
        if (startTime != null) {
            wrapper.ge(WorkOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(WorkOrder::getCreateTime, endTime);
        }

        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        Map<String, Object> stats = new LinkedHashMap<>();

        // 总工单数
        int totalCount = orders.size();
        stats.put("totalCount", totalCount);

        // 维修工单数 (orderType=1)
        long repairCount = orders.stream().filter(o -> o.getOrderType() == 1).count();
        stats.put("repairCount", repairCount);

        // 保养工单数 (orderType=2)
        long maintenanceCount = orders.stream().filter(o -> o.getOrderType() == 2).count();
        stats.put("maintenanceCount", maintenanceCount);

        // 已完成+已归档数 (status=4 or status=5)
        long completedCount = orders.stream()
                .filter(o -> o.getStatus() == WorkOrder.STATUS_COMPLETED || o.getStatus() == WorkOrder.STATUS_ARCHIVED)
                .count();
        stats.put("completedCount", completedCount);

        // 完成率
        double completionRate = totalCount > 0 ? (completedCount * 100.0 / totalCount) : 0;
        stats.put("completionRate", Math.round(completionRate * 10) / 10.0);

        // 待处理数（待派发+已派发+已接单）
        long pendingCount = orders.stream()
                .filter(o -> o.getStatus() <= WorkOrder.STATUS_ACCEPTED)
                .count();
        stats.put("pendingCount", pendingCount);

        // 待验收数
        long waitingVerifyCount = orders.stream()
                .filter(o -> o.getStatus() == WorkOrder.STATUS_SUBMITTED)
                .count();
        stats.put("waitingVerifyCount", waitingVerifyCount);

        return stats;
    }

    /**
     * 获取平均处理耗时统计
     * 统计口径：从接单时间(acceptTime)到提交时间(submitTime)的时长
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     */
    public Map<String, Object> getAverageDuration(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getDeleted, 0);
        wrapper.isNotNull(WorkOrder::getAcceptTime);
        wrapper.isNotNull(WorkOrder::getSubmitTime);
        if (startTime != null) {
            wrapper.ge(WorkOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(WorkOrder::getCreateTime, endTime);
        }

        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        Map<String, Object> result = new LinkedHashMap<>();

        // 全部工单平均耗时
        double avgHours = calculateAverageHours(orders);
        result.put("avgDurationHours", Math.round(avgHours * 10) / 10.0);
        result.put("avgDurationDisplay", formatDuration(avgHours));

        // 维修工单平均耗时
        List<WorkOrder> repairOrders = orders.stream().filter(o -> o.getOrderType() == 1).collect(Collectors.toList());
        double repairAvgHours = calculateAverageHours(repairOrders);
        result.put("repairAvgHours", Math.round(repairAvgHours * 10) / 10.0);
        result.put("repairAvgDisplay", formatDuration(repairAvgHours));
        result.put("repairSampleCount", repairOrders.size());

        // 保养工单平均耗时
        List<WorkOrder> maintenanceOrders = orders.stream().filter(o -> o.getOrderType() == 2).collect(Collectors.toList());
        double maintenanceAvgHours = calculateAverageHours(maintenanceOrders);
        result.put("maintenanceAvgHours", Math.round(maintenanceAvgHours * 10) / 10.0);
        result.put("maintenanceAvgDisplay", formatDuration(maintenanceAvgHours));
        result.put("maintenanceSampleCount", maintenanceOrders.size());

        result.put("totalSampleCount", orders.size());

        return result;
    }

    private double calculateAverageHours(List<WorkOrder> orders) {
        if (orders.isEmpty()) return 0;
        double totalMinutes = 0;
        for (WorkOrder order : orders) {
            Duration duration = Duration.between(order.getAcceptTime(), order.getSubmitTime());
            totalMinutes += duration.toMinutes();
        }
        return totalMinutes / orders.size() / 60.0;
    }

    private String formatDuration(double hours) {
        if (hours < 1) {
            return Math.round(hours * 60) + "分钟";
        } else if (hours < 24) {
            return Math.round(hours * 10) / 10.0 + "小时";
        } else {
            return Math.round(hours / 24 * 10) / 10.0 + "天";
        }
    }

    /**
     * 获取故障高发设施Top N
     * 统计口径：按设施ID分组统计维修工单(orderType=1)数量
     * @param topN 返回条数
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     */
    public List<Map<String, Object>> getTopFaultFacilities(int topN, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getDeleted, 0);
        wrapper.eq(WorkOrder::getOrderType, 1); // 仅维修工单
        if (startTime != null) {
            wrapper.ge(WorkOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(WorkOrder::getCreateTime, endTime);
        }

        List<WorkOrder> repairOrders = workOrderMapper.selectList(wrapper);

        // 按设施分组统计
        Map<Long, List<WorkOrder>> groupByFacility = repairOrders.stream()
                .collect(Collectors.groupingBy(WorkOrder::getFacilityId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<WorkOrder>> entry : groupByFacility.entrySet()) {
            List<WorkOrder> facilityOrders = entry.getValue();
            if (!facilityOrders.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                WorkOrder sample = facilityOrders.get(0);
                item.put("facilityId", sample.getFacilityId());
                item.put("facilityCode", sample.getFacilityCode());
                item.put("facilityName", sample.getFacilityName());
                item.put("repairCount", facilityOrders.size());
                result.add(item);
            }
        }

        // 按维修次数降序排列，取Top N
        return result.stream()
                .sorted((a, b) -> ((Integer) b.get("repairCount")).compareTo((Integer) a.get("repairCount")))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户个人工作统计
     * @param userId 用户ID
     */
    public Map<String, Object> getUserStats(Long userId) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getDeleted, 0);
        wrapper.eq(WorkOrder::getAssigneeId, userId);

        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        Map<String, Object> stats = new LinkedHashMap<>();

        // 我的总工单数
        stats.put("totalCount", orders.size());

        // 我的已完成数
        long completedCount = orders.stream()
                .filter(o -> o.getStatus() == WorkOrder.STATUS_COMPLETED || o.getStatus() == WorkOrder.STATUS_ARCHIVED)
                .count();
        stats.put("completedCount", completedCount);

        // 我的待处理数
        long pendingCount = orders.stream()
                .filter(o -> o.getStatus() >= WorkOrder.STATUS_ASSIGNED && o.getStatus() <= WorkOrder.STATUS_ACCEPTED)
                .count();
        stats.put("pendingCount", pendingCount);

        // 我的平均处理耗时
        List<WorkOrder> submittedOrders = orders.stream()
                .filter(o -> o.getAcceptTime() != null && o.getSubmitTime() != null)
                .collect(Collectors.toList());
        double avgHours = calculateAverageHours(submittedOrders);
        stats.put("avgDurationHours", Math.round(avgHours * 10) / 10.0);
        stats.put("avgDurationDisplay", formatDuration(avgHours));

        return stats;
    }
}

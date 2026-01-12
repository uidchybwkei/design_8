package com.port.scheduler;

import com.port.service.MaintenancePlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduler.class);

    @Autowired
    private MaintenancePlanService planService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void generateMaintenanceOrders() {
        log.info("开始执行保养工单生成定时任务...");
        try {
            int count = planService.executeScheduledGeneration();
            log.info("保养工单生成定时任务完成，生成工单数: {}", count);
        } catch (Exception e) {
            log.error("保养工单生成定时任务异常", e);
        }
    }
}

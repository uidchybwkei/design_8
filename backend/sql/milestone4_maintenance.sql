-- ================================================
-- 里程碑4：保养计划与保养工单
-- ================================================

-- 1. 保养计划表
CREATE TABLE IF NOT EXISTS `maintenance_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `plan_name` VARCHAR(100) NOT NULL COMMENT '计划名称',
    `plan_type` TINYINT DEFAULT 1 COMMENT '计划类型: 1-周期保养',
    `facility_id` BIGINT NOT NULL COMMENT '关联设施ID',
    `facility_code` VARCHAR(50) DEFAULT NULL COMMENT '设施编码（冗余）',
    `facility_name` VARCHAR(100) DEFAULT NULL COMMENT '设施名称（冗余）',
    `cycle_days` INT NOT NULL COMMENT '周期天数（每N天执行一次）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '保养内容描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `last_generate_date` DATE DEFAULT NULL COMMENT '上次生成工单日期',
    `next_generate_date` DATE DEFAULT NULL COMMENT '下次生成工单日期',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_facility` (`facility_id`),
    KEY `idx_status` (`status`),
    KEY `idx_next_date` (`next_generate_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保养计划表';

-- 2. 保养工单生成记录表（幂等控制）
CREATE TABLE IF NOT EXISTS `maintenance_generate_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `plan_id` BIGINT NOT NULL COMMENT '保养计划ID',
    `generate_date` DATE NOT NULL COMMENT '生成日期（周期标识）',
    `order_id` BIGINT DEFAULT NULL COMMENT '生成的工单ID',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '生成的工单编号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plan_date` (`plan_id`, `generate_date`) COMMENT '幂等控制：同一计划同一日期只能生成一次',
    KEY `idx_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保养工单生成记录表（幂等控制）';

-- ================================================
-- 说明
-- ================================================
-- 1. 保养工单复用 work_order 表，通过 order_type = 2 区分
--    - order_type = 1: 维修工单
--    - order_type = 2: 保养工单
-- 
-- 2. 幂等控制策略：
--    - maintenance_generate_log 表通过 (plan_id, generate_date) 唯一索引
--    - 定时任务生成工单前先检查该记录是否存在
--    - 若存在则跳过，保证同一计划同一周期只生成一次
--
-- 3. 定时任务逻辑：
--    - 每日扫描 status=1 且 next_generate_date <= 当前日期 的计划
--    - 检查幂等记录，不存在则生成保养工单
--    - 更新 last_generate_date 和 next_generate_date
--
-- 4. 保养工单状态流转：
--    - 复用维修工单状态机（0-5）
--    - 待派发 → 已派发 → 已接单 → 待验收 → 已完成 → 已归档

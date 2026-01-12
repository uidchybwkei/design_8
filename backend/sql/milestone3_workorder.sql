-- 里程碑3：维修工单表结构

USE design_8_new;

-- 维修工单主表
CREATE TABLE IF NOT EXISTS `work_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号（唯一）',
    `order_type` TINYINT DEFAULT 1 COMMENT '工单类型: 1-维修, 2-保养（本里程碑仅用1）',
    `facility_id` BIGINT NOT NULL COMMENT '关联设施ID',
    `facility_code` VARCHAR(50) NOT NULL COMMENT '设施编码（冗余）',
    `facility_name` VARCHAR(100) NOT NULL COMMENT '设施名称（冗余）',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待派发, 1-已派发, 2-已接单, 3-待验收, 4-已完成, 5-已归档',
    `fault_description` VARCHAR(500) DEFAULT NULL COMMENT '故障描述',
    `fault_images` VARCHAR(1000) DEFAULT NULL COMMENT '故障图片（JSON数组）',
    `reporter_id` BIGINT DEFAULT NULL COMMENT '上报人ID',
    `reporter_name` VARCHAR(50) DEFAULT NULL COMMENT '上报人姓名',
    `report_time` DATETIME DEFAULT NULL COMMENT '上报时间',
    `assignee_id` BIGINT DEFAULT NULL COMMENT '执行人ID',
    `assignee_name` VARCHAR(50) DEFAULT NULL COMMENT '执行人姓名',
    `assign_time` DATETIME DEFAULT NULL COMMENT '派单时间',
    `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
    `process_description` VARCHAR(1000) DEFAULT NULL COMMENT '处理说明',
    `process_images` VARCHAR(2000) DEFAULT NULL COMMENT '处理图片（JSON数组，提交时必须≥1张）',
    `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间（服务器时间戳）',
    `verify_time` DATETIME DEFAULT NULL COMMENT '验收时间',
    `verifier_id` BIGINT DEFAULT NULL COMMENT '验收人ID',
    `verifier_name` VARCHAR(50) DEFAULT NULL COMMENT '验收人姓名',
    `verify_remark` VARCHAR(500) DEFAULT NULL COMMENT '验收备注',
    `archive_time` DATETIME DEFAULT NULL COMMENT '归档时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_facility_id` (`facility_id`),
    KEY `idx_status` (`status`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_reporter_id` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单表';

-- 工单操作日志表（流程节点记录，用于全流程追溯）
CREATE TABLE IF NOT EXISTS `work_order_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE/ASSIGN/ACCEPT/SUBMIT/VERIFY/ARCHIVE/REJECT',
    `from_status` TINYINT DEFAULT NULL COMMENT '原状态',
    `to_status` TINYINT DEFAULT NULL COMMENT '目标状态',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '操作备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单操作日志表';

-- 文件附件表（通用）
CREATE TABLE IF NOT EXISTS `file_attachment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    `file_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型（MIME）',
    `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型: FAULT_IMAGE/PROCESS_IMAGE',
    `biz_id` BIGINT DEFAULT NULL COMMENT '业务ID',
    `uploader_id` BIGINT DEFAULT NULL COMMENT '上传人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件附件表';

-- 状态说明：
-- 0: 待派发 (PENDING) - 已上报，等待后台派单
-- 1: 已派发 (ASSIGNED) - 后台已指派执行人，等待接单
-- 2: 已接单 (ACCEPTED) - 执行人已接单，处理中
-- 3: 待验收 (SUBMITTED) - 执行人已提交处理结果，等待验收
-- 4: 已完成 (COMPLETED) - 验收通过
-- 5: 已归档 (ARCHIVED) - 已归档，不可修改

-- 状态流转规则：
-- 待派发(0) -> 已派发(1)：后台派单
-- 已派发(1) -> 已接单(2)：执行人接单
-- 已接单(2) -> 待验收(3)：执行人提交（必须校验图片、时间戳）
-- 待验收(3) -> 已完成(4)：后台验收通过
-- 待验收(3) -> 已接单(2)：后台驳回（可选，本里程碑暂不实现）
-- 已完成(4) -> 已归档(5)：后台归档


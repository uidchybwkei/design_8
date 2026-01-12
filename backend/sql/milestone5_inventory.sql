-- ================================================
-- 里程碑5：库存备件最小可用 + 工单消耗关联
-- ================================================

USE design_8_new;

-- 1. 备件主数据表
CREATE TABLE IF NOT EXISTS `inventory_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_code` VARCHAR(50) NOT NULL COMMENT '备件编码（唯一）',
    `item_name` VARCHAR(100) NOT NULL COMMENT '备件名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '型号/规格',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位（个、件、米、kg等）',
    `current_stock` INT DEFAULT 0 COMMENT '当前库存数量',
    `warning_threshold` INT DEFAULT 10 COMMENT '预警阈值（库存≤此值触发预警）',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_code` (`item_code`),
    KEY `idx_status` (`status`),
    KEY `idx_warning` (`current_stock`, `warning_threshold`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备件主数据表';

-- 2. 库存流水记录表（入库/出库）
CREATE TABLE IF NOT EXISTS `inventory_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_id` BIGINT NOT NULL COMMENT '备件ID',
    `item_code` VARCHAR(50) NOT NULL COMMENT '备件编码（冗余）',
    `item_name` VARCHAR(100) NOT NULL COMMENT '备件名称（冗余）',
    `record_type` TINYINT NOT NULL COMMENT '记录类型: 1-入库, 2-出库',
    `quantity` INT NOT NULL COMMENT '数量（正数）',
    `before_stock` INT NOT NULL COMMENT '操作前库存',
    `after_stock` INT NOT NULL COMMENT '操作后库存',
    `reason` VARCHAR(200) DEFAULT NULL COMMENT '操作原因/备注',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联工单ID（出库时可关联）',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '关联工单编号（冗余）',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_type` (`record_type`),
    KEY `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水记录表';

-- 3. 工单备件消耗关联表
CREATE TABLE IF NOT EXISTS `work_order_consumption` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号（冗余）',
    `item_id` BIGINT NOT NULL COMMENT '备件ID',
    `item_code` VARCHAR(50) NOT NULL COMMENT '备件编码（冗余）',
    `item_name` VARCHAR(100) NOT NULL COMMENT '备件名称（冗余）',
    `quantity` INT NOT NULL COMMENT '消耗数量',
    `record_id` BIGINT DEFAULT NULL COMMENT '关联的库存流水ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_item` (`item_id`),
    UNIQUE KEY `uk_order_item` (`order_id`, `item_id`) COMMENT '同一工单同一备件只能有一条消耗记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单备件消耗关联表';

-- ================================================
-- 初始化测试数据（可选）
-- ================================================
INSERT INTO `inventory_item` (`item_code`, `item_name`, `specification`, `unit`, `current_stock`, `warning_threshold`, `status`) VALUES
('SP001', '六角螺栓', 'M12×50', '个', 100, 20, 1),
('SP002', '防锈漆', '1L装', '桶', 15, 5, 1),
('SP003', '密封圈', 'DN100', '个', 50, 10, 1),
('SP004', '润滑油', '5L装', '桶', 8, 5, 1),
('SP005', '钢丝绳', '直径12mm', '米', 200, 50, 1);

-- 里程碑2：设施资产表结构

USE design_8_new;

-- 设施分类表
CREATE TABLE IF NOT EXISTS `facility_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(30) NOT NULL COMMENT '分类编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-停用',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设施分类表';

-- 设施主表
CREATE TABLE IF NOT EXISTS `facility` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设施ID',
    `facility_code` VARCHAR(50) NOT NULL COMMENT '设施编码（唯一，用于二维码）',
    `facility_name` VARCHAR(100) NOT NULL COMMENT '设施名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '所属分类ID',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '安装位置描述',
    `specification` VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
    `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '生产厂家',
    `install_date` DATE DEFAULT NULL COMMENT '安装日期',
    `warranty_date` DATE DEFAULT NULL COMMENT '质保到期日',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-停用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_facility_code` (`facility_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设施主表';

-- 插入设施分类初始数据
INSERT INTO `facility_category` (`category_name`, `category_code`, `description`, `sort_order`) VALUES
('码头设施', 'DOCK', '码头、泊位、栈桥等', 1),
('航道设施', 'CHANNEL', '航标、灯塔、导航设施等', 2),
('机械设备', 'MACHINE', '起重机、装卸设备等', 3),
('电气设施', 'ELECTRIC', '配电设备、照明设施等', 4),
('安全设施', 'SAFETY', '消防、救生、防护设施等', 5);

-- 插入示例设施数据
INSERT INTO `facility` (`facility_code`, `facility_name`, `category_id`, `location`, `specification`, `manufacturer`, `install_date`, `status`) VALUES
('FAC202601001', '1号泊位系缆桩', 1, '1号泊位东侧', 'QT-500型', '港口机械厂', '2020-06-15', 1),
('FAC202601002', '主航道灯浮标A1', 2, '主航道入口处', 'HL-3000', '航标设备公司', '2021-03-20', 1),
('FAC202601003', '3号门机', 3, '3号泊位中部', 'MQ25-30', '重工集团', '2019-11-08', 1),
('FAC202601004', '配电房变压器T1', 4, '配电房A区', 'S11-630', '电气设备公司', '2018-05-22', 1),
('FAC202601005', '消防泵房主泵', 5, '消防泵房', 'XBD8/20-100', '消防设备厂', '2020-08-10', 1);

-- 港航设施维修保养系统 - 里程碑1：初始化数据

USE design_8_new;

-- 插入角色数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`) VALUES
('ADMIN', '管理员/调度', '后台管理员，可管理设施、派单、验收、统计', 1),
('EXECUTOR', '维修/保养执行者', '微信端执行者，接单、到场、提交记录', 1),
('REPORTER', '巡检/上报者', '微信端上报者，扫码查档、上报故障', 1);

-- 插入用户数据（密码均为：123456，MD5加密后为：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `openid`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '13800138001', NULL, 1),
('executor1', 'e10adc3949ba59abbe56e057f20f883e', '执行者张三', '13800138002', 'mock_openid_wx001', 1),
('reporter1', 'e10adc3949ba59abbe56e057f20f883e', '上报者李四', '13800138003', 'mock_openid_wx002', 1);

-- 绑定用户角色关系
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 管理员
(2, 2),  -- executor1 -> 执行者
(3, 3);  -- reporter1 -> 上报者

-- 插入权限数据（预留，后续里程碑扩展）
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `resource_type`, `resource_path`, `status`) VALUES
('user:view', '查看用户信息', 'api', '/user/**', 1);

-- 绑定角色权限（所有角色都可以查看自己的用户信息）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1),
(2, 1),
(3, 1);

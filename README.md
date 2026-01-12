# 港航设施维修保养系统

## 项目简介

本系统是一个面向港口航道设施的维修保养管理系统，支持设施台账管理、工单全流程闭环、保养计划自动执行、库存备件管理以及运维数据统计分析。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot 2.7 + MyBatis-Plus + MySQL |
| 管理后台 | Vue 3 + Element Plus + Vite |
| 微信小程序 | uni-app |

---

## 快速启动

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS design_8_new DEFAULT CHARSET utf8mb4"

# 导入所有表结构和初始数据（按顺序执行）
mysql -u root -p design_8_new < backend/sql/milestone2_base.sql
mysql -u root -p design_8_new < backend/sql/milestone3_workorder.sql
mysql -u root -p design_8_new < backend/sql/milestone4_maintenance.sql
mysql -u root -p design_8_new < backend/sql/milestone5_inventory.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 3. 启动管理后台

```bash
cd admin-web
npm install
npm run dev
```

管理后台运行在 `http://localhost:3000`

### 4. 启动微信小程序

使用微信开发者工具打开 `miniapp` 目录

---

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 后台管理、派单、验收 |
| 执行者 | executor1 | 123456 | 微信端执行工单 |

---

## 业务闭环演示

### 闭环1：维修工单

```
1. 微信端登录 executor1
2. 首页 → 故障上报 → 选择设施 → 填写故障描述 → 提交
3. 后台登录 admin → 工单管理 → 找到待派发工单 → 派单给 executor1
4. 微信端 → 任务中心 → 接单
5. 填写处理记录 → 选择消耗备件（可选）→ 上传处理图片 → 提交
6. 后台 → 工单详情 → 验收通过
7. 后台 → 归档（可选）
```

### 闭环2：保养工单

```
1. 后台 → 保养计划 → 新增计划（选择设施、保养周期）
2. 保养计划到期后系统自动生成保养工单
3. 后续流程与维修工单一致（派单→接单→处理→验收）
```

### 闭环3：库存管理

```
1. 后台 → 库存管理 → 查看备件台账
2. 入库：点击备件行的"入库"按钮 → 输入数量
3. 出库：点击备件行的"出库"按钮 → 输入数量（不得超过当前库存）
4. 工单关联消耗：
   - 方式A：微信端处理工单时选择消耗备件
   - 方式B：后台工单详情点击"关联备件"
5. 库存预警：库存≤阈值的备件自动显示在预警列表
```

---

## 统计分析页面

管理后台 → 统计分析

### 关键指标

| 指标 | 统计口径 |
|------|----------|
| 工单完成率 | 已完成+已归档工单数 / 总工单数 × 100% |
| 平均处理耗时 | Σ(提交时间 - 接单时间) / 已提交工单数 |
| 故障高发设施 | 按设施分组统计维修工单数量，取Top N |

### 数据说明

- 数据范围：仅统计未删除的工单
- 时间筛选：基于工单创建时间
- 区分类型：维修工单(orderType=1)、保养工单(orderType=2)

---

## 防作假审计字段

工单详情中展示以下关键字段用于防作假审核：

| 字段 | 说明 |
|------|------|
| 上报人/执行人 | 操作人身份 |
| 接单时间/提交时间/验收时间 | 服务器时间戳 |
| 处理位置（经纬度） | GPS定位信息 |
| 处理图片 | 现场照片证据 |

---

## 统计接口清单

| 接口 | 方法 | 说明 |
|------|------|------|
| `/stats/workorder/overview` | GET | 工单统计概览 |
| `/stats/workorder/duration` | GET | 平均处理耗时 |
| `/stats/facility/top-fault` | GET | 故障高发设施Top N |
| `/stats/user/my` | GET | 当前用户个人统计 |

所有统计接口均为**只读**，支持 `startTime` 和 `endTime` 参数筛选时间范围。

---

## 项目结构

```
code/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/port/
│   │   ├── controller/      # REST接口
│   │   ├── service/         # 业务逻辑
│   │   ├── mapper/          # MyBatis Mapper
│   │   └── entity/          # 实体类
│   └── sql/                 # 数据库脚本
├── admin-web/               # Vue3 管理后台
│   ├── src/views/           # 页面组件
│   └── src/api/             # API封装
├── miniapp/                 # uni-app 微信小程序
│   ├── pages/               # 页面
│   └── api/                 # API封装
└── docs/                    # 项目文档
```

---

## 里程碑完成情况

| 里程碑 | 内容 | 状态 |
|--------|------|------|
| M1 | 项目初始化 | ✅ |
| M2 | 用户认证 + 设施台账 | ✅ |
| M3 | 工单全流程闭环 | ✅ |
| M4 | 保养计划自动执行 | ✅ |
| M5 | 库存备件 + 工单消耗 | ✅ |
| M6 | 统计分析 + 最终验收 | ✅ |

---

## 注意事项

1. 确保 MySQL 服务已启动
2. 后端配置文件：`backend/src/main/resources/application.yml`
3. 微信小程序需配置后端地址：`miniapp/utils/request.js`

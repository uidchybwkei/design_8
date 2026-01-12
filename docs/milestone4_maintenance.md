# 里程碑4：保养计划与保养工单

## 数据库表结构

### 1. 保养计划表 (maintenance_plan)


| 字段               | 类型         | 说明                 |
| ------------------ | ------------ | -------------------- |
| id                 | BIGINT       | 主键                 |
| plan_name          | VARCHAR(100) | 计划名称             |
| plan_type          | TINYINT      | 计划类型: 1-周期保养 |
| facility_id        | BIGINT       | 关联设施ID           |
| facility_code      | VARCHAR(50)  | 设施编码（冗余）     |
| facility_name      | VARCHAR(100) | 设施名称（冗余）     |
| cycle_days         | INT          | 周期天数             |
| description        | VARCHAR(500) | 保养内容描述         |
| status             | TINYINT      | 状态: 0-停用, 1-启用 |
| last_generate_date | DATE         | 上次生成工单日期     |
| next_generate_date | DATE         | 下次生成工单日期     |

### 2. 保养工单生成记录表 (maintenance_generate_log)


| 字段          | 类型        | 说明                 |
| ------------- | ----------- | -------------------- |
| id            | BIGINT      | 主键                 |
| plan_id       | BIGINT      | 保养计划ID           |
| generate_date | DATE        | 生成日期（周期标识） |
| order_id      | BIGINT      | 生成的工单ID         |
| order_no      | VARCHAR(50) | 生成的工单编号       |

**幂等控制**：`(plan_id, generate_date)` 唯一索引

---

## 工单类型扩展

保养工单复用 `work_order` 表，通过 `order_type` 字段区分：


| order_type | 说明     |
| ---------- | -------- |
| 1          | 维修工单 |
| 2          | 保养工单 |

---

## 定时任务

### 触发方式

1. **自动触发**：每日凌晨 1:00 执行 (`0 0 1 * * ?`)
2. **手动触发**：调用 `POST /maintenance-plan/trigger-generate` 接口

### 执行逻辑

```
1. 扫描所有 status=1 且 next_generate_date <= 当前日期 的保养计划
2. 对每个计划：
   a. 检查 maintenance_generate_log 中是否存在 (plan_id, 当前日期) 记录
   b. 若存在，跳过（幂等控制）
   c. 若不存在，生成保养工单 (order_type=2)
   d. 插入生成记录
   e. 更新 last_generate_date 和 next_generate_date
```

### 幂等控制策略

- 唯一索引 `uk_plan_date (plan_id, generate_date)` 保证同一计划同一天只生成一次
- 即使多次触发定时任务，也不会重复生成工单

---

## 后端接口清单

### 保养计划管理


| 接口                                  | 方法   | 说明                   |
| ------------------------------------- | ------ | ---------------------- |
| `/maintenance-plan/list`              | GET    | 获取保养计划列表       |
| `/maintenance-plan/{id}`              | GET    | 获取计划详情           |
| `/maintenance-plan`                   | POST   | 创建保养计划           |
| `/maintenance-plan/{id}`              | PUT    | 更新保养计划           |
| `/maintenance-plan/{id}/enable`       | POST   | 启用计划               |
| `/maintenance-plan/{id}/disable`      | POST   | 停用计划               |
| `/maintenance-plan/{id}`              | DELETE | 删除计划               |
| `/maintenance-plan/trigger-generate`  | POST   | 手动触发定时任务       |
| `/maintenance-plan/{id}/generate-now` | POST   | 立即生成指定计划的工单 |

### 工单接口扩展


| 接口              | 方法 | 新增参数                   |
| ----------------- | ---- | -------------------------- |
| `/workorder/page` | GET  | `orderType` - 工单类型筛选 |

---

## 硬性规则校验

### 1. 保养工单提交校验

与维修工单一致：

- `processImages`：必须≥1张图片
- 服务器时间戳自动生成

### 2. 停用计划不生成工单

- `status=0` 的计划不会被定时任务扫描
- 手动生成时会返回空

### 3. 已归档工单不可修改

- `status=5` 的保养工单与维修工单一样不可修改

---

## 测试账号


| 角色   | 用户名    | 密码   | 说明     |
| ------ | --------- | ------ | -------- |
| 管理员 | admin     | 123456 | 后台登录 |
| 执行者 | executor1 | 123456 | 微信端   |

---

## 完整保养闭环演示步骤

### Step 1: 初始化数据库

```bash
mysql -u root -p design_8_new < backend/sql/milestone4_maintenance.sql
```

### Step 2: 启动后端

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

### Step 3: 启动管理后台

```bash
cd admin-web
npm run dev
```

### Step 4: 创建保养计划

1. 管理后台登录（admin/123456）
2. 点击"保养计划"菜单
3. 点击"新增计划"
4. 填写：
   - 计划名称：1号泊位系缆桩月度保养
   - 关联设施：1号泊位系缆桩
   - 周期(天)：30
   - 保养内容：检查系缆桩固定螺栓，清理锈蚀，涂抹防锈油
5. 确认创建

### Step 5: 生成保养工单

1. 在保养计划列表中，点击"执行定时任务"按钮
2. 或点击对应计划的"立即生成"按钮
3. 提示工单生成成功

### Step 6: 后台派单

1. 点击"工单管理"
2. 筛选类型为"保养"
3. 找到刚生成的保养工单
4. 点击"派单"，选择执行人

### Step 7: 微信端执行

1. 微信端登录
2. 进入任务中心
3. 在待接单列表找到保养工单（带"保养"标签）
4. 点击进入详情，接单
5. 点击"填写处理记录"
6. 填写处理说明，上传图片
7. 提交

### Step 8: 后台验收

1. 管理后台刷新工单列表
2. 找到待验收的保养工单
3. 点击"验收"，确认

### Step 9: 归档

1. 工单状态变为已完成
2. 点击"归档"

---


### Step 4: 完整保养闭环


| 步骤 | 端     | 操作                                         |
| ---- | ------ | -------------------------------------------- |
| 1    | 后台   | 保养计划 → 新增计划 → 选择设施 → 设置周期 |
| 2    | 后台   | 点击"立即生成"或"执行定时任务"               |
| 3    | 后台   | 工单管理 → 筛选"保养" → 派单               |
| 4    | 微信端 | 任务中心 → 待接单（显示"保养"标签）→ 接单  |
| 5    | 微信端 | 填写处理记录 → 上传图片 → 提交             |
| 6    | 后台   | 验收 → 归档                                 |

---


## 完成定义（DoD）核验

- [X]  后台可创建并启用保养计划
- [X]  定时任务可自动生成保养工单
- [X]  手动触发定时任务用于演示
- [X]  可完整演示一次保养闭环
- [X]  保养工单与设施历史正确关联
- [X]  保养工单与维修工单在同一体系下运行
- [X]  幂等控制生效（同一计划同一天不重复生成）
- [X]  停用的计划不生成新工单

---

## 本阶段不包含

- ❌ 库存/备件/消耗功能
- ❌ 统计分析页面
- ❌ 地图可视化功能
- ❌ 外部地图API调用

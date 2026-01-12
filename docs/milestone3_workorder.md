# 里程碑3：维修工单闭环

## 数据库表结构

### 1. 维修工单表 (work_order)


| 字段                | 类型          | 说明                                                             |
| ------------------- | ------------- | ---------------------------------------------------------------- |
| id                  | BIGINT        | 主键                                                             |
| order_no            | VARCHAR(50)   | 工单编号（唯一）                                                 |
| order_type          | TINYINT       | 工单类型: 1-维修                                                 |
| facility_id         | BIGINT        | 关联设施ID                                                       |
| facility_code       | VARCHAR(50)   | 设施编码（冗余）                                                 |
| facility_name       | VARCHAR(100)  | 设施名称（冗余）                                                 |
| status              | TINYINT       | 状态: 0-待派发, 1-已派发, 2-已接单, 3-待验收, 4-已完成, 5-已归档 |
| fault_description   | VARCHAR(500)  | 故障描述                                                         |
| fault_images        | VARCHAR(1000) | 故障图片（JSON数组）                                             |
| reporter_id         | BIGINT        | 上报人ID                                                         |
| reporter_name       | VARCHAR(50)   | 上报人姓名                                                       |
| report_time         | DATETIME      | 上报时间                                                         |
| assignee_id         | BIGINT        | 执行人ID                                                         |
| assignee_name       | VARCHAR(50)   | 执行人姓名                                                       |
| assign_time         | DATETIME      | 派单时间                                                         |
| accept_time         | DATETIME      | 接单时间                                                         |
| process_description | VARCHAR(1000) | 处理说明                                                         |
| process_images      | VARCHAR(2000) | 处理图片（JSON数组）                                             |
| process_longitude   | DECIMAL(10,6) | 处理时经度                                                       |
| process_latitude    | DECIMAL(10,6) | 处理时纬度                                                       |
| submit_time         | DATETIME      | 提交时间（服务器时间戳）                                         |
| verify_time         | DATETIME      | 验收时间                                                         |
| verifier_id         | BIGINT        | 验收人ID                                                         |
| verifier_name       | VARCHAR(50)   | 验收人姓名                                                       |
| verify_remark       | VARCHAR(500)  | 验收备注                                                         |
| archive_time        | DATETIME      | 归档时间                                                         |

### 2. 工单操作日志表 (work_order_log)


| 字段          | 类型         | 说明       |
| ------------- | ------------ | ---------- |
| id            | BIGINT       | 主键       |
| order_id      | BIGINT       | 工单ID     |
| order_no      | VARCHAR(50)  | 工单编号   |
| action        | VARCHAR(50)  | 操作类型   |
| from_status   | TINYINT      | 原状态     |
| to_status     | TINYINT      | 目标状态   |
| operator_id   | BIGINT       | 操作人ID   |
| operator_name | VARCHAR(50)  | 操作人姓名 |
| remark        | VARCHAR(500) | 操作备注   |
| create_time   | DATETIME     | 操作时间   |

### 3. 文件附件表 (file_attachment)


| 字段        | 类型         | 说明       |
| ----------- | ------------ | ---------- |
| id          | BIGINT       | 主键       |
| file_name   | VARCHAR(200) | 原始文件名 |
| file_path   | VARCHAR(500) | 存储路径   |
| file_url    | VARCHAR(500) | 访问URL    |
| file_size   | BIGINT       | 文件大小   |
| file_type   | VARCHAR(50)  | 文件类型   |
| biz_type    | VARCHAR(50)  | 业务类型   |
| biz_id      | BIGINT       | 业务ID     |
| uploader_id | BIGINT       | 上传人ID   |
| create_time | DATETIME     | 上传时间   |

---

## 状态机说明

```
待派发(0) ─────派单────→ 已派发(1)
                            │
                          接单
                            ↓
                        已接单(2)
                            │
                          提交
                            ↓
                        待验收(3)
                            │
                          验收
                            ↓
                        已完成(4)
                            │
                          归档
                            ↓
                        已归档(5)
```

**状态流转规则**：

- 0→1：后台派单
- 1→2：执行人接单
- 2→3：执行人提交（强制校验：图片≥1、服务器时间戳、经纬度）
- 3→4：后台验收通过
- 4→5：后台归档

---

## 后端接口清单

### 工单管理接口


| 接口                                       | 方法 | 说明                   |
| ------------------------------------------ | ---- | ---------------------- |
| `/workorder/page`                          | GET  | 分页查询工单列表       |
| `/workorder/{id}`                          | GET  | 获取工单详情           |
| `/workorder/{id}/logs`                     | GET  | 获取工单操作日志       |
| `/workorder/my`                            | GET  | 获取我的工单（执行人） |
| `/workorder/facility/{facilityId}/history` | GET  | 获取设施维修历史       |
| `/workorder/report`                        | POST | 上报故障（创建工单）   |
| `/workorder/{id}/assign`                   | POST | 派单                   |
| `/workorder/{id}/reassign`                 | POST | 转派                   |
| `/workorder/{id}/accept`                   | POST | 接单                   |
| `/workorder/{id}/submit`                   | POST | 提交处理结果           |
| `/workorder/{id}/verify`                   | POST | 验收                   |
| `/workorder/{id}/archive`                  | POST | 归档                   |
| `/workorder/assignable-users`              | GET  | 获取可分配的执行人     |

### 文件上传接口


| 接口           | 方法 | 说明     |
| -------------- | ---- | -------- |
| `/file/upload` | POST | 上传文件 |

---

## 硬性规则校验

### 1. 提交时必填校验

提交工单（`/workorder/{id}/submit`）时，后端强制校验：

- `processImages`：必须是有效JSON数组，且至少包含1个图片URL
- `longitude`：必须提供经度
- `latitude`：必须提供纬度
- `submitTime`：由服务器自动生成（不接受客户端传入）

任一缺失返回错误：

```json
{
  "code": 400,
  "message": "提交时必须上传至少1张处理图片"
}
```

### 2. 状态流转校验

- 只能按状态机规则流转，不允许跳跃或回退
- 例如：已接单(2)不能直接到已完成(4)

### 3. 权限校验

- 接单/提交：只能操作自己被分配的工单
- 派单/验收/归档：需要管理权限

### 4. 已归档不可修改

- 已归档(5)状态的工单，所有修改接口均返回错误

### 5. 停用设施不能创建工单

- 调用上报故障接口时，如果设施状态为停用(0)，返回错误

---

## 测试账号


| 角色   | 用户名    | 密码   | 说明                       |
| ------ | --------- | ------ | -------------------------- |
| 管理员 | admin     | 123456 | 后台登录，可派单/验收/归档 |
| 执行者 | executor1 | 123456 | 微信端，mock_openid_wx001  |

---

## 完整闭环演示流程

### Step 1: 初始化数据库

```bash
mysql -u root -p design_8_new < backend/sql/milestone3_workorder.sql
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
npm install
npm run dev
```

### Step 4: 微信小程序（HBuilderX运行到微信开发者工具）

---

## 演示步骤

### 1️⃣ 微信端上报故障

1. 微信端登录（mock openid）
2. 点击"扫码"Tab，输入设施编码 `FAC202601001` 查询
3. 进入设施详情，点击"上报故障"
4. 填写故障描述，提交

### 2️⃣ 后台派单

1. 管理后台登录（admin/123456）
2. 点击"工单管理"，找到刚创建的待派发工单
3. 点击"派单"，选择执行人（执行者张三），确认

### 3️⃣ 微信端接单

1. 微信端进入"任务中心"Tab
2. 在"待接单"列表中找到工单
3. 点击进入详情，点击"接单"

### 4️⃣ 微信端到场扫码确认（可选）

1. 点击"到场扫码确认"
2. 扫描设施二维码，确认到达现场

### 5️⃣ 微信端提交处理结果

1. 点击"填写处理记录"
2. 填写处理说明
3. 上传至少1张图片
4. 获取当前位置（自动获取经纬度）
5. 提交

### 6️⃣ 后台验收

1. 管理后台刷新工单列表
2. 找到"待验收"状态的工单
3. 点击"详情"查看处理记录和图片
4. 点击"验收"，输入备注（可选），确认

### 7️⃣ 后台归档

1. 工单状态变为"已完成"
2. 点击"归档"，确认

### 8️⃣ 查看设施维修历史

1. 微信端扫码进入设施详情
2. 点击"维修记录"，可看到刚完成的工单

---

## 完成定义（DoD）核验

- [X]  可以完整演示一次维修闭环（从上报到归档）
- [X]  工单状态流转、权限控制、防作假校验全部生效
- [X]  后台与微信端数据一致，刷新后状态不丢失
- [X]  设施详情中能看到刚完成的维修记录
- [X]  提交时强制校验图片、时间戳、经纬度

---



## 完整闭环演示


| 步骤 | 端     | 操作                                                                    |
| ---- | ------ | ----------------------------------------------------------------------- |
| 1    | 微信端 | 登录 → 扫码 → 输入`<span>FAC202601001</span>` → 设施详情 → 上报故障 |
| 2    | 后台   | admin登录 → 工单管理 → 找到待派发工单 → 派单给执行人                 |
| 3    | 微信端 | 任务中心 → 待接单 → 点击工单 → 接单                                  |
| 4    | 微信端 | 填写处理记录 → 上传图片 → 获取位置 → 提交                            |
| 5    | 后台   | 刷新 → 待验收工单 → 详情 → 验收通过                                  |
| 6    | 后台   | 已完成 → 归档                                                          |
| 7    | 微信端 | 扫码进设施详情 → 维修记录 → 可见刚完成的工单                          |

---

## 测试账号

## 本阶段不包含

- 任何保养计划、定时任务、保养工单
- 任何库存、备件、消耗相关内容
- 任何统计分析页面或接口
- 任何地图可视化、地图组件、marker、路线规划
- GPS仅作为字段保存和展示，不渲染地图

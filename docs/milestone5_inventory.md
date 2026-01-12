# 里程碑5：库存备件最小可用 + 工单消耗关联

## 一、数据库设计

### 1. 备件主数据表 (inventory_item)

```sql
CREATE TABLE `inventory_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_code` VARCHAR(50) NOT NULL COMMENT '备件编码（唯一）',
    `item_name` VARCHAR(100) NOT NULL COMMENT '备件名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '型号/规格',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位（个、件、米、kg等）',
    `current_stock` INT DEFAULT 0 COMMENT '当前库存数量',
    `warning_threshold` INT DEFAULT 10 COMMENT '预警阈值（库存≤此值触发预警）',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    ...
);
```

### 2. 库存流水记录表 (inventory_record)

```sql
CREATE TABLE `inventory_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `item_id` BIGINT NOT NULL COMMENT '备件ID',
    `record_type` TINYINT NOT NULL COMMENT '记录类型: 1-入库, 2-出库',
    `quantity` INT NOT NULL COMMENT '数量（正数）',
    `before_stock` INT NOT NULL COMMENT '操作前库存',
    `after_stock` INT NOT NULL COMMENT '操作后库存',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联工单ID（出库时可关联）',
    ...
);
```

### 3. 工单备件消耗关联表 (work_order_consumption)

```sql
CREATE TABLE `work_order_consumption` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `item_id` BIGINT NOT NULL COMMENT '备件ID',
    `quantity` INT NOT NULL COMMENT '消耗数量',
    `record_id` BIGINT DEFAULT NULL COMMENT '关联的库存流水ID',
    ...
    UNIQUE KEY `uk_order_item` (`order_id`, `item_id`)
);
```

---

## 二、后端接口清单

### 备件管理


| 方法   | 路径                           | 说明             |
| ------ | ------------------------------ | ---------------- |
| GET    | `/inventory/item/list`         | 获取所有备件列表 |
| GET    | `/inventory/item/{id}`         | 获取备件详情     |
| GET    | `/inventory/item/warning`      | 获取预警备件列表 |
| POST   | `/inventory/item`              | 创建备件         |
| PUT    | `/inventory/item/{id}`         | 更新备件         |
| POST   | `/inventory/item/{id}/enable`  | 启用备件         |
| POST   | `/inventory/item/{id}/disable` | 停用备件         |
| DELETE | `/inventory/item/{id}`         | 删除备件         |

### 库存操作


| 方法 | 路径                              | 说明               |
| ---- | --------------------------------- | ------------------ |
| POST | `/inventory/stock-in`             | 入库操作           |
| POST | `/inventory/stock-out`            | 出库操作           |
| GET  | `/inventory/record/item/{itemId}` | 获取备件的流水记录 |
| GET  | `/inventory/record/recent`        | 获取最近流水记录   |

### 工单消耗关联


| 方法 | 路径                                     | 说明               |
| ---- | ---------------------------------------- | ------------------ |
| POST | `/inventory/consumption`                 | 添加工单备件消耗   |
| GET  | `/inventory/consumption/order/{orderId}` | 获取工单的备件消耗 |

---

## 三、硬性规则

1. **出库校验**：出库数量必须 ≤ 当前库存
2. **流水记录**：所有库存变动必须有流水记录，不允许直接修改库存数
3. **消耗关联时机**：仅【已完成(status=4)】工单可关联备件消耗
4. **归档不可逆**：已归档工单的备件消耗不可修改、库存记录不可回滚

---

## 四、测试账号


| 角色   | 用户名    | 密码   | 说明       |
| ------ | --------- | ------ | ---------- |
| 管理员 | admin     | 123456 | 后台管理   |
| 执行者 | executor1 | 123456 | 微信端执行 |

---

## 五、完整演示步骤

### Step 1: 初始化数据库

```bash
mysql -u root -p design_8_new < backend/sql/milestone5_inventory.sql
```

### Step 2: 启动后端

```bash
cd backend
mvn spring-boot:run
```

### Step 3: 启动管理后台

```bash
cd admin-web
npm run dev
```

### Step 4: 备件管理

1. 管理后台登录（admin/123456）
2. 点击"库存管理"菜单
3. 在"备件台账"中查看已有备件（初始化数据包含5个测试备件）
4. 点击"新增备件"添加新备件
5. 可编辑/启停现有备件

### Step 5: 入库操作

1. 在备件列表中，点击某个备件的"入库"按钮
2. 输入数量和备注
3. 确认后库存增加
4. 在"库存流水"标签页查看入库记录

### Step 6: 出库操作

1. 在备件列表中，点击某个备件的"出库"按钮
2. 输入数量（不能超过当前库存）
3. 确认后库存减少
4. 在"库存流水"标签页查看出库记录

### Step 7: 库存预警

1. 点击"库存预警"标签页
2. 查看库存≤预警阈值的备件列表
3. 可直接点击"入库"补货

### Step 8: 工单关联备件消耗

1. 创建并完成一个工单（维修或保养都可以）
2. 工单流程：上报→派单→接单→处理→提交→验收
3. 验收通过后，工单状态变为"已完成"(status=4)
4. 打开工单详情，在"备件消耗"区域点击"关联备件"
5. 选择备件、输入消耗数量
6. 确认后自动扣减库存，生成消耗记录

### Step 9: 微信端查看

1. 微信端登录（executor1/123456）
2. 进入任务中心，选择一个已完成的工单
3. 在工单详情底部查看"备件消耗"信息

### Step 10: 归档

1. 后台对已完成工单点击"归档"
2. 归档后备件消耗不可再修改

---


**完整闭环：**

1. **库存管理** → 查看备件台账 → 入库/出库操作
2. **库存预警** → 查看低库存备件 → 补货
3. **创建工单** → 完成工单流程（派单→接单→处理→验收）
4. **关联消耗** → 工单详情 → 点击"关联备件" → 选择备件+数量
5. **微信端查看** → 工单详情底部显示备件消耗

## 六、DoD 验证清单

- [ ]  后台可维护备件台账（新增/编辑/启停）
- [ ]  可通过入库/出库操作正确改变库存
- [ ]  库存不足时出库被拒绝
- [ ]  库存≤预警阈值的备件出现在预警列表
- [ ]  已完成工单可关联备件消耗
- [ ]  关联消耗时自动扣减库存
- [ ]  已归档工单无法新增/修改消耗
- [ ]  微信端工单详情可查看消耗记录
- [ ]  数据库重启后库存数据一致

---

## 七、文件清单

### 后端新增文件

```
backend/sql/milestone5_inventory.sql
backend/src/main/java/com/port/entity/InventoryItem.java
backend/src/main/java/com/port/entity/InventoryRecord.java
backend/src/main/java/com/port/entity/WorkOrderConsumption.java
backend/src/main/java/com/port/mapper/InventoryItemMapper.java
backend/src/main/java/com/port/mapper/InventoryRecordMapper.java
backend/src/main/java/com/port/mapper/WorkOrderConsumptionMapper.java
backend/src/main/java/com/port/service/InventoryService.java
backend/src/main/java/com/port/controller/InventoryController.java
```

### 后端修改文件

```
backend/src/main/java/com/port/service/UserService.java  # 新增getCurrentUser方法
```

### 管理后台新增/修改文件

```
admin-web/src/api/inventory.js  # 新增
admin-web/src/views/Inventory.vue  # 重写
admin-web/src/views/Workorder.vue  # 新增备件消耗展示
```

### 微信端修改文件

```
miniapp/api/workorder.js  # 新增getOrderConsumptions
miniapp/pages/workorder/detail.vue  # 新增备件消耗展示
```

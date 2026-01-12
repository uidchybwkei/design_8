# 里程碑2：设施资产与一物一码扫码识别

## 数据库表结构

### 1. 设施分类表 (facility_category)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| category_name | VARCHAR(50) | 分类名称 |
| category_code | VARCHAR(30) | 分类编码（唯一） |
| description | VARCHAR(200) | 分类描述 |
| sort_order | INT | 排序 |
| status | TINYINT | 状态: 1-启用, 0-停用 |
| deleted | TINYINT | 删除标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 2. 设施主表 (facility)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| facility_code | VARCHAR(50) | 设施编码（唯一，用于二维码） |
| facility_name | VARCHAR(100) | 设施名称 |
| category_id | BIGINT | 所属分类ID |
| location | VARCHAR(200) | 安装位置描述 |
| specification | VARCHAR(200) | 规格型号 |
| manufacturer | VARCHAR(100) | 生产厂家 |
| install_date | DATE | 安装日期 |
| warranty_date | DATE | 质保到期日 |
| status | TINYINT | 状态: 1-启用, 0-停用 |
| remark | VARCHAR(500) | 备注 |
| deleted | TINYINT | 删除标记 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_by | BIGINT | 更新人ID |
| update_time | DATETIME | 更新时间 |

---

## 后端接口说明

### 设施管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/facility/page` | GET | 分页查询设施列表 |
| `/facility/{id}` | GET | 根据ID获取设施详情 |
| `/facility/code/{code}` | GET | 根据编码获取设施（含状态校验） |
| `/facility/scan/{code}` | GET | 扫码查询设施（含状态校验） |
| `/facility` | POST | 新增设施 |
| `/facility` | PUT | 更新设施 |
| `/facility/{id}/status` | PUT | 更新设施状态（启用/停用） |
| `/facility/{id}` | DELETE | 删除设施 |
| `/facility/categories` | GET | 获取分类列表 |
| `/facility/qrcode/{id}` | GET | 获取设施二维码内容 |

### 接口参数说明

#### 分页查询 `/facility/page`
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认10）
- `categoryId`: 分类ID（可选）
- `status`: 状态（可选，1启用/0停用）
- `keyword`: 关键词（名称/编码模糊搜索）

#### 扫码查询 `/facility/scan/{code}`
- 返回设施详情
- 如果设施不存在，返回 404 错误
- 如果设施已停用，返回 400 错误

---

## 测试步骤

### 1. 初始化数据库

```bash
# 执行里程碑2的SQL脚本
mysql -u root -p design_8_new < backend/sql/milestone2_facility.sql
```

### 2. 启动后端

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

### 3. 启动管理后台

```bash
cd admin-web
npm install  # 首次需要安装qrcode依赖
npm run dev
```

访问 http://localhost:3000，使用 admin/123456 登录。

### 4. 测试管理后台功能

1. **查看设施列表**：点击左侧菜单"设施管理"
2. **新增设施**：点击"新增设施"，填写信息并保存
3. **编辑设施**：点击列表中的"编辑"按钮
4. **生成二维码**：点击"二维码"按钮，可下载二维码图片
5. **停用设施**：点击"停用"按钮
6. **启用设施**：点击"启用"按钮

### 5. 启动微信小程序

在 HBuilderX 中运行 miniapp 项目到微信开发者工具。

### 6. 测试微信端功能

1. **登录**：点击"微信登录"
2. **进入扫码页面**：点击底部Tab"扫码"
3. **扫描二维码**：
   - 方式1：点击"扫描二维码"扫描管理后台下载的二维码
   - 方式2：手动输入设施编码（如 FAC202601001）点击查询
4. **查看设施详情**：确认能正确显示设施信息

### 7. 验证异常情况

1. **非法编码**：输入不存在的编码，应提示"设施不存在或已删除"
2. **已停用设施**：在后台停用某设施后扫码，应提示"该设施已停用"

---

## 示例设施数据

| 编码 | 名称 | 分类 | 位置 |
|------|------|------|------|
| FAC202601001 | 1号泊位系缆桩 | 码头设施 | 1号泊位东侧 |
| FAC202601002 | 主航道灯浮标A1 | 航道设施 | 主航道入口处 |
| FAC202601003 | 3号门机 | 机械设备 | 3号泊位中部 |
| FAC202601004 | 配电房变压器T1 | 电气设施 | 配电房A区 |
| FAC202601005 | 消防泵房主泵 | 安全设施 | 消防泵房 |

---

## 完成定义（DoD）核验

- [x] 后台可以新增/编辑/停用设施，并生成可下载的二维码
- [x] 微信端扫码该二维码后，可以正确展示设施信息
- [x] 非法或已停用的设施二维码，有明确的错误提示
- [x] 设施模块代码、接口、表结构清晰独立，不与后续模块耦合

---

## 本阶段不包含

- 任何工单、保养、库存相关表或接口
- 任何地图可视化、地图组件、marker、导航、地理编码
- 任何外部地图 API 调用

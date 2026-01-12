# 港航设施维修保养系统 - 里程碑1交付文档

## 项目概述
港航设施维修保养系统，基于 Spring Boot + Vue3 + Element Plus + uni-app 技术栈开发。

**当前里程碑：里程碑1 - 工程基座与统一规范（可启动、可登录、可鉴权）**

## 技术栈
- **后端**：Spring Boot 2.7.18 + MyBatis-Plus + MySQL + JWT
- **管理后台**：Vue 3 + Element Plus + Vite + Pinia
- **微信端**：uni-app（编译到微信小程序）

## 项目结构
```
code/
├── backend/              # Spring Boot 后端
│   ├── src/
│   ├── sql/             # 数据库脚本
│   └── pom.xml
├── admin-web/           # Vue3 管理后台
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── miniapp/             # uni-app 微信端
│   ├── pages/
│   ├── api/
│   ├── utils/
│   └── package.json
└── docs/                # 文档
```

## 环境要求

### 后端
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+ / 8.0+

### 前端
- Node.js 16+
- npm 或 pnpm

### 微信小程序
- 微信开发者工具（最新版）
- HBuilderX（可选，推荐使用命令行）

## 快速启动

### 第一步：初始化数据库

1. 确保 MySQL 已启动
2. 执行数据库脚本：
```bash
# 进入后端sql目录
cd backend/sql

# 执行建表脚本
mysql -uroot -p < schema.sql

# 执行初始化数据脚本
mysql -uroot -p < data.sql
```

3. 确认数据库 `design_8_new` 已创建且包含以下表：
   - sys_user（用户表）
   - sys_role（角色表）
   - sys_permission（权限表）
   - sys_user_role（用户角色关联表）
   - sys_role_permission（角色权限关联表）

### 第二步：启动后端服务

```bash
# 进入后端目录
cd backend

# 修改配置（如需要）
# 编辑 src/main/resources/application.yml
# 确认数据库连接信息（默认：localhost:3306，用户名root，密码root）

# 安装依赖并启动
mvn clean install
mvn spring-boot:run

# 或使用IDE（IDEA/Eclipse）直接运行 MaintenanceApplication
```

**启动成功标志**：
- 控制台输出：`Started MaintenanceApplication in xx seconds`
- 端口：http://localhost:8080

### 第三步：启动管理后台

```bash
# 进入管理后台目录
cd admin-web

# 安装依赖
npm install
# 或 pnpm install

# 启动开发服务器
npm run dev
```

**启动成功标志**：
- 浏览器自动打开：http://localhost:3000
- 显示登录页面

### 第四步：启动微信小程序（可选）

```bash
# 进入小程序目录
cd miniapp

# 安装依赖
npm install

# 编译到微信小程序
npm run dev:mp-weixin
```

然后：
1. 打开微信开发者工具
2. 导入项目：选择 `miniapp/dist/dev/mp-weixin` 目录
3. AppID：使用测试号或自己的AppID

## 测试账号

系统已预置以下测试账号（密码均为：123456）：

| 账号名 | 角色 | 用途 | 绑定openid |
|--------|------|------|-----------|
| admin | 管理员/调度 | 后台管理 | 无 |
| executor1 | 维修/保养执行者 | 微信端执行 | mock_openid_wx001 |
| reporter1 | 巡检/上报者 | 微信端上报 | mock_openid_wx002 |

## 里程碑1验收测试

### 测试1：后台登录与鉴权
1. 访问 http://localhost:3000
2. 使用账号 `admin` / 密码 `123456` 登录
3. **预期结果**：
   - 登录成功，跳转到首页
   - 左侧菜单显示：首页、设施管理、工单管理、保养计划、库存管理、统计分析
   - 右上角显示用户名"管理员"
   - 点击各菜单项，页面切换正常（显示占位提示）

### 测试2：Token鉴权
1. 登录后，打开浏览器开发者工具 -> Network
2. 点击"设施管理"菜单
3. **预期结果**：
   - 请求头包含 `Authorization: Bearer xxx`
   - 不会跳转到登录页

4. 手动删除 localStorage 中的 token：
```javascript
localStorage.removeItem('token')
```
5. 刷新页面
6. **预期结果**：自动跳转到登录页

### 测试3：微信端登录（使用mock openid）
1. 在微信开发者工具中打开小程序
2. 点击"微信登录"按钮
3. **预期结果**：
   - 提示"登录成功"
   - 自动跳转到"任务中心"页面（显示占位）
   - 底部 Tab 栏显示：任务中心、扫码、我的

### 测试4：微信端获取用户信息
1. 登录后，切换到"我的"页面
2. **预期结果**：
   - 显示用户真实姓名"执行者张三"
   - 显示手机号"13800138002"
   - 可以点击"退出登录"

### 测试5：接口鉴权拦截
使用 Postman 或 curl 测试：

```bash
# 未登录访问受保护接口（应返回401）
curl http://localhost:8080/user/info

# 登录获取token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 使用token访问（应返回200）
curl http://localhost:8080/user/info \
  -H "Authorization: Bearer <上一步返回的token>"
```

**预期结果**：
- 未携带token返回 401 错误
- 携带token返回用户信息

## 里程碑1完成标准（DoD）

✅ **后端**：
- [x] Spring Boot 工程可启动
- [x] JWT 鉴权拦截器生效
- [x] RBAC 数据模型建立（仅 user/role/permission/user_role 表）
- [x] 统一返回结构（Result/PageResult）
- [x] 登录接口可用（/auth/login、/auth/wx-login）
- [x] 用户信息接口可用（/user/info）

✅ **管理后台**：
- [x] Vue3 + Element Plus 工程可启动
- [x] 登录页面可用，支持账号密码登录
- [x] Token 存储到 localStorage
- [x] 路由守卫生效（未登录跳转登录页）
- [x] 基础布局（侧边栏菜单+页头+内容区）
- [x] 空页面占位（设施/工单/保养/库存/统计）

✅ **微信端**：
- [x] uni-app 工程可编译到微信小程序
- [x] 登录页面可用，支持微信登录（mock）
- [x] Token 存储到 storage
- [x] 底部 Tab 栏（任务中心/扫码/我的）
- [x] 空页面占位

## 本里程碑不包含的功能

❌ **不做**（在后续里程碑实现）：
- 设施管理业务逻辑（里程碑2）
- 工单管理业务逻辑（里程碑3）
- 保养计划业务逻辑（里程碑4）
- 库存管理业务逻辑（里程碑5）
- 统计分析业务逻辑（里程碑6）

❌ **严格禁止**（全流程都不做）：
- 地图可视化（腾讯/高德/百度等地图SDK）
- 地图点位展示（marker）
- 路线规划/导航
- 地理编码/逆地理编码
- 任何外部地图API调用

## 常见问题

### Q1: 后端启动失败，提示数据库连接错误
**A**: 检查以下项：
1. MySQL 是否已启动
2. 数据库 `design_8_new` 是否已创建
3. `application.yml` 中的数据库用户名密码是否正确

### Q2: 管理后台登录后接口返回401
**A**: 
1. 确认后端已启动
2. 检查浏览器控制台 Network 中接口地址是否正确（应为 http://localhost:8080）
3. 检查 vite.config.js 中的 proxy 配置

### Q3: 微信小程序无法连接后端
**A**: 
1. 微信开发者工具需要关闭"不校验合法域名"选项
2. 或在 manifest.json 中设置 `"urlCheck": false`
3. 确认 `miniapp/utils/request.js` 中 BASE_URL 为 `http://localhost:8080`

### Q4: 微信登录提示"用户未绑定"
**A**: 
- 里程碑1使用 mock openid，固定为 `mock_openid_wx001`
- 确保数据库中 `executor1` 用户的 openid 字段为 `mock_openid_wx001`

## 下一步（里程碑2）

完成里程碑1验收后，将开始实现：
- 设施资产管理（建档、CRUD、二维码生成与绑定）
- 扫码识别设施（微信端扫码查看设施详情）
- 设施历史记录入口（占位，里程碑3填充数据）

---

**里程碑1已完成，请开始测试。未收到你的确认，不会进入里程碑2。**

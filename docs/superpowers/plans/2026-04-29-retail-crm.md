# 零售业CRM系统 实施计划

> **Goal:** 基于名创优品案例，构建零售业CRM系统，覆盖客户管理、会员运营、营销转化、销售服务、数据分析和员工管理六大模块。

**架构:** 前后端分离，Vue3 + SpringBoot 3.x，RESTful API，JWT认证，ECharts数据可视化。

**技术栈:** Vue 3 (Composition API) + Element Plus + ECharts 5 | SpringBoot 3.x + MyBatis-Plus + MySQL 8.0 | Maven + Vite

---

## 项目结构

```
crm-latest/
├── crm-backend/                        # SpringBoot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/crm/
│       │   ├── CrmApplication.java
│       │   ├── config/                 # SecurityConfig, CorsConfig, MybatisConfig
│       │   ├── common/                 # Result, PageResult, GlobalExceptionHandler
│       │   ├── security/               # JwtUtils, JwtFilter, LoginController
│       │   ├── module/
│       │   │   ├── customer/           # 客户管理 (controller/service/mapper/entity)
│       │   │   ├── member/             # 会员管理
│       │   │   ├── marketing/          # 营销管理
│       │   │   ├── sales/              # 销售服务管理
│       │   │   ├── analytics/          # 数据分析
│       │   │   └── employee/           # 员工管理
│       │   └── utils/
│       └── resources/
│           ├── application.yml
│           └── db/init.sql             # 建表脚本
│
├── crm-frontend/                       # Vue3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js
│       ├── api/                        # 按模块拆分: customer.js, member.js ...
│       ├── views/                      # 按模块分目录: customer/, member/ ...
│       ├── components/                 # Layout, Sidebar, Navbar
│       └── utils/                      # request.js (axios实例), auth.js
```

---

## 数据库核心表 (14张)

| 表名 | 模块 | 关键字段 |
|------|------|---------|
| `sys_user` | 认证 | id, username, password, real_name, role, phone, status |
| `customer` | 客户 | id, name, phone, email, level(高/中/低), source, status, owner_id, created_at |
| `customer_lead` | 线索 | id, customer_id, employee_id, status(跟进中/已转化/已流失), pool(是否公海), created_at |
| `member` | 会员 | id, customer_id, card_level(普通/银卡/金卡), points, registered_at |
| `point_record` | 积分 | id, member_id, type(获取/消耗), points, reason, created_at |
| `marketing_campaign` | 活动 | id, name, type(促销/满减/新品), start_time, end_time, status, target_group |
| `coupon` | 优惠券 | id, campaign_id, name, discount_value, min_amount, total_qty, used_qty |
| `coupon_record` | 券发放 | id, coupon_id, customer_id, status(未用/已用/过期), send_time, use_time |
| `sales_order` | 订单 | id, customer_id, employee_id, total_amount, status, created_at |
| `order_item` | 订单明细 | id, order_id, product_name, qty, price |
| `follow_up` | 回访 | id, customer_id, employee_id, plan_time, actual_time, content, type(回访/投诉) |
| `todo_task` | 待办 | id, employee_id, type(跟进/回访/活动), content, deadline, status |
| `employee_performance` | 业绩 | id, employee_id, month, sales_amount, new_customers, follow_up_count |

---

## API 设计概览 (RESTful)

统一响应格式: `{ code: 200, message: "ok", data: {} }`

**认证:** `POST /api/auth/login`, `GET /api/auth/info`

**客户:** `GET|POST /api/customers`, `GET|PUT|DELETE /api/customers/:id`, `GET /api/customers/pool` (公海), `POST /api/customers/:id/claim` (领取)

**会员:** `GET|POST /api/members`, `GET /api/members/:id/profile` (画像), `POST /api/members/:id/points` (积分变更)

**营销:** `GET|POST /api/campaigns`, `POST /api/campaigns/:id/send` (发券), `GET /api/coupons/stats` (核销统计)

**销售:** `GET|POST /api/orders`, `GET|POST /api/follow-ups`, `GET /api/sales/funnel` (漏斗数据)

**分析:** `GET /api/analytics/sales`, `GET /api/analytics/customers`, `GET /api/analytics/products`

**员工:** `GET|POST /api/todos`, `PUT /api/todos/:id/complete`, `GET /api/employees/performance`

---

## 实施阶段 (32个Task，预计7-10个工作日)

### 阶段零: 环境搭建

#### Task 1: 初始化SpringBoot项目
- 使用 Spring Initializr 创建: Java 17, SpringBoot 3.x, 依赖选 Spring Web, MyBatis-Plus, MySQL Driver, Spring Security, Lombok, Validation
- 解压到 `crm-backend/`, 运行 `./mvnw clean compile` 验证

#### Task 2: 初始化Vue3项目
- `npm create vite@latest crm-frontend -- --template vue`
- `npm install element-plus echarts vue-router@4 axios pinia`
- 运行 `npm run dev` 验证

#### Task 3: 数据库建表
- 在MySQL中创建 `crm_db` 数据库
- 执行 `db/init.sql`（包含上述14张表的完整DDL，含索引和外键）

#### Task 4: 后端基础配置
- `application.yml`: 配置数据源、MyBatis-Plus、服务端口 8080
- `CorsConfig.java`: 允许 localhost:5173 跨域
- `SecurityConfig.java`: 放行 `/api/auth/**`, 其余需要JWT认证
- `Result.java`: 统一返回体 `{code, message, data}`
- `GlobalExceptionHandler.java`: `@RestControllerAdvice` 全局异常处理

#### Task 5: JWT认证
- `JwtUtils.java`: token生成/验证/解析
- `JwtFilter.java`: `OncePerRequestFilter` 拦截验证
- `LoginController.java`: `POST /api/auth/login` 验证用户返回token
- 前端: `utils/request.js` (axios拦截器, 自动带token, 401跳登录), `utils/auth.js` (token存储)

---

### 阶段一: 客户管理模块 (最核心)

#### Task 6: 客户档案 CRUD
- 后端: `CustomerController` + `CustomerService` + `Customer` entity
- 支持分页查询、条件搜索（姓名/电话/等级）、Excel导入导出
- 前端: `views/customer/CustomerList.vue` (表格+搜索+分页), `CustomerForm.vue` (新增/编辑弹窗)

#### Task 7: 线索与公海池
- `customer_lead` 表: 导购登记线索，`pool=true` 进入公海
- 后端: `GET /api/customers/pool` 公海列表, `POST /api/customers/:id/claim` 员工领取
- 前端: `views/customer/LeadPool.vue` (公海池表格，可筛选可领取)

#### Task 8: 客户分类分层
- 后端: 定时任务按消费频次/金额自动计算分层 → 更新 `customer.level`
- 规则: 近30天消费≥3次且金额≥2000 → "高价值"; 近90天无消费 → "沉睡"
- 前端: `CustomerList.vue` 中增加等级筛选下拉，表格中用 `el-tag` 区分颜色

---

### 阶段二: 会员管理模块

#### Task 9: 会员注册与等级
- 后端: `MemberController` + `MemberService`, 消费时自动累计积分
- 等级规则: 积分0-999普通, 1000-4999银卡, 5000+金卡
- 前端: `views/member/MemberList.vue` (会员列表，等级筛选)

#### Task 10: 积分管理
- `point_record` 表记录每笔积分变动
- 后端: `POST /api/members/:id/points` (增加/扣减)
- 前端: `views/member/PointManage.vue` (积分变动记录表+手动加减积分)

#### Task 11: 客户画像
- 后端: `GET /api/members/:id/profile` 聚合消费偏好、到店门店、购买品类
- 返回标签数组如: `["美妆", "高频", "周末到店", "客单价50-100"]`
- 前端: `views/member/MemberDetail.vue` 用 `el-tag` 展示画像标签 + ECharts饼图(品类占比)

---

### 阶段三: 营销管理模块

#### Task 12: 活动管理
- 后端: `CampaignController` CRUD, 活动类型枚举(促销/满减/新品)
- 前端: `views/marketing/CampaignList.vue` + `CampaignForm.vue`

#### Task 13: 精准推送
- 后端: `POST /api/campaigns/:id/send` 根据 `target_group` 字段匹配客户
- 规则: target_group="高价值客户" → 查 customer.level='高价值' 的客户 → 批量发券
- 前端: `CampaignForm.vue` 中增加目标客群下拉选择(全部/高价值/银卡/金卡/沉睡)

#### Task 14: 优惠券管理
- 后端: `CouponController` 发券/核销/统计
- 核销统计接口: `GET /api/coupons/stats` 返回每个活动发券数、核销数、核销率
- 前端: `views/marketing/CouponManage.vue` (券列表+核销率展示)

---

### 阶段四: 销售与服务管理模块

#### Task 15: 订单管理
- 后端: `OrderController` + `OrderService`, 创建订单时更新 customer 消费统计
- 前端: `views/sales/OrderList.vue` + `OrderForm.vue`

#### Task 16: 销售漏斗
- 后端: `GET /api/sales/funnel` 统计线索数→意向→成交 各阶段数量
- 前端: `views/sales/OrderList.vue` 顶部用 ECharts 漏斗图展示

#### Task 17: 回访管理
- 后端: `FollowUpController` CRUD, 支持制定回访计划+完成回访记录
- 后端创建回访计划时同时插入 `todo_task` 待办
- 前端: `views/sales/FollowUpList.vue` (回访列表+完成按钮)

#### Task 18: 投诉处理
- `follow_up` 表中 `type='投诉'` 区分
- 前端: `FollowUpList.vue` 中 tab 切换(回访/投诉) + 状态流转(待处理→处理中→已解决)

---

### 阶段五: 数据分析与员工管理

#### Task 19: 商业智能分析页
- 后端: `AnalyticsController`
  - `GET /api/analytics/sales` → 近12月月销售额趋势
  - `GET /api/analytics/customers` → 近12月客户增长趋势 + 等级分布
  - `GET /api/analytics/products` → 商品销量TOP10
- 前端: `views/analytics/Analytics.vue`
  - ECharts折线图(销售额趋势) + 柱状图(客户增长) + 饼图(等级分布) + 横向柱状图(商品排行)

#### Task 20: 员工业绩排行
- 后端: `GET /api/employees/performance` 按月查业绩，含排行榜
- 前端: `views/employee/Performance.vue` 排行榜表格 + 目标完成率进度条

#### Task 21: 待办提醒
- 后端: `TodoController` + 定时任务每日生成待办 (过期线索/计划回访/即将到期的活动)
- 前端: `views/employee/TodoList.vue` (待办列表+完成标记)
- 首页 `Dashboard.vue` 右上角显示未读待办数(badge)

---

### 阶段六: 集成与收尾

#### Task 22: 首页仪表盘
- `views/Dashboard.vue`: 顶部4个统计卡片(今日新增客户/本月销售额/待跟进线索/待处理回访)
- 下方 ECharts 最近7天销售额趋势折线图 + 近30天客户增长柱状图
- 后端: `GET /api/dashboard/stats` 聚合返回以上4个数字

#### Task 23: 前后端联调 & 路由守卫
- `router/index.js`: 配置路由守卫，未登录跳/login
- 所有页面与后端联调，验证API返回数据格式
- 处理加载状态(el-skeleton)、空状态(el-empty)、错误提示(el-message)

#### Task 24: 最终验证
- 完整业务流程走通: 录入客户 → 转为会员 → 创建订单 → 发优惠券 → 记录回访 → 查看分析图表
- 验证公海池流转: 员工离职 → 名下客户回流公海 → 其他员工领取

---

## 开发顺序建议

按依赖关系推进，每完成一个模块即可独立验证：

```
环境搭建 (Task 1-5)
    ↓
客户管理 (Task 6-8) ← 最核心，其他模块依赖客户数据
    ↓
会员管理 (Task 9-11) ← 依赖客户模块
    ↓
销售管理 (Task 15-18) ← 依赖客户模块
    ↓
营销管理 (Task 12-14) ← 依赖客户+会员模块
    ↓
数据分析 (Task 19) + 员工管理 (Task 20-21) ← 依赖销售数据
    ↓
仪表盘 (Task 22) + 联调 (Task 23-24)
```

---

## 关键约定

- **前端:** 使用 Composition API (`<script setup>`), Element Plus 中文配置, Axios 统一拦截
- **后端:** 统一返回 `Result<T>` 包装, 异常统一处理, MyBatis-Plus 代码生成器快速生成entity/mapper
- **数据库:** 所有表包含 `created_at`/`updated_at` 时间戳, 逻辑删除用 `is_deleted` 字段
- **安全:** 密码 BCrypt 加密, JWT 24h过期, 接口按角色鉴权(管理员/普通员工)

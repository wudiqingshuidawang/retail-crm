# 零售业 CRM 系统

基于名创优品零售场景设计的客户关系管理系统，覆盖客户管理、销售订单、会员积分、营销活动、员工绩效等核心业务模块。

## 技术栈

**后端**
- Java 17 + Spring Boot 3.2.5
- MyBatis-Plus 3.5.7
- Spring Security + JWT 认证
- MySQL

**前端**
- Vue 3 + Vite 8
- Element Plus（UI 组件库）
- Pinia（状态管理）
- ECharts（数据可视化）
- Axios（HTTP 请求）

## 功能模块

| 模块 | 说明 |
|------|------|
| 客户管理 | 客户 CRUD、客户详情、公海池（线索池） |
| 销售订单 | 订单管理、订单明细 |
| 跟进记录 | 客户跟进记录管理 |
| 会员管理 | 会员注册、积分记录 |
| 营销活动 | 活动创建与管理 |
| 优惠券 | 优惠券发放、使用、核销 |
| 员工绩效 | 绩效排名展示 |
| 待办事项 | 任务提醒管理 |
| 数据仪表盘 | 销售趋势、客户分布等图表 |

## 项目结构

```
crm-latest/
├── crm-backend/                # Spring Boot 后端
│   ├── src/main/java/com/crm/module/
│   │   ├── auth/               # 认证模块
│   │   ├── customer/           # 客户模块
│   │   ├── sales/              # 销售模块
│   │   ├── member/             # 会员模块
│   │   ├── marketing/          # 营销模块
│   │   ├── employee/           # 员工模块
│   │   └── analytics/          # 数据分析模块
│   └── src/main/resources/
│       ├── application.yml     # 应用配置
│       └── db/
│           ├── init.sql        # 建表语句
│           └── seed.sql        # 测试数据
├── crm-frontend/               # Vue 3 前端
│   └── src/
│       ├── views/              # 页面组件
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia 状态
│       └── api/                # API 请求封装
└── docs/                       # 项目文档
```

## 本地启动

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE crm_db CHARACTER SET utf8mb4;"

# 导入建表语句和测试数据
mysql -u root -p crm_db < crm-backend/src/main/resources/db/init.sql
mysql -u root -p crm_db < crm-backend/src/main/resources/db/seed.sql
```

### 2. 启动后端

```bash
cd crm-backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 3. 启动前端

```bash
cd crm-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 测试账号

seed.sql 中预置了测试用户，可直接登录使用。

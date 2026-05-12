# 医院专家挂号系统

基于 Spring Boot + Vue 3 + MySQL 的在线医院挂号系统，支持患者预约挂号、医生排班管理、管理员审核、AI 智能问诊等完整业务流程。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Axios | Vue 3.5 / Element Plus 2.14 |
| 后端 | Spring Boot + MyBatis-Plus + Spring Security + JWT | Spring Boot 3.2.5 / MyBatis-Plus 3.5.6 |
| 数据库 | MySQL | 8.0 |
| AI | DeepSeek-R1 (ModelScope API) | marked.js 渲染 Markdown |
| 部署 | Docker + Docker Compose | 容器化一键部署 |

## 功能概览

### 🔹 患者端

| 模块 | 功能 |
|------|------|
| 账号管理 | 注册（需管理员审批）、登录、修改密码、编辑个人信息 |
| 预约挂号 | 按科室/日期搜索号源 → 挂号 → 模拟支付，三步完成 |
| 时段限制 | 当日上午号源 7:00-9:00 可约，下午号源 14:00 前可约 |
| 取消预约 | 就诊前一日可取消，当日不可取消 |
| 挂号历史 | 按日期范围查询历史挂号记录 |
| 病历查看 | 查看医生为自己创建的病历 |
| **AI 智能问诊** | 基于 DeepSeek-R1 的医疗咨询助手，支持 Markdown 渲染 |
| 首页仪表盘 | 快速入口 + 个人信息管理 |

### 🔹 医生端

| 模块 | 功能 |
|------|------|
| 账号管理 | 注册、登录、改密码、编辑资料（医院/科室/职称/专长） |
| 发布出诊 | 单次排班 + 周期性排班（如每周一三五，自动滚动生成） |
| 编辑排班 | 修改已有排班信息，已发布排班编辑后自动回到待审核状态 |
| 视图切换 | 列表视图（10 条/页）+ 日历卡片视图（30 条/页） |
| 病历管理 | 按姓名/身份证号查询患者，新增、编辑病历 |
| 首页仪表盘 | 出诊管理入口 + 个人信息管理 |

### 🔹 管理员端

| 模块 | 功能 |
|------|------|
| 账号管理 | 内置 admin 账户，首次登录强制修改密码 |
| 用户管理 | 审批/拒绝注册、编辑/删除用户、批量审批 |
| 自动审批注册 | 可独立开关"自动通过患者注册"和"自动通过医生注册" |
| 出诊审核 | 通过/拒绝/批量审核，拒绝时可填写原因 |
| 自动审核排班 | N 天内 + 号源数 ≤ M 的非周期性排班自动通过，阈值可在线配置 |
| 操作日志 | 查看所有管理员操作审计记录 |
| 首页仪表盘 | 各管理模块入口 |

## 快速开始

### 方式一：Docker 一键部署（推荐新手）

> 适用于没有安装 JDK/Maven/Node/MySQL 的设备

**前提**：安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```bash
# 双击运行
setup.bat
```

脚本自动完成：检查 Docker → 构建镜像 → 启动 MySQL + 后端 + 前端 → 打开浏览器。

访问地址：**http://localhost**

常用 Docker 命令：

```bash
docker compose logs       # 查看日志
docker compose down       # 停止服务（保留数据库数据）
docker compose down -v    # 停止服务并清空数据
docker compose restart    # 重启服务
```

### 方式二：本地开发环境

> 适用于已安装 JDK 21 + Maven + Node.js + MySQL 的设备

**前提**：

| 软件 | 版本要求 | 配置 |
|------|----------|------|
| JDK | 21 | `%USERPROFILE%\.jdks\jdk-21` |
| Maven | 3.9+ | `%USERPROFILE%\.m2\apache-maven-3.9.8` |
| Node.js | 18+ | npm 可用 |
| MySQL | 8.0 | 端口 3306，root 密码 123456 |

```bash
# 1. 初始化数据库
mysql -u root -p123456 < sql/init.sql

# 2. 双击运行
start.bat
```

脚本自动完成：检查 MySQL → 初始化数据库 → 启动后端 → 启动前端 → 打开浏览器。

前端：**http://localhost:5173**
后端：**http://localhost:8080**

## 关闭服务

关掉所有弹出的命令行窗口即可

## 默认账户

启动脚本会自动从数据库读取最新管理员密码并显示。

| 角色 | 账号 | 初始密码 | 备注 |
|------|------|----------|------|
| 管理员 | `admin` | `admin` | 首次登录强制修改密码 |

> 修改密码后，下次运行启动脚本会显示新密码。

## 项目结构

```
Hospital_System/
├── start.bat                  # 本地开发一键启动
├── setup.bat                  # Docker 一键部署
├── docker-compose.yml         # Docker 编排
├── README.md
├── AI_api.txt                 # AI 配置文件
├── sql/
│   └── init.sql               # 数据库初始化脚本（含表结构 + admin 种子数据）
├── hospital-server/           # 后端 Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   ├── AI_api.txt
│   └── src/main/
│       ├── java/com/hospital/
│       │   ├── common/         # Result, BusinessException, GlobalExceptionHandler
│       │   ├── config/         # Security, MyBatisPlus, CORS, DataInitializer, AutoAuditConfig
│       │   ├── controller/     # REST 控制器
│       │   ├── dto/            # 请求/响应 DTO
│       │   ├── entity/         # 数据库实体
│       │   ├── enums/          # UserRole, UserStatus, ScheduleStatus 等枚举
│       │   ├── interceptor/    # 限流拦截器
│       │   ├── mapper/         # MyBatis-Plus Mapper（含自定义 SQL）
│       │   ├── security/       # JWT Token 生成、认证过滤器
│       │   ├── service/        # 业务接口 + 实现
│       │   └── task/           # 定时任务（周期性号源滚动生成）
│       └── resources/
│           ├── application.yml           # 主配置
│           ├── application-dev.yml       # 开发环境配置
│           └── application-docker.yml    # Docker 环境配置
└── hospital-web/              # 前端 Vue 3
    ├── Dockerfile
    ├── nginx.conf             # Docker 部署用 Nginx 配置
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/               # Axios 请求封装（分模块：auth, user, schedule, registration, ai）
        ├── router/            # 路由配置（含角色守卫）
        ├── stores/            # Pinia 状态管理
        ├── styles/            # 主题样式（Element Plus 变量覆盖）
        └── views/
            ├── Login.vue / Register.vue / MainLayout.vue
            ├── patient/       # 患者首页、挂号、我的挂号、病历、AI 问诊
            ├── doctor/        # 医生首页、出诊管理、病历管理
            └── admin/         # 管理员首页、用户管理、出诊审核、操作日志
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `users` | 用户基础信息（身份证号、姓名、角色、状态、密码等） |
| `doctor_profiles` | 医生专业信息（医院、科室、职称、专长） |
| `medical_records` | 患者病历 |
| `schedules` | 出诊排班（含周期性标记、乐观锁版本号） |
| `registrations` | 挂号记录（唯一约束：患者+排班） |
| `audit_logs` | 管理员操作审计日志 |
| `schedule_audits` | 排班审核记录 |

## AI 问诊配置

AI 聊天功能使用 ModelScope API，通过 `hospital-server/AI_api.txt` 配置：

```
URL：https://api-inference.modelscope.cn/v1
API Key：ms-xxxxxxxxxxxxxxxx
model：deepseek-ai/DeepSeek-R1-0528
```

- 系统提示词遵循中国医疗法规，强调"仅供参考，不能替代专业医疗诊断"
- 修改配置文件后重启服务即可生效，无需重新编译
- 回复内容以 Markdown 格式渲染，支持标题、列表、代码块等

## 安全设计

| 机制 | 说明 |
|------|------|
| 号源扣减 | 乐观锁（`version` 字段）+ 自定义 SQL 防并发 |
| 挂号/支付/取消 | 条件 UPDATE SQL（`WHERE status = 0`），防止并发重复操作 |
| 重约保护 | 插入前清理患者同号源的已取消旧记录，避免唯一键冲突 |
| 角色守卫 | 后端 Spring Security 权限控制 + 前端路由 `meta.role` 守卫 |
| 限流 | Spring Interceptor 内存限流（Nginx 备用） |
| 审计 | 管理员所有写操作自动记录 JSON 格式的 old/new value |
| JWT | 登出时 token 加入内存黑名单，过滤器拒绝已登出 token |

## 许可证

本项目仅用于学习和教育目的。

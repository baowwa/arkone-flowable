# ArkOne Flowable - 测序流程管理系统

基于 Spring Boot + Flowable + Vue3 的测序流程管理系统。

## 项目结构

```
arkone-flowable/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/arkone/flowable/
│   │   │   │       ├── controller/      # 控制器
│   │   │   │       ├── service/         # 服务层
│   │   │   │       ├── entity/          # 实体类
│   │   │   │       ├── repository/      # 数据访问层
│   │   │   │       └── FlowableApplication.java
│   │   │   └── resources/
│   │   │       ├── processes/           # 流程定义文件
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── api/             # API接口
│   │   ├── router/          # 路由配置
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
└── docs/                    # 文档
```

## 功能特性

### 1. 全质粒测序流程
- 支持3种样本类型分支：
  - 平板样本/沉菌、菌液样本：摇菌 → 核酸提取 → 样本前处理 → 文库构建 → 上机测序
  - 直抽菌液：核酸提取 → 样本前处理 → 文库构建 → 上机测序
  - 质粒核酸样本：样本前处理 → 文库构建 → 上机测序

### 2. PCR产物测序流程
- 支持2种样本类型分支：
  - PCR产物(原液)：样本前处理 → 文库构建 → 测序复合物制备及纯化 → 上机测序
  - PCR产物(已纯化)：文库构建 → 测序复合物制备及纯化 → 上机测序

## 技术栈

### 后端
- Java 21
- Spring Boot 3.2.2
- Flowable 7.0.1
- MySQL 8.0+
- Mybatis-Plus

### 前端
- Vue 3.4
- Element Plus 2.5
- Vue Router 4.2
- Pinia 2.1
- Vite 5.0

## 快速开始

### 后端启动

1. 创建数据库
```sql
CREATE DATABASE arkone_flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置
编辑 `backend/src/main/resources/application.yml`，配置数据库连接信息。

3. 启动后端
```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动。

### 前端启动

1. 安装依赖
```bash
cd frontend
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

前端服务将在 http://localhost:3000 启动。

## API接口

### 启动流程
- POST `/api/sequencing/plasmid/start` - 启动全质粒测序流程
- POST `/api/sequencing/pcr/start` - 启动PCR产物测序流程

### 任务管理
- POST `/api/sequencing/task/{taskId}/complete` - 完成任务
- GET `/api/sequencing/process/{processInstanceId}/tasks` - 获取流程任务列表
- GET `/api/sequencing/process/{processInstanceId}/data` - 获取流程数据

## 开发说明

### 流程定义
流程定义文件位于 `backend/src/main/resources/processes/`：
- `plasmid-sequencing.bpmn20.xml` - 全质粒测序流程
- `pcr-product.bpmn20.xml` - PCR产物测序流程

### 表单组件
表单组件位于 `frontend/src/components/forms/`：
- `ShakingForm.vue` - 摇菌表单
- `NucleicAcidExtractionForm.vue` - 核酸提取表单
- `SamplePretreatmentForm.vue` - 样本前处理表单
- `LibraryConstructionForm.vue` - 文库构建表单
- `ComplexPreparationForm.vue` - 测序复合物制备表单
- `SequencingForm.vue` - 上机测序表单

## 许可证

MIT

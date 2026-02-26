# ArkOne Flowable 前端项目

基于 Vue 3 + TypeScript + Element Plus 构建的测序流程管理系统(LIMS)前端应用。

## 技术栈

- **框架**: Vue 3.4+ (Composition API + `<script setup>`)
- **语言**: TypeScript 5.x
- **UI组件库**: Element Plus 2.5+
- **状态管理**: Pinia 3.x
- **路由**: Vue Router 4.x
- **HTTP客户端**: Axios 1.6+
- **公式计算**: mathjs 15.x
- **构建工具**: Vite 7.x
- **日期处理**: dayjs 1.11+

## 核心功能

### 1. 样本管理
- 样本列表(分页、筛选、排序)
- 样本详情查看
- 创建样本
- 更新样本状态
- 批量操作

### 2. 批量数据录入 (MVP核心功能)
- 96孔板批量数据录入
- Tab/Enter键盘导航
- 实时字段验证
- 公式字段自动计算
- 进度跟踪(已填写X/96)
- 草稿自动保存(LocalStorage)
- 批量提交

### 3. 板位图可视化
- 96孔板/48孔板可视化(Canvas渲染)
- 孔位状态显示(空/已占用/当前选中)
- 一键填充板号
- 点击孔位查看详情
- 缩放和导航功能

### 4. 流程管理
- 启动全质粒测序流程
- 启动PCR产物测序流程
- 任务列表
- 完成任务
- 批量完成任务

### 5. 动态表单引擎
- 根据节点字段定义动态渲染表单
- 支持多种字段类型(text/number/date/datetime/select/textarea/formula)
- 字段验证(必填、正则、范围)
- 公式字段自动计算

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── request.ts     # Axios配置
│   │   ├── sample.ts      # 样本API
│   │   ├── process.ts     # 流程API
│   │   ├── field.ts       # 字段定义API
│   │   └── project.ts     # 项目API
│   ├── assets/            # 资源文件
│   │   └── styles/        # 样式文件
│   ├── components/        # 公共组件
│   │   ├── AppLayout.vue  # 布局组件
│   │   └── DynamicForm.vue # 动态表单组件
│   ├── router/            # 路由配置
│   │   └── index.ts
│   ├── stores/            # Pinia状态管理
│   │   ├── sample.ts      # 样本Store
│   │   └── task.ts        # 任务Store
│   ├── types/             # TypeScript类型定义
│   │   └── index.ts
│   ├── utils/             # 工具函数
│   │   └── index.ts       # 公式计算、验证、存储等
│   ├── views/             # 页面组件
│   │   ├── batch/         # 批量录入
│   │   │   └── BatchEntry.vue
│   │   ├── plate/         # 板位图
│   │   │   └── PlateMap.vue
│   │   ├── project/       # 项目管理
│   │   │   └── ProjectList.vue
│   │   ├── sample/        # 样本管理
│   │   │   ├── SampleList.vue
│   │   │   ├── SampleDetail.vue
│   │   │   └── SampleCreate.vue
│   │   ├── task/          # 任务管理
│   │   │   ├── TaskList.vue
│   │   │   └── TaskDetail.vue
│   │   └── NotFound.vue
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── .env.development       # 开发环境配置
├── .env.production        # 生产环境配置
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 快速开始

### 前置要求

- Node.js >= 18.x
- npm >= 9.x 或 pnpm >= 8.x

### 安装依赖

```bash
cd frontend
npm install
```

### 开发环境运行

```bash
npm run dev
```

应用将在 http://localhost:3000 启动

### 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist/` 目录

### 预览生产构建

```bash
npm run preview
```

## 环境配置

### 开发环境 (.env.development)

```
VITE_API_BASE_URL=http://localhost:8080/api
```

### 生产环境 (.env.production)

```
VITE_API_BASE_URL=https://api.example.com/api
```

## API接口

后端API基础地址: `http://localhost:8080/api`

### 样本管理
- `GET /samples` - 获取样本列表
- `GET /samples/:id` - 获取样本详情
- `POST /samples` - 创建样本
- `PUT /samples/:id` - 更新样本
- `DELETE /samples/:id` - 删除样本

### 批量操作
- `POST /batch/samples` - 批量创建样本(最多500条)
- `PUT /batch/samples/status` - 批量更新状态
- `POST /batch/tasks/complete` - 批量完成任务

### 流程管理
- `POST /process/start` - 启动流程
- `GET /tasks` - 获取任务列表
- `POST /tasks/:taskId/complete` - 完成任务

### 字段定义
- `GET /fields/nodes/:nodeId` - 获取节点字段定义

## 关键特性说明

### 1. 批量数据录入

批量录入组件支持96个样本的快速数据录入:

- **键盘导航**: Tab键切换字段,Enter键跳到下一行
- **实时验证**: 输入时即时验证,错误字段红色高亮
- **公式计算**: 依赖字段变化时自动重新计算公式字段
- **草稿保存**: 每5秒自动保存到LocalStorage
- **进度跟踪**: 实时显示已完成样本数量

### 2. 板位图可视化

使用Canvas API渲染96孔板:

- **孔位配置**: 8行×12列,每个孔位40×40px
- **状态颜色**: 空闲(白色)、已使用(蓝色)、选中(绿色)、异常(橙色)
- **交互功能**: 点击孔位查看详情,缩放,查找空位
- **一键填充**: 输入板号后自动填充所有96个孔位

### 3. 公式计算

使用mathjs库进行安全的公式计算:

- **白名单函数**: 仅支持 max/min/round/abs/ceil/floor
- **长度限制**: 公式最长500字符
- **变量引用**: 使用 `{field_code}` 引用其他字段
- **实时计算**: 依赖字段变化时自动更新

### 4. 字段验证

支持多种验证规则:

- **必填验证**: required字段不能为空
- **数值范围**: min/max限制
- **正则表达式**: pattern验证
- **自定义消息**: 可配置错误提示

## 开发规范

### 代码风格

- 使用 TypeScript 严格模式
- 使用 Composition API + `<script setup>`
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具链相关
```

## 常见问题

### 1. API请求失败

检查后端服务是否启动: `http://localhost:8080`

### 2. 公式计算不生效

确保公式语法正确,且只使用白名单函数

### 3. 草稿保存失败

检查浏览器LocalStorage是否可用

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 相关文档

- [产品需求文档](../docs/产品需求文档.md)
- [技术设计文档](../docs/技术设计文档.md)
- [UI设计方案](../docs/UI设计方案.md)
- [原型文件](../prototypes/)

## License

Copyright © 2026 ArkOne

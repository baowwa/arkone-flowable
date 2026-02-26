# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

ArkOne Flowable 是一个基于 Spring Boot + Flowable + Vue3 构建的测序流程管理系统(LIMS)。系统通过可配置的工作流、动态表单字段和自动化数据验证来管理样本全生命周期追踪。

**当前状态**: 开发前阶段。需求、架构和原型已完成,可以开始实施。

## 项目结构

```
arkone-flowable/
├── docs/                    # 核心归档文档
│   ├── 竞品分析报告.md        # 竞品分析
│   ├── 产品需求文档.md        # 产品需求(含批量录入、MVP范围)
│   ├── UI设计方案.md         # UI设计方案
│   ├── 技术设计文档.md        # 技术设计(架构、API、数据库、安全)
│   └── archive/            # 过程文档归档
├── prototypes/              # 高保真HTML原型
│   ├── batch-data-entry.html  # 批量数据录入(96个样本)
│   ├── plate-map.html         # 96/48孔板可视化
│   └── [其他原型]
├── backend/                 # Spring Boot后端(待创建)
└── frontend/                # Vue3前端(待创建)
```

## 关键架构决策

### P0决策(全部已批准 - 见技术设计文档.md)

1. **公式引擎**: 使用 Aviator(不是 JavaScript ScriptEngine)以确保安全
   - 仅白名单函数: max/min/round/abs/ceil/floor
   - 500字符限制,1秒超时
   - 实现方案: 见技术设计文档.md第6.1节

2. **加密方式**: 使用 AES/GCM(不是 AES/ECB)
   - 随机IV生成
   - 实现方案: 见技术设计文档.md第8.2节

3. **事务一致性**: Flowable + 业务数据在同一事务中
   - 同数据源,同本地事务
   - `process_node_data`保存 + `taskService.complete`在单个@Transactional中

4. **批量操作**: 默认"部分成功"模式
   - 支持`atomic=true`实现"全成功或全失败"
   - 每批最多500条
   - 返回逐条结果

5. **样本状态机**: 6种状态及定义的转换规则
   - 状态: pending/in_progress/paused/exception/completed/cancelled
   - 转换矩阵见产品需求文档.md

6. **版本管理**: 旧实例继续使用旧版本
   - 新实例使用最新版本
   - 在`field_snapshot` JSONB列中存储字段快照

## 技术栈

### 后端(待实现)
- Spring Boot 3.2.x
- Flowable 7.0.x
- MyBatis-Plus 3.5.x
- PostgreSQL 15+ (使用JSONB存储动态字段)
- Redis 7.x (缓存、会话)
- Aviator 5.4.1 (公式引擎 - 必需)

### 前端(待实现)
- Vue 3.4.x + TypeScript 5.x
- Element Plus 2.5.x
- bpmn-js 17.x (MVP阶段使用默认UI)
- Pinia (状态管理)
- Canvas API (板位图渲染)

## 关键实现要求

### 安全整改(必须首先完成)
1. 用Aviator替换JavaScript ScriptEngine
2. 用AES/GCM替换AES/ECB
3. 添加数据库唯一约束: `UNIQUE(container_id, position, deleted=false)`

### 批量数据录入(MVP功能)
**背景**: 96孔板有96个样本。逐个录入效率低下(需2-3小时)。

**必需功能**:
1. **表格批量录入**(batch-data-entry.html原型已存在)
   - 在表格中显示全部96个样本
   - Tab/Enter键盘导航
   - 实时验证
   - 自动计算公式字段
   - 进度跟踪(已填写X/96)
   - 草稿自动保存

2. **板位图一键填充**(plate-map.html原型已存在)
   - 输入一次板号
   - 自动填充全部96个孔位
   - 10秒 vs 30分钟(效率提升99%)

**效率目标**: 96个样本从2-3小时 → 30分钟(效率提升90%+)

### 数据库设计

核心表(见技术设计文档.md第2节):
- `lims_sample` - 样本主数据
- `lims_process_node_data` - 节点数据(JSONB存储动态字段 + field_snapshot)
- `lims_field_definition` - 字段配置
- `lims_container` - 容器/板管理
- `lims_audit_log` - 审计日志(按月分区)

**重要**: 在`lims_process_node_data`表中添加`field_snapshot JSONB`列用于版本管理。

## 工作流定义

### 全质粒测序(plasmid-sequencing.bpmn20.xml)
样本类型分支到不同节点:
- 平板/沉菌样本: 摇菌 → 核酸提取 → 样本前处理 → 文库构建 → 上机测序
- 直抽菌液: 核酸提取 → 样本前处理 → 文库构建 → 上机测序
- 质粒核酸: 样本前处理 → 文库构建 → 上机测序

### PCR产物测序(pcr-product.bpmn20.xml)
- PCR产物(原液): 样本前处理 → 文库构建 → 测序复合物制备 → 上机测序
- PCR产物(已纯化): 文库构建 → 测序复合物制备 → 上机测序

## API接口

完整API定义见技术设计文档.md第5.2-5.9节:

### 核心API(MVP)
- POST `/samples` - 创建样本
- GET `/samples` - 样本列表
- GET `/samples/{id}` - 样本详情
- POST `/process/tasks/{taskId}/complete` - 完成任务
- GET `/process/nodes/{nodeId}/fields` - 获取字段定义

### 批量API(MVP必需)
- POST `/samples/batch` - 批量创建(最多500条)
- PUT `/samples/batch/status` - 批量更新状态
- POST `/process/tasks/batch/complete` - 批量完成任务

### 错误码
统一错误码定义见技术设计文档.md第5.9节:
- SAMPLE_NOT_FOUND, DUPLICATE_SAMPLE_NAME, POSITION_OCCUPIED等

## 多代理协作

本项目使用专业的Claude Code代理(见claudecode/多角色协作使用说明.md):

**可用代理**:
- business-analyst (业务分析师)
- product-manager (产品经理)
- ui-designer (UI设计师)
- architect-reviewer (架构审查师)
- backend-developer (后端开发)
- frontend-developer (前端开发)
- 测试工程师 (test engineer)

**使用方式**: "让 [代理名] [任务]" 或 "协调 [代理们] 完成 [任务]"

## 开发工作流

### 第1阶段: 安全整改与核心API(第1周)
1. 实现Aviator公式引擎(4小时)
2. 实现AES/GCM加密(2小时)
3. 添加数据库约束(1小时)
4. 实现核心API(3-5天)

### 第2阶段: 核心功能(第2-4周)
1. 样本管理
2. 动态表单引擎
3. 工作流配置

### 第3阶段: 批量录入(第5-8周)
1. 表格批量录入
2. 板位图可视化
3. 一键填充

### 第4阶段: 集成与测试(第9-12周)
1. 前后端集成
2. 端到端测试
3. 性能优化

## 重要注意事项

### 公式计算
- **后端**: 使用Aviator(不是ScriptEngine)
- **前端**: 使用mathjs,限制函数
- 保持前后端公式语法一致

### 并发控制
- 使用数据库唯一约束进行孔位分配
- 使用乐观锁(`version`字段)控制样本状态
- 失败时重试最多3次,间隔100ms

### 批量操作
- 默认: 部分成功(atomic=false)
- 可选: 全成功或全失败(atomic=true)
- 始终返回逐条结果
- 每条操作写入审计日志

### 版本管理
- 旧实例继续使用旧版本(Flowable原生支持)
- 新实例使用最新版本
- 实例启动时存储字段快照
- 使用快照显示历史数据

## 参考文档

**核心归档文档**(位于 docs/):
1. `docs/竞品分析报告.md` - 竞品分析
2. `docs/产品需求文档.md` - 产品需求(含批量录入、业务规则、MVP范围)
3. `docs/UI设计方案.md` - UI设计方案
4. `docs/技术设计文档.md` - 技术设计(含���构、API、数据库、安全方案、P0决策)

**原型**(UI参考):
- `prototypes/batch-data-entry.html` - 批量录入交互
- `prototypes/plate-map.html` - 板位图可视化
- `prototypes/README.md` - 原型文档

**过程文档**(如需追溯):
- `docs/archive/` - 归档的过程文档(P0决策、需求基线、会议纪要等)

## 快速命令

### 查看原型
```bash
cd prototypes
python3 -m http.server 8888
open http://localhost:8888
```

### 后端(创建后)
```bash
cd backend
mvn spring-boot:run
```

### 前端(创建后)
```bash
cd frontend
npm install
npm run dev
```

## 常见陷阱(必须避免)

1. **不要**使用JavaScript ScriptEngine - 使用Aviator
2. **不要**使用AES/ECB - 使用AES/GCM
3. **不要**忘记field_snapshot列用于版本管理
4. **不要**只实现逐个录入 - 批量录入是MVP必需功能
5. **不要**跳过事务边界 - Flowable + 业务数据必须原子化
6. **不要**允许非法状态转换 - 遵循状态机规则(需求基线13.1节)

## 获取帮助

- 需求问题: 见`docs/产品需求文档.md`
- 技术问题: 见`docs/技术设计文档.md`
- UI/UX问题: 见`prototypes/`和`docs/UI设计方案.md`
- 竞品参考: 见`docs/竞品分析报告.md`
- 多代理帮助: 见`claudecode/多角色协作使用说明.md`
- 历史追溯: 见`docs/archive/`

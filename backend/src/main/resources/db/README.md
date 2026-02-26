# ArkOne LIMS 数据库初始化脚本

## 概述

本目录包含 ArkOne LIMS 系统的 PostgreSQL 数据库初始化脚本，用于创建核心业务表、索引和初始数据。

## 脚本说明

### 01_init_schema.sql
创建所有核心业务表结构：
- `lims_project` - 检验项目表
- `lims_container` - 容器表
- `lims_sample` - 样本表（包含容器位置管理和乐观锁）
- `lims_process_node_data` - 流程节点数据表（JSONB动态字段）
- `lims_field_definition` - 字段定义表
- `lims_audit_log` - 审计日志表（按月分区）

特性：
- UUID 主键
- 逻辑删除（deleted 字段）
- 乐观锁（version 字段）
- 审计字段（created_at, updated_at, created_by, updated_by）
- 自动更新时间戳触发器
- 外键约束和级联删除

### 02_init_indexes.sql
创建性能优化索引：
- B-tree 索引：用于等值查询、范围查询和排序
- GIN 索引：用于 JSONB 字段的全文检索
- 复合索引：优化多条件查询
- 唯一约束：`UNIQUE(container_id, position) WHERE deleted = false`
- 条件索引：`WHERE deleted = false` 减少索引大小

### 03_init_data.sql
插入系统初始数据：
- 3个默认检验项目（全质粒测序、PCR检测、NGS测序）
- 6个流程节点的字段定义（40+个字段）
- 字段验证规则和选项配置
- 4个默认容器（96孔板和48深孔板）

## 执行顺序

**必须按以下顺序执行脚本：**

```bash
# 1. 创建表结构
psql -U postgres -d arkone_lims -f 01_init_schema.sql

# 2. 创建索引
psql -U postgres -d arkone_lims -f 02_init_indexes.sql

# 3. 插入初始数据
psql -U postgres -d arkone_lims -f 03_init_data.sql
```

## 使用 Docker Compose

如果使用 Docker Compose 部署，可以将脚本挂载到容器：

```yaml
services:
  postgres:
    image: postgres:15-alpine
    volumes:
      - ./backend/src/main/resources/db:/docker-entrypoint-initdb.d
    environment:
      POSTGRES_DB: arkone_lims
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
```

容器启动时会自动按文件名顺序执行 `/docker-entrypoint-initdb.d` 目录下的 SQL 脚本。

## 数据库要求

- PostgreSQL 12+
- 启用 `pgcrypto` 扩展（用于 UUID 生成）

## 核心表设计

### lims_sample（样本表）

关键字段：
- `container_id` + `position`：容器位置管理
- `version`：乐观锁，防止并发更新冲突
- `parent_sample_id`：样本关系追溯
- `process_instance_id`：关联 Flowable 流程实例

唯一约束：
```sql
UNIQUE(container_id, position) WHERE deleted = false
```

### lims_process_node_data（节点数据表）

JSONB 字段示例：
```json
{
  "48深孔板号": "P001",
  "48深孔板孔号": "A01",
  "摇菌开始时间": "2026-02-24T09:45:00",
  "核酸浓度": 125.5,
  "补ddH₂O": 10.2
}
```

支持的查询：
```sql
-- 查询特定字段
SELECT * FROM lims_process_node_data
WHERE data->>'核酸浓度' > '100';

-- JSONB 包含查询
SELECT * FROM lims_process_node_data
WHERE data @> '{"48深孔板号": "P001"}';
```

### lims_audit_log（审计日志表）

分区策略：
- 按月分区（RANGE 分区）
- 自动继承主表索引
- 查询时需包含 `created_at` 条件以利用分区裁剪

添加新分区：
```sql
CREATE TABLE lims_audit_log_2027_01 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2027-01-01') TO ('2027-02-01');
```

## 索引优化

### GIN 索引使用场景

```sql
-- 全文检索
SELECT * FROM lims_process_node_data
WHERE data @> '{"sample_type": "plate"}';

-- 字段存在性检查
SELECT * FROM lims_process_node_data
WHERE data ? '核酸浓度';
```

### 表达式索引使用场景

```sql
-- 利用表达式索引
SELECT * FROM lims_process_node_data
WHERE data->>'核酸浓度' > '100';
```

## 性能优化建议

1. **定期维护**
```sql
-- 更新统计信息
ANALYZE lims_sample;

-- 清理死元组
VACUUM ANALYZE lims_process_node_data;
```

2. **监控慢查询**
```sql
-- 启用慢查询日志
ALTER DATABASE arkone_lims SET log_min_duration_statement = 1000;
```

3. **检查索引使用情况**
```sql
-- 查看未使用的索引
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND indexname NOT LIKE '%_pkey';
```

## 数据迁移

如果需要从旧版本迁移，请参考以下步骤：

1. 备份现有数据
```bash
pg_dump -U postgres arkone_lims > backup.sql
```

2. 执行迁移脚本
```bash
psql -U postgres -d arkone_lims -f migration_v1_to_v2.sql
```

3. 验证数据完整性
```sql
SELECT COUNT(*) FROM lims_sample WHERE deleted = false;
```

## 故障排查

### 问题：UUID 生成失败
```
ERROR: function gen_random_uuid() does not exist
```

解决方案：
```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

### 问题：分区表查询慢
确保查询包含分区键：
```sql
-- 慢查询（全表扫描）
SELECT * FROM lims_audit_log WHERE user_id = 'user123';

-- 快速查询（分区裁剪）
SELECT * FROM lims_audit_log
WHERE user_id = 'user123'
  AND created_at >= '2026-02-01'
  AND created_at < '2026-03-01';
```

### 问题：唯一约束冲突
```
ERROR: duplicate key value violates unique constraint "uk_sample_container_position"
```

检查是否有未删除的记录占用该位置：
```sql
SELECT * FROM lims_sample
WHERE container_id = 'xxx'
  AND position = 'A01'
  AND deleted = false;
```

## 联系方式

如有问题，请联系技术团队或提交 Issue。

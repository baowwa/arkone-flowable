-- ============================================================================
-- ArkOne LIMS 数据库初始化脚本 - 索引
-- 版本: v1.0
-- 创建日期: 2026-02-25
-- 描述: 创建性能优化索引，包括B-tree索引、GIN索引和唯一约束
-- ============================================================================

-- ============================================================================
-- 1. lims_project 表索引
-- ============================================================================
CREATE INDEX idx_project_code ON lims_project(project_code) WHERE deleted = false;
CREATE INDEX idx_project_status ON lims_project(status) WHERE deleted = false;
CREATE INDEX idx_project_created_at ON lims_project(created_at DESC);

-- ============================================================================
-- 2. lims_container 表索引
-- ============================================================================
CREATE INDEX idx_container_code ON lims_container(container_code) WHERE deleted = false;
CREATE INDEX idx_container_type ON lims_container(container_type) WHERE deleted = false;
CREATE INDEX idx_container_status ON lims_container(status) WHERE deleted = false;
CREATE INDEX idx_container_created_at ON lims_container(created_at DESC);

-- ============================================================================
-- 3. lims_sample 表索引
-- ============================================================================
-- 基础索引
CREATE INDEX idx_sample_code ON lims_sample(sample_code) WHERE deleted = false;
CREATE INDEX idx_sample_type ON lims_sample(sample_type) WHERE deleted = false;
CREATE INDEX idx_sample_status ON lims_sample(status) WHERE deleted = false;
CREATE INDEX idx_sample_project ON lims_sample(project_id) WHERE deleted = false;
CREATE INDEX idx_sample_container ON lims_sample(container_id) WHERE deleted = false;
CREATE INDEX idx_sample_parent ON lims_sample(parent_sample_id) WHERE deleted = false;
CREATE INDEX idx_sample_process_instance ON lims_sample(process_instance_id) WHERE deleted = false;
CREATE INDEX idx_sample_current_node ON lims_sample(current_node_id) WHERE deleted = false;
CREATE INDEX idx_sample_created_at ON lims_sample(created_at DESC);

-- 复合索引：项目+状态（用于项目样本列表查询）
CREATE INDEX idx_sample_project_status ON lims_sample(project_id, status) WHERE deleted = false;

-- 复合索引：容器+位置（用于容器管理查询）
CREATE INDEX idx_sample_container_position ON lims_sample(container_id, position) WHERE deleted = false;

-- 唯一约束：同一容器内位置唯一（排除已删除记录）
CREATE UNIQUE INDEX uk_sample_container_position ON lims_sample(container_id, position)
    WHERE deleted = false AND container_id IS NOT NULL;

-- 复合索引：样本类型+状态（用于统计查询）
CREATE INDEX idx_sample_type_status ON lims_sample(sample_type, status) WHERE deleted = false;

-- ============================================================================
-- 4. lims_process_node_data 表索引
-- ============================================================================
-- 基础索引
CREATE INDEX idx_node_data_sample ON lims_process_node_data(sample_id) WHERE deleted = false;
CREATE INDEX idx_node_data_node ON lims_process_node_data(node_id) WHERE deleted = false;
CREATE INDEX idx_node_data_status ON lims_process_node_data(status) WHERE deleted = false;
CREATE INDEX idx_node_data_created_at ON lims_process_node_data(created_at DESC);

-- 复合索引：样本+节点（用于查询样本在特定节点的数据）
CREATE INDEX idx_node_data_sample_node ON lims_process_node_data(sample_id, node_id) WHERE deleted = false;

-- GIN索引：支持JSONB字段全文检索
CREATE INDEX idx_node_data_gin ON lims_process_node_data USING GIN (data);
CREATE INDEX idx_node_data_field_snapshot_gin ON lims_process_node_data USING GIN (field_snapshot);

-- B-tree索引：高频查询的JSONB字段（根据业务需求添加）
-- 示例：核酸浓度字段
CREATE INDEX idx_node_data_concentration ON lims_process_node_data ((data->>'核酸浓度'))
    WHERE deleted = false AND data ? '核酸浓度';

-- 示例：条形码字段
CREATE INDEX idx_node_data_barcode ON lims_process_node_data ((data->>'barcode'))
    WHERE deleted = false AND data ? 'barcode';

-- 示例：48深孔板号字段
CREATE INDEX idx_node_data_plate_code ON lims_process_node_data ((data->>'48深孔板号'))
    WHERE deleted = false AND data ? '48深孔板号';

-- ============================================================================
-- 5. lims_field_definition 表索引
-- ============================================================================
-- 基础索引
CREATE INDEX idx_field_node ON lims_field_definition(node_id) WHERE deleted = false;
CREATE INDEX idx_field_type ON lims_field_definition(field_type) WHERE deleted = false;
CREATE INDEX idx_field_display_order ON lims_field_definition(node_id, display_order) WHERE deleted = false;

-- GIN索引：支持验证规则和选项的JSONB查询
CREATE INDEX idx_field_validation_rule_gin ON lims_field_definition USING GIN (validation_rule);
CREATE INDEX idx_field_options_gin ON lims_field_definition USING GIN (options);

-- ============================================================================
-- 6. lims_audit_log 表索引（分区表索引）
-- ============================================================================
-- 基础索引
CREATE INDEX idx_audit_user ON lims_audit_log(user_id);
CREATE INDEX idx_audit_action ON lims_audit_log(action);
CREATE INDEX idx_audit_entity ON lims_audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON lims_audit_log(created_at DESC);

-- 复合索引：用户+操作类型+时间（用于用户行为分析）
CREATE INDEX idx_audit_user_action_time ON lims_audit_log(user_id, action, created_at DESC);

-- 复合索引：实体类型+实体ID+时间（用于实体变更历史查询）
CREATE INDEX idx_audit_entity_time ON lims_audit_log(entity_type, entity_id, created_at DESC);

-- GIN索引：支持JSONB字段的变更内容检索
CREATE INDEX idx_audit_old_value_gin ON lims_audit_log USING GIN (old_value);
CREATE INDEX idx_audit_new_value_gin ON lims_audit_log USING GIN (new_value);

-- ============================================================================
-- 索引使用说明
-- ============================================================================
--
-- 1. B-tree 索引：
--    - 用于等值查询、范围查询、排序操作
--    - WHERE deleted = false 条件索引可减少索引大小，提高查询效率
--    - 复合索引顺序：高选择性字段在前，低选择性字段在后
--
-- 2. GIN 索引：
--    - 用于 JSONB 字段的全文检索和包含查询
--    - 支持 @>, ?, ?&, ?| 等操作符
--    - 索引维护成本较高，适合读多写少的场景
--
-- 3. 唯一约束：
--    - uk_sample_container_position: 确保同一容器内位置唯一
--    - uk_node_field: 确保同一节点内字段名唯一
--
-- 4. 分区表索引：
--    - 审计日志表按月分区，每个分区自动继承主表索引
--    - 查询时需包含 created_at 条件以利用分区裁剪
--
-- 5. 性能优化建议：
--    - 定期执行 VACUUM ANALYZE 更新统计信息
--    - 监控慢查询日志，根据实际查询模式调整索引
--    - 对于高频 JSONB 字段查询，考虑添加表达式索引
--    - 定期检查索引使用情况，删除未使用的索引
--
-- ============================================================================

-- ============================================================================
-- ArkOne LIMS 数据库初始化脚本 - 表结构
-- 版本: v1.0
-- 创建日期: 2026-02-25
-- 描述: 创建核心业务表，包含样本、项目、流程节点数据、字段定义、容器和审计日志
-- ============================================================================

-- 启用 UUID 扩展
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. 检验项目表 (lims_project)
-- 描述: 存储检验项目信息，每个项目关联一个 Flowable 流程定义
-- ============================================================================
CREATE TABLE lims_project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_code VARCHAR(50) UNIQUE NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    description TEXT,
    process_definition_id VARCHAR(64),
    status VARCHAR(20) DEFAULT 'active',
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE lims_project IS '检验项目表';
COMMENT ON COLUMN lims_project.id IS '主键UUID';
COMMENT ON COLUMN lims_project.project_code IS '项目编码，唯一标识';
COMMENT ON COLUMN lims_project.project_name IS '项目名称';
COMMENT ON COLUMN lims_project.description IS '项目描述';
COMMENT ON COLUMN lims_project.process_definition_id IS 'Flowable流程定义ID';
COMMENT ON COLUMN lims_project.status IS '项目状态: active-启用, inactive-停用';
COMMENT ON COLUMN lims_project.created_by IS '创建人';
COMMENT ON COLUMN lims_project.created_at IS '创建时间';
COMMENT ON COLUMN lims_project.updated_by IS '更新人';
COMMENT ON COLUMN lims_project.updated_at IS '更新时间';
COMMENT ON COLUMN lims_project.deleted IS '逻辑删除标记';

-- ============================================================================
-- 2. 容器表 (lims_container)
-- 描述: 存储实验容器信息，如96孔板、48深孔板等
-- ============================================================================
CREATE TABLE lims_container (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    container_code VARCHAR(50) UNIQUE NOT NULL,
    container_type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    used_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE lims_container IS '容器表';
COMMENT ON COLUMN lims_container.id IS '主键UUID';
COMMENT ON COLUMN lims_container.container_code IS '容器编码，唯一标识';
COMMENT ON COLUMN lims_container.container_type IS '容器类型: 96-well-plate, 48-deep-well-plate等';
COMMENT ON COLUMN lims_container.capacity IS '容器容量（孔位数）';
COMMENT ON COLUMN lims_container.used_count IS '已使用孔位数';
COMMENT ON COLUMN lims_container.status IS '容器状态: active-使用中, full-已满, archived-已归档';
COMMENT ON COLUMN lims_container.created_by IS '创建人';
COMMENT ON COLUMN lims_container.created_at IS '创建时间';
COMMENT ON COLUMN lims_container.updated_by IS '更新人';
COMMENT ON COLUMN lims_container.updated_at IS '更新时间';
COMMENT ON COLUMN lims_container.deleted IS '逻辑删除标记';

-- ============================================================================
-- 3. 样本表 (lims_sample)
-- 描述: 存储样本信息，支持容器位置管理和样本关系追溯
-- ============================================================================
CREATE TABLE lims_sample (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sample_code VARCHAR(50) UNIQUE NOT NULL,
    sample_name VARCHAR(200) NOT NULL,
    project_id UUID NOT NULL,
    sample_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    process_instance_id VARCHAR(64),
    current_node_id VARCHAR(100),
    container_id UUID,
    position VARCHAR(20),
    parent_sample_id UUID,
    version INT DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_sample_project FOREIGN KEY (project_id) REFERENCES lims_project(id),
    CONSTRAINT fk_sample_container FOREIGN KEY (container_id) REFERENCES lims_container(id),
    CONSTRAINT fk_sample_parent FOREIGN KEY (parent_sample_id) REFERENCES lims_sample(id)
);

COMMENT ON TABLE lims_sample IS '样本表';
COMMENT ON COLUMN lims_sample.id IS '主键UUID';
COMMENT ON COLUMN lims_sample.sample_code IS '样本编码，格式: AKYYYYMMDDnnn';
COMMENT ON COLUMN lims_sample.sample_name IS '样本名称';
COMMENT ON COLUMN lims_sample.project_id IS '所属项目ID';
COMMENT ON COLUMN lims_sample.sample_type IS '样本类型: plate-平板样本, liquid-直抽菌液, plasmid-质粒核酸';
COMMENT ON COLUMN lims_sample.status IS '样本状态: pending-待处理, in_progress-处理中, completed-已完成, failed-失败';
COMMENT ON COLUMN lims_sample.process_instance_id IS 'Flowable流程实例ID';
COMMENT ON COLUMN lims_sample.current_node_id IS '当前所在流程节点ID';
COMMENT ON COLUMN lims_sample.container_id IS '所在容器ID';
COMMENT ON COLUMN lims_sample.position IS '容器内位置，如A01, B12等';
COMMENT ON COLUMN lims_sample.parent_sample_id IS '父样本ID，用于样本分装追溯';
COMMENT ON COLUMN lims_sample.version IS '乐观锁版本号';
COMMENT ON COLUMN lims_sample.created_by IS '创建人';
COMMENT ON COLUMN lims_sample.created_at IS '创建时间';
COMMENT ON COLUMN lims_sample.updated_by IS '更新人';
COMMENT ON COLUMN lims_sample.updated_at IS '更新时间';
COMMENT ON COLUMN lims_sample.deleted IS '逻辑删除标记';

-- ============================================================================
-- 4. 流程节点数据表 (lims_process_node_data)
-- 描述: 存储每个流程节点的业务数据，使用JSONB存储动态字段
-- ============================================================================
CREATE TABLE lims_process_node_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sample_id UUID NOT NULL,
    node_id VARCHAR(100) NOT NULL,
    node_name VARCHAR(200) NOT NULL,
    data JSONB NOT NULL,
    field_snapshot JSONB,
    status VARCHAR(20) DEFAULT 'completed',
    version INT DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_node_data_sample FOREIGN KEY (sample_id) REFERENCES lims_sample(id) ON DELETE CASCADE
);

COMMENT ON TABLE lims_process_node_data IS '流程节点数据表';
COMMENT ON COLUMN lims_process_node_data.id IS '主键UUID';
COMMENT ON COLUMN lims_process_node_data.sample_id IS '样本ID';
COMMENT ON COLUMN lims_process_node_data.node_id IS '流程节点ID';
COMMENT ON COLUMN lims_process_node_data.node_name IS '流程节点名称';
COMMENT ON COLUMN lims_process_node_data.data IS '节点业务数据，JSONB格式存储动态字段';
COMMENT ON COLUMN lims_process_node_data.field_snapshot IS '字段定义快照，记录当时的字段配置';
COMMENT ON COLUMN lims_process_node_data.status IS '节点状态: pending-待处理, in_progress-处理中, completed-已完成';
COMMENT ON COLUMN lims_process_node_data.version IS '乐观锁版本号';
COMMENT ON COLUMN lims_process_node_data.created_by IS '创建人';
COMMENT ON COLUMN lims_process_node_data.created_at IS '创建时间';
COMMENT ON COLUMN lims_process_node_data.updated_by IS '更新人';
COMMENT ON COLUMN lims_process_node_data.updated_at IS '更新时间';
COMMENT ON COLUMN lims_process_node_data.deleted IS '逻辑删除标记';

-- ============================================================================
-- 5. 字段定义表 (lims_field_definition)
-- 描述: 定义每个流程节点的动态字段配置
-- ============================================================================
CREATE TABLE lims_field_definition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_label VARCHAR(200),
    field_type VARCHAR(50) NOT NULL,
    unit VARCHAR(50),
    required BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(500),
    validation_rule JSONB,
    formula VARCHAR(500),
    options JSONB,
    display_order INT DEFAULT 0,
    version INT DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,

    CONSTRAINT uk_node_field UNIQUE (node_id, field_name)
);

COMMENT ON TABLE lims_field_definition IS '字段定义表';
COMMENT ON COLUMN lims_field_definition.id IS '主键UUID';
COMMENT ON COLUMN lims_field_definition.node_id IS '流程节点ID';
COMMENT ON COLUMN lims_field_definition.field_name IS '字段名称（英文标识）';
COMMENT ON COLUMN lims_field_definition.field_label IS '字段标签（中文显示名）';
COMMENT ON COLUMN lims_field_definition.field_type IS '字段类型: text, number, date, datetime, select, checkbox等';
COMMENT ON COLUMN lims_field_definition.unit IS '单位，如ng/μL, ℃等';
COMMENT ON COLUMN lims_field_definition.required IS '是否必填';
COMMENT ON COLUMN lims_field_definition.default_value IS '默认值';
COMMENT ON COLUMN lims_field_definition.validation_rule IS '验证规则，JSONB格式: {type, min, max, pattern, message}';
COMMENT ON COLUMN lims_field_definition.formula IS '计算公式，用于自动计算字段';
COMMENT ON COLUMN lims_field_definition.options IS '选项配置，JSONB格式: {items: [{label, value}]}';
COMMENT ON COLUMN lims_field_definition.display_order IS '显示顺序';
COMMENT ON COLUMN lims_field_definition.version IS '乐观锁版本号';
COMMENT ON COLUMN lims_field_definition.created_by IS '创建人';
COMMENT ON COLUMN lims_field_definition.created_at IS '创建时间';
COMMENT ON COLUMN lims_field_definition.updated_by IS '更新人';
COMMENT ON COLUMN lims_field_definition.updated_at IS '更新时间';
COMMENT ON COLUMN lims_field_definition.deleted IS '逻辑删除标记';

-- ============================================================================
-- 6. 审计日志表 (lims_audit_log) - 分区表
-- 描述: 记录所有数据变更操作，按月分区存储
-- ============================================================================
CREATE TABLE lims_audit_log (
    id UUID DEFAULT gen_random_uuid(),
    user_id VARCHAR(100),
    user_name VARCHAR(200),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE lims_audit_log IS '审计日志表（按月分区）';
COMMENT ON COLUMN lims_audit_log.id IS '主键UUID';
COMMENT ON COLUMN lims_audit_log.user_id IS '操作用户ID';
COMMENT ON COLUMN lims_audit_log.user_name IS '操作用户名';
COMMENT ON COLUMN lims_audit_log.action IS '操作类型: CREATE, UPDATE, DELETE, LOGIN, LOGOUT等';
COMMENT ON COLUMN lims_audit_log.entity_type IS '实体类型: SAMPLE, PROJECT, CONTAINER等';
COMMENT ON COLUMN lims_audit_log.entity_id IS '实体ID';
COMMENT ON COLUMN lims_audit_log.old_value IS '变更前的值，JSONB格式';
COMMENT ON COLUMN lims_audit_log.new_value IS '变更后的值，JSONB格式';
COMMENT ON COLUMN lims_audit_log.ip_address IS '操作IP地址';
COMMENT ON COLUMN lims_audit_log.user_agent IS '用户代理信息';
COMMENT ON COLUMN lims_audit_log.created_at IS '操作时间';

-- 创建审计日志分区表（2026年2月-12月）
CREATE TABLE lims_audit_log_2026_02 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

CREATE TABLE lims_audit_log_2026_03 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');

CREATE TABLE lims_audit_log_2026_04 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');

CREATE TABLE lims_audit_log_2026_05 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE lims_audit_log_2026_06 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE TABLE lims_audit_log_2026_07 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE lims_audit_log_2026_08 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE TABLE lims_audit_log_2026_09 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

CREATE TABLE lims_audit_log_2026_10 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

CREATE TABLE lims_audit_log_2026_11 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

CREATE TABLE lims_audit_log_2026_12 PARTITION OF lims_audit_log
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- ============================================================================
-- 7. 更新时间触发器函数
-- 描述: 自动更新 updated_at 字段
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为所有表添加更新时间触发器
CREATE TRIGGER update_lims_project_updated_at BEFORE UPDATE ON lims_project
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lims_container_updated_at BEFORE UPDATE ON lims_container
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lims_sample_updated_at BEFORE UPDATE ON lims_sample
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lims_process_node_data_updated_at BEFORE UPDATE ON lims_process_node_data
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lims_field_definition_updated_at BEFORE UPDATE ON lims_field_definition
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

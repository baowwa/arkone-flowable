-- ============================================================================
-- ArkOne LIMS 数据库初始化脚本 - 初始数据
-- 版本: v1.0
-- 创建日期: 2026-02-25
-- 描述: 插入系统初始数据，包括默认项目和字段定义
-- ============================================================================

-- ============================================================================
-- 1. 插入默认检验项目
-- ============================================================================

-- 全质粒测序项目
INSERT INTO lims_project (id, project_code, project_name, description, process_definition_id, status, created_by, created_at)
VALUES
    (
        '00000000-0000-0000-0000-000000000001',
        'PROJ_PLASMID_SEQ',
        '全质粒测序',
        '全质粒测序流程，支持平板样本、直抽菌液、质粒核酸三种样本类型',
        'plasmid_sequencing',
        'active',
        'system',
        CURRENT_TIMESTAMP
    );

-- 其他项目示例（可根据实际需求添加）
INSERT INTO lims_project (id, project_code, project_name, description, status, created_by, created_at)
VALUES
    (
        '00000000-0000-0000-0000-000000000002',
        'PROJ_PCR',
        'PCR检测',
        'PCR检测流程',
        'active',
        'system',
        CURRENT_TIMESTAMP
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        'PROJ_NGS',
        'NGS测序',
        '二代测序流程',
        'active',
        'system',
        CURRENT_TIMESTAMP
    );

-- ============================================================================
-- 2. 插入字段定义 - 样本登记节点
-- ============================================================================
INSERT INTO lims_field_definition (node_id, field_name, field_label, field_type, required, display_order, created_by)
VALUES
    ('sample_register', 'sample_name', '样本名称', 'text', true, 1, 'system'),
    ('sample_register', 'sample_type', '样本类型', 'select', true, 2, 'system'),
    ('sample_register', 'sample_source', '样本来源', 'text', false, 3, 'system'),
    ('sample_register', 'remark', '备注', 'textarea', false, 4, 'system');

-- 样本类型选项配置
UPDATE lims_field_definition
SET options = '{
    "items": [
        {"label": "平板样本", "value": "plate"},
        {"label": "直抽菌液", "value": "liquid"},
        {"label": "质粒核酸", "value": "plasmid"}
    ]
}'::jsonb
WHERE node_id = 'sample_register' AND field_name = 'sample_type';

-- ============================================================================
-- 3. 插入字段定义 - 摇菌节点
-- ============================================================================
INSERT INTO lims_field_definition (node_id, field_name, field_label, field_type, unit, required, display_order, validation_rule, created_by)
VALUES
    ('task_shake', 'plate_48_code', '48深孔板号', 'text', NULL, true, 1, NULL, 'system'),
    ('task_shake', 'plate_48_position', '48深孔板孔号', 'text', NULL, true, 2, NULL, 'system'),
    ('task_shake', 'shake_start_time', '摇菌开始时间', 'datetime', NULL, true, 3, NULL, 'system'),
    ('task_shake', 'shake_end_time', '摇菌截止时间', 'datetime', NULL, true, 4, NULL, 'system'),
    ('task_shake', 'shake_duration', '摇菌时长', 'number', '小时', false, 5, NULL, 'system'),
    ('task_shake', 'shake_speed', '摇菌转速', 'number', 'rpm', false, 6, NULL, 'system'),
    ('task_shake', 'shake_temperature', '摇菌温度', 'number', '℃', false, 7, NULL, 'system');

-- 摇菌转速验证规则
UPDATE lims_field_definition
SET validation_rule = '{
    "type": "number",
    "min": 0,
    "max": 500,
    "message": "请输入0-500之间的转速值"
}'::jsonb
WHERE node_id = 'task_shake' AND field_name = 'shake_speed';

-- 摇菌温度验证规则
UPDATE lims_field_definition
SET validation_rule = '{
    "type": "number",
    "min": 20,
    "max": 40,
    "message": "请输入20-40℃之间的温度值"
}'::jsonb
WHERE node_id = 'task_shake' AND field_name = 'shake_temperature';

-- ============================================================================
-- 4. 插入字段定义 - 核酸提取节点
-- ============================================================================
INSERT INTO lims_field_definition (node_id, field_name, field_label, field_type, unit, required, display_order, validation_rule, created_by)
VALUES
    ('task_extract', 'extract_method', '提取方法', 'select', NULL, true, 1, NULL, 'system'),
    ('task_extract', 'extract_kit', '提取试剂盒', 'text', NULL, false, 2, NULL, 'system'),
    ('task_extract', 'extract_volume', '提取体积', 'number', 'μL', true, 3, NULL, 'system'),
    ('task_extract', 'elution_volume', '洗脱体积', 'number', 'μL', true, 4, NULL, 'system'),
    ('task_extract', 'extract_date', '提取日期', 'date', NULL, true, 5, NULL, 'system');

-- 提取方法选项配置
UPDATE lims_field_definition
SET options = '{
    "items": [
        {"label": "磁珠法", "value": "magnetic_beads"},
        {"label": "柱提法", "value": "column"},
        {"label": "酚氯仿法", "value": "phenol_chloroform"}
    ]
}'::jsonb
WHERE node_id = 'task_extract' AND field_name = 'extract_method';

-- 提取体积验证规则
UPDATE lims_field_definition
SET validation_rule = '{
    "type": "number",
    "min": 1,
    "max": 1000,
    "message": "请输入1-1000μL之间的体积值"
}'::jsonb
WHERE node_id = 'task_extract' AND field_name = 'extract_volume';

-- ============================================================================
-- 5. 插入字段定义 - 样本前处理节点
-- ============================================================================
INSERT INTO lims_field_definition (node_id, field_name, field_label, field_type, unit, required, display_order, validation_rule, created_by)
VALUES
    ('task_preprocess', 'nucleic_acid_concentration', '核酸浓度', 'number', 'ng/μL', true, 1, NULL, 'system'),
    ('task_preprocess', 'od_260_280', 'OD260/280', 'number', NULL, false, 2, NULL, 'system'),
    ('task_preprocess', 'od_260_230', 'OD260/230', 'number', NULL, false, 3, NULL, 'system'),
    ('task_preprocess', 'total_amount', '总量', 'number', 'ng', false, 4, NULL, 'system'),
    ('task_preprocess', 'add_ddh2o', '补ddH₂O', 'number', 'μL', false, 5, NULL, 'system'),
    ('task_preprocess', 'dilution_factor', '稀释倍数', 'number', NULL, false, 6, NULL, 'system');

-- 核酸浓度验证规则
UPDATE lims_field_definition
SET validation_rule = '{
    "type": "number",
    "min": 0,
    "max": 10000,
    "message": "请输入0-10000ng/μL之间的浓度值"
}'::jsonb
WHERE node_id = 'task_preprocess' AND field_name = 'nucleic_acid_concentration';

-- OD值验证规则
UPDATE lims_field_definition
SET validation_rule = '{
    "type": "number",
    "min": 0,
    "max": 5,
    "message": "请输入0-5之间的OD值"
}'::jsonb
WHERE node_id = 'task_preprocess' AND field_name IN ('od_260_280', 'od_260_230');

-- 总量计算公式（核酸浓度 × 体积）
UPDATE lims_field_definition
SET formula = 'nucleic_acid_concentration * extract_volume'
WHERE node_id = 'task_preprocess' AND field_name = 'total_amount';

-- ============================================================================
-- 6. 插入字段定义 - 文库构建节点
-- ============================================================================
INSERT INTO lims_field_definition (node_id, field_name, field_label, field_type, unit, required, display_order, validation_rule, created_by)
VALUES
    ('task_library', 'library_kit', '文库构建试剂盒', 'text', NULL, true, 1, NULL, 'system'),
    ('task_library', 'library_type', '文库类型', 'select', NULL, true, 2, NULL, 'system'),
    ('task_library', 'input_amount', '投入量', 'number', 'ng', true, 3, NULL, 'system'),
    ('task_library', 'adapter_index', '接头索引', 'text', NULL, false, 4, NULL, 'system'),
    ('task_library', 'pcr_cycles', 'PCR循环数', 'number', NULL, false, 5, NULL, 'system'),
    ('task_library', 'library_concentration', '文库浓度', 'number', 'ng/μL', true, 6, NULL, 'system'),
    ('task_library', 'library_size', '文库片段大小', 'number', 'bp', false, 7, NULL, 'system');

-- 文库类型选项配置
UPDATE lims_field_definition
SET options = '{
    "items": [
        {"label": "标准文库", "value": "standard"},
        {"label": "长片段文库", "value": "long_fragment"},
        {"label": "短片段文库", "value": "short_fragment"}
    ]
}'::jsonb
WHERE node_id = 'task_library' AND field_name = 'library_type';

-- PCR循环数验证规则
UPDATE lims_field_definition
SET validation_rule = '{
    "type": "number",
    "min": 1,
    "max": 30,
    "message": "请输入1-30之间的循环数"
}'::jsonb
WHERE node_id = 'task_library' AND field_name = 'pcr_cycles';

-- ============================================================================
-- 7. 插入字段定义 - 上机测序节点
-- ============================================================================
INSERT INTO lims_field_definition (node_id, field_name, field_label, field_type, unit, required, display_order, validation_rule, created_by)
VALUES
    ('task_sequencing', 'sequencing_platform', '测序平台', 'select', NULL, true, 1, NULL, 'system'),
    ('task_sequencing', 'sequencing_mode', '测序模式', 'select', NULL, true, 2, NULL, 'system'),
    ('task_sequencing', 'read_length', '读长', 'number', 'bp', true, 3, NULL, 'system'),
    ('task_sequencing', 'sequencing_depth', '测序深度', 'number', 'X', false, 4, NULL, 'system'),
    ('task_sequencing', 'run_id', '测序批次号', 'text', NULL, true, 5, NULL, 'system'),
    ('task_sequencing', 'lane_number', '泳道号', 'text', NULL, false, 6, NULL, 'system'),
    ('task_sequencing', 'sequencing_date', '测序日期', 'date', NULL, true, 7, NULL, 'system');

-- 测序平台选项配置
UPDATE lims_field_definition
SET options = '{
    "items": [
        {"label": "Illumina NovaSeq", "value": "novaseq"},
        {"label": "Illumina MiSeq", "value": "miseq"},
        {"label": "PacBio Sequel", "value": "pacbio"},
        {"label": "Oxford Nanopore", "value": "nanopore"}
    ]
}'::jsonb
WHERE node_id = 'task_sequencing' AND field_name = 'sequencing_platform';

-- 测序模式选项配置
UPDATE lims_field_definition
SET options = '{
    "items": [
        {"label": "单端测序", "value": "single_end"},
        {"label": "双端测序", "value": "paired_end"}
    ]
}'::jsonb
WHERE node_id = 'task_sequencing' AND field_name = 'sequencing_mode';

-- ============================================================================
-- 8. 插入默认容器
-- ============================================================================
INSERT INTO lims_container (id, container_code, container_type, capacity, status, created_by)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'PLATE96-001', '96-well-plate', 96, 'active', 'system'),
    ('10000000-0000-0000-0000-000000000002', 'PLATE96-002', '96-well-plate', 96, 'active', 'system'),
    ('10000000-0000-0000-0000-000000000003', 'PLATE48-001', '48-deep-well-plate', 48, 'active', 'system'),
    ('10000000-0000-0000-0000-000000000004', 'PLATE48-002', '48-deep-well-plate', 48, 'active', 'system');

-- ============================================================================
-- 9. 插入测试样本数据（可选，用于开发测试）
-- ============================================================================
-- 取消注释以下代码以插入测试数据

/*
INSERT INTO lims_sample (
    id, sample_code, sample_name, project_id, sample_type, status,
    container_id, position, created_by
)
VALUES
    (
        '20000000-0000-0000-0000-000000000001',
        'AK20260225001',
        '测试样本001',
        '00000000-0000-0000-0000-000000000001',
        'plate',
        'pending',
        '10000000-0000-0000-0000-000000000001',
        'A01',
        'system'
    ),
    (
        '20000000-0000-0000-0000-000000000002',
        'AK20260225002',
        '测试样本002',
        '00000000-0000-0000-0000-000000000001',
        'liquid',
        'pending',
        '10000000-0000-0000-0000-000000000001',
        'A02',
        'system'
    ),
    (
        '20000000-0000-0000-0000-000000000003',
        'AK20260225003',
        '测试样本003',
        '00000000-0000-0000-0000-000000000001',
        'plasmid',
        'pending',
        '10000000-0000-0000-0000-000000000001',
        'A03',
        'system'
    );
*/

-- ============================================================================
-- 数据初始化完成
-- ============================================================================
--
-- 已完成以下初始化：
-- 1. 插入3个默认检验项目（全质粒测序、PCR检测、NGS测序）
-- 2. 插入6个流程节点的字段定义（共40+个字段）
-- 3. 配置字段验证规则和选项
-- 4. 插入4个默认容器
--
-- 后续操作：
-- 1. 根据实际业务需求调整字段定义
-- 2. 添加更多项目和容器
-- 3. 配置 Flowable 流程定义
-- 4. 部署应用并测试数据流转
--
-- ============================================================================

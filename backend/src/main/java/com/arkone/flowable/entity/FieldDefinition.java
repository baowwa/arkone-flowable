package com.arkone.flowable.entity;

import com.arkone.flowable.common.typehandler.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 字段定义实体
 * 对应数据库表：lims_field_definition
 * 定义流程节点的字段配置
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("lims_field_definition")
public class FieldDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程节点ID
     */
    @TableField("node_id")
    private String nodeId;

    /**
     * 字段名称（英文标识）
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段标签（中文显示名）
     */
    @TableField("field_label")
    private String fieldLabel;

    /**
     * 字段类型
     * text: 文本
     * number: 数字
     * date: 日期
     * datetime: 日期时间
     * select: 下拉选择
     * multiselect: 多选
     * boolean: 布尔值
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 单位（用于数值类型字段）
     */
    @TableField("unit")
    private String unit;

    /**
     * 是否必填
     */
    @TableField("required")
    private Boolean required;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 验证规则（JSONB格式）
     * 例如：{"type": "number", "min": 0, "max": 1000}
     */
    @TableField(value = "validation_rule", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> validationRule;

    /**
     * 计算公式（用于自动计算字段）
     * 例如：补ddH₂O = 50 - 核酸浓度
     */
    @TableField("formula")
    private String formula;

    /**
     * 选项配置（JSONB格式，用于select/multiselect类型）
     * 例如：{"items": [{"label": "平板样本", "value": "plate"}]}
     */
    @TableField(value = "options", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> options;

    /**
     * 显示顺序
     */
    @TableField("display_order")
    private Integer displayOrder;
}

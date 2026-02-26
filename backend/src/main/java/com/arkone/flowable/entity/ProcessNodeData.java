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
 * 流程节点数据实体
 * 对应数据库表：lims_process_node_data
 * 存储样本在各个流程节点的业务数据
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("lims_process_node_data")
public class ProcessNodeData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联的样本ID
     */
    @TableField("sample_id")
    private String sampleId;

    /**
     * 流程节点ID（对应BPMN中的节点ID）
     */
    @TableField("node_id")
    private String nodeId;

    /**
     * 流程节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点业务数据（JSONB格式）
     * 存储该节点的所有字段值
     * 例如：{"48深孔板号": "P001", "核酸浓度": 125.5}
     */
    @TableField(value = "data", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> data;

    /**
     * 字段定义快照（JSONB格式）
     * 记录当时的字段配置，用于历史数据追溯
     */
    @TableField(value = "field_snapshot", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> fieldSnapshot;

    /**
     * 节点数据状态
     * draft: 草稿
     * completed: 已完成
     * verified: 已审核
     */
    @TableField("status")
    private String status;
}

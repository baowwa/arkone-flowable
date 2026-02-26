package com.arkone.flowable.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 样本实体
 * 对应数据库表：lims_sample
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("lims_sample")
public class Sample extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 样本编码，唯一标识
     */
    @TableField("sample_code")
    private String sampleCode;

    /**
     * 样本名称
     */
    @TableField("sample_name")
    private String sampleName;

    /**
     * 样本类型
     * plate: 平板样本
     * liquid: 直抽菌液
     * plasmid: 质粒核酸
     */
    @TableField("sample_type")
    private String sampleType;

    /**
     * 样本状态
     * pending: 待处理
     * in_progress: 处理中
     * completed: 已完成
     * failed: 失败
     */
    @TableField("status")
    private String status;

    /**
     * 关联的项目ID
     */
    @TableField("project_id")
    private String projectId;

    /**
     * 关联的流程实例ID（Flowable流程实例）
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 当前所在流程节点ID
     */
    @TableField("current_node_id")
    private String currentNodeId;

    /**
     * 关联的容器ID
     */
    @TableField("container_id")
    private String containerId;

    /**
     * 在容器中的位置
     * 例如：A01、B12等
     */
    @TableField("position")
    private String position;

    /**
     * 父样本ID（用于样本分装、转移等场景）
     */
    @TableField("parent_sample_id")
    private String parentSampleId;
}

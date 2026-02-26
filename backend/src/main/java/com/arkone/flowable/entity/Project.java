package com.arkone.flowable.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 检验项目实体
 * 对应数据库表：lims_project
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("lims_project")
public class Project extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 项目编码，唯一标识
     */
    @TableField("project_code")
    private String projectCode;

    /**
     * 项目名称
     */
    @TableField("project_name")
    private String projectName;

    /**
     * 项目描述
     */
    @TableField("description")
    private String description;

    /**
     * 关联的流程定义ID（Flowable流程定义）
     */
    @TableField("process_definition_id")
    private String processDefinitionId;

    /**
     * 项目状态
     * active: 激活
     * inactive: 停用
     */
    @TableField("status")
    private String status;
}

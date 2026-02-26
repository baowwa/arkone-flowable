package com.arkone.flowable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 样本响应DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleResponse {

    /**
     * 样本ID
     */
    private String id;

    /**
     * 样本编码
     */
    private String sampleCode;

    /**
     * 样本名称
     */
    private String sampleName;

    /**
     * 样本类型
     */
    private String sampleType;

    /**
     * 样本状态
     */
    private String status;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 容器ID
     */
    private String containerId;

    /**
     * 容器位置
     */
    private String position;

    /**
     * 父样本ID
     */
    private String parentSampleId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

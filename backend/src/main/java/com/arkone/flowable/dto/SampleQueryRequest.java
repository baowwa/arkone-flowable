package com.arkone.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 样本查询请求DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleQueryRequest {

    /**
     * 样本编码（模糊查询）
     */
    private String sampleCode;

    /**
     * 样本名称（模糊查询）
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
     * 容器ID
     */
    private String containerId;

    /**
     * 页码（从1开始）
     */
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer pageSize = 20;
}

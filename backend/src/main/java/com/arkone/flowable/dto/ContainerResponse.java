package com.arkone.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 容器响应DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerResponse {

    /**
     * 容器ID
     */
    private String id;

    /**
     * 容器编码
     */
    private String containerCode;

    /**
     * 容器类型
     */
    private String containerType;

    /**
     * 容器容量
     */
    private Integer capacity;

    /**
     * 已使用数量
     */
    private Integer usedCount;

    /**
     * 容器状态
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

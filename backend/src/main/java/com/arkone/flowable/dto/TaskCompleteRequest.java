package com.arkone.flowable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 任务完成请求DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {

    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /**
     * 样本ID
     */
    @NotBlank(message = "样本ID不能为空")
    private String sampleId;

    /**
     * 节点数据
     */
    @NotNull(message = "节点数据不能为空")
    private Map<String, Object> data;

    /**
     * 流程变量（可选）
     */
    private Map<String, Object> variables;
}

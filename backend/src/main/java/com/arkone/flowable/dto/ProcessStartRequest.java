package com.arkone.flowable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 流程启动请求DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStartRequest {

    /**
     * 项目ID
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    /**
     * 样本ID列表
     */
    @NotEmpty(message = "样本ID列表不能为空")
    private List<String> sampleIds;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 业务键（可选）
     */
    private String businessKey;
}

package com.arkone.flowable.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量完成任务请求DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskCompleteRequest {

    /**
     * 任务完成请求列表（最多100条）
     */
    @NotEmpty(message = "任务列表不能为空")
    @Size(max = 100, message = "批量完成任务最多支持100条")
    @Valid
    private List<TaskCompleteRequest> tasks;

    /**
     * 失败模式: atomic-原子模式(全部成功或全部失败), partial-部分成功模式
     */
    @Builder.Default
    private String failureMode = "partial";
}

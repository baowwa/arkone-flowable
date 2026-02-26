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
 * 批量创建样本请求DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateRequest {

    /**
     * 样本列表（最多500条）
     */
    @NotEmpty(message = "样本列表不能为空")
    @Size(max = 500, message = "批量创建最多支持500条样本")
    @Valid
    private List<SampleCreateRequest> samples;

    /**
     * 失败模式: atomic-原子模式(全部成功或全部失败), partial-部分成功模式
     */
    @Builder.Default
    private String failureMode = "partial";
}

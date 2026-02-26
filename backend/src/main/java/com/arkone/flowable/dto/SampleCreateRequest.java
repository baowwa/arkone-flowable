package com.arkone.flowable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 样本创建请求DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Schema(description = "样本创建请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleCreateRequest {

    /**
     * 样本名称
     */
    @Schema(description = "样本名称", example = "Sample-001", required = true)
    @NotBlank(message = "样本名称不能为空")
    private String sampleName;

    /**
     * 样本类型: plate-平板样本, liquid-直抽菌液, plasmid-质粒核酸
     */
    @Schema(description = "样本类型", example = "plate", allowableValues = {"plate", "liquid", "plasmid"}, required = true)
    @NotBlank(message = "样本类型不能为空")
    private String sampleType;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID", example = "550e8400e29b41d4a716446655440000", required = true)
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    /**
     * 容器ID（可选）
     */
    @Schema(description = "容器ID", example = "550e8400e29b41d4a716446655440001")
    private String containerId;

    /**
     * 容器位置（可选）
     */
    @Schema(description = "容器位置", example = "A01")
    private String position;

    /**
     * 父样本ID（可选，用于样本分装）
     */
    @Schema(description = "父样本ID", example = "550e8400e29b41d4a716446655440002")
    private String parentSampleId;
}

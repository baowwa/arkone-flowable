package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.BatchCreateRequest;
import com.arkone.flowable.dto.BatchOperationResult;
import com.arkone.flowable.dto.SampleResponse;
import com.arkone.flowable.service.BatchOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 批量操作控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "批量操作", description = "样本的批量创建、批量更新等操作")
@Slf4j
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchOperationController {

    private final BatchOperationService batchOperationService;

    /**
     * 批量创建样本
     *
     * @param request 批量创建请求
     * @return 批量操作结果
     */
    @Operation(summary = "批量创建样本", description = "批量创建样本，最多支持500条，支持原子模式和部分成功模式")
    @PostMapping("/samples")
    public Result<BatchOperationResult<SampleResponse>> batchCreateSamples(
            @Valid @RequestBody BatchCreateRequest request) {
        log.info("批量创建样本: 数量={}, 模式={}", request.getSamples().size(), request.getFailureMode());
        BatchOperationResult<SampleResponse> result = batchOperationService.batchCreateSamples(request);
        return Result.success(result);
    }

    /**
     * 批量更新样本状态
     *
     * @param sampleIds 样本ID列表
     * @param status    新状态
     * @return 批量操作结果
     */
    @Operation(summary = "批量更新样本状态", description = "批量更新样本状态")
    @Parameter(name = "status", description = "新状态(pending/in_progress/completed/failed)", required = true)
    @PutMapping("/samples/status")
    public Result<BatchOperationResult<SampleResponse>> batchUpdateStatus(
            @RequestBody List<String> sampleIds,
            @RequestParam String status) {
        log.info("批量更新样本状态: 数量={}, 状态={}", sampleIds.size(), status);
        BatchOperationResult<SampleResponse> result = batchOperationService.batchUpdateStatus(sampleIds, status);
        return Result.success(result);
    }
}

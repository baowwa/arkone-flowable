package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.SampleCreateRequest;
import com.arkone.flowable.dto.SampleQueryRequest;
import com.arkone.flowable.dto.SampleResponse;
import com.arkone.flowable.service.SampleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 样本管理控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "样本管理", description = "样本的创建、查询、更新等操作")
@Slf4j
@RestController
@RequestMapping("/samples")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    /**
     * 创建样本
     *
     * @param request 样本创建请求
     * @return 样本响应
     */
    @Operation(summary = "创建样本", description = "创建单个样本，自动生成样本编码")
    @PostMapping
    public Result<SampleResponse> createSample(@Valid @RequestBody SampleCreateRequest request) {
        log.info("创建样本: {}", request.getSampleName());
        SampleResponse response = sampleService.createSample(request);
        return Result.success(response);
    }

    /**
     * 分页查询样本列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Operation(summary = "查询样本列表", description = "分页查询样本列表，支持多条件筛选")
    @GetMapping
    public Result<Page<SampleResponse>> querySamples(SampleQueryRequest request) {
        log.info("查询样本列表: {}", request);
        Page<SampleResponse> page = sampleService.querySamples(request);
        return Result.success(page);
    }

    /**
     * 获取样本详情
     *
     * @param id 样本ID
     * @return 样本响应
     */
    @Operation(summary = "获取样本详情", description = "根据样本ID获取样本详细信息")
    @Parameter(name = "id", description = "样本ID", required = true)
    @GetMapping("/{id}")
    public Result<SampleResponse> getSampleById(@PathVariable String id) {
        log.info("获取样本详情: {}", id);
        SampleResponse response = sampleService.getSampleById(id);
        return Result.success(response);
    }

    /**
     * 更新样本状态
     *
     * @param id     样本ID
     * @param status 新状态
     * @return 样本响应
     */
    @Operation(summary = "更新样本状态", description = "更新样本的状态(pending/in_progress/completed/failed)")
    @Parameter(name = "id", description = "样本ID", required = true)
    @Parameter(name = "status", description = "新状态", required = true)
    @PutMapping("/{id}/status")
    public Result<SampleResponse> updateSampleStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("更新样本状态: {} -> {}", id, status);
        SampleResponse response = sampleService.updateSampleStatus(id, status);
        return Result.success(response);
    }
}

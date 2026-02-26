package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.ContainerResponse;
import com.arkone.flowable.service.ContainerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 容器管理控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "容器管理", description = "容器的查询等操作")
@Slf4j
@RestController
@RequestMapping("/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    /**
     * 分页查询容器列表
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param containerType 容器类型(可选)
     * @param status        容器状态(可选)
     * @return 分页结果
     */
    @Operation(summary = "查询容器列表", description = "分页查询容器列表,支持按类型和状态筛选")
    @GetMapping
    public Result<Page<ContainerResponse>> queryContainers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "容器类型") @RequestParam(required = false) String containerType,
            @Parameter(description = "容器状态") @RequestParam(required = false) String status) {
        log.info("查询容器列表: pageNum={}, pageSize={}, containerType={}, status={}",
                pageNum, pageSize, containerType, status);
        Page<ContainerResponse> page = containerService.queryContainers(pageNum, pageSize, containerType, status);
        return Result.success(page);
    }

    /**
     * 获取容器详情
     *
     * @param id 容器ID
     * @return 容器响应
     */
    @Operation(summary = "获取容器详情", description = "根据容器ID获取容器详细信息")
    @Parameter(name = "id", description = "容器ID", required = true)
    @GetMapping("/{id}")
    public Result<ContainerResponse> getContainerById(@PathVariable String id) {
        log.info("获取容器详情: {}", id);
        ContainerResponse response = containerService.getContainerById(id);
        return Result.success(response);
    }
}

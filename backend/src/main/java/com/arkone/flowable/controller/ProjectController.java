package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.ProjectResponse;
import com.arkone.flowable.service.ProjectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 项目管理控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "项目管理", description = "项目的查询等操作")
@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 分页查询项目列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   项目状态(可选)
     * @return 分页结果
     */
    @Operation(summary = "查询项目列表", description = "分页查询项目列表,支持按状态筛选")
    @GetMapping
    public Result<Page<ProjectResponse>> queryProjects(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目状态") @RequestParam(required = false) String status) {
        log.info("查询项目列表: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        Page<ProjectResponse> page = projectService.queryProjects(pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 获取项目详情
     *
     * @param id 项目ID
     * @return 项目响应
     */
    @Operation(summary = "获取项目详情", description = "根据项目ID获取项目详细信息")
    @Parameter(name = "id", description = "项目ID", required = true)
    @GetMapping("/{id}")
    public Result<ProjectResponse> getProjectById(@PathVariable String id) {
        log.info("获取项目详情: {}", id);
        ProjectResponse response = projectService.getProjectById(id);
        return Result.success(response);
    }
}

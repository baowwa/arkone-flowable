package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.BatchOperationResult;
import com.arkone.flowable.dto.BatchTaskCompleteRequest;
import com.arkone.flowable.dto.TaskCompleteRequest;
import com.arkone.flowable.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "任务管理", description = "流程任务的完成、批量完成等操作")
@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 完成任务
     *
     * @param taskId  任务ID
     * @param request 任务完成请求
     * @return 成功响应
     */
    @Operation(summary = "完成任务", description = "完成指定的流程任务，保存节点数据")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PostMapping("/{taskId}/complete")
    public Result<Void> completeTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskCompleteRequest request) {
        log.info("完成任务: taskId={}, sampleId={}", taskId, request.getSampleId());
        request.setTaskId(taskId);
        taskService.completeTask(request);
        return Result.success();
    }

    /**
     * 批量完成任务
     *
     * @param request 批量任务完成请求
     * @return 批量操作结果
     */
    @Operation(summary = "批量完成任务", description = "批量完成流程任务，最多支持100条")
    @PostMapping("/batch/complete")
    public Result<BatchOperationResult<String>> batchCompleteTasks(
            @Valid @RequestBody BatchTaskCompleteRequest request) {
        log.info("批量完成任务: 数量={}, 模式={}", request.getTasks().size(), request.getFailureMode());
        BatchOperationResult<String> result = taskService.batchCompleteTasks(request);
        return Result.success(result);
    }
}

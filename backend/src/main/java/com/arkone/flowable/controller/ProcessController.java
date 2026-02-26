package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.ProcessInstanceResponse;
import com.arkone.flowable.dto.ProcessStartRequest;
import com.arkone.flowable.service.ProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 流程管理控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "流程管理", description = "流程的启动、查询等操作")
@Slf4j
@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    /**
     * 启动全质粒测序流程
     *
     * @param request 流程启动请求
     * @return 流程实例响应
     */
    @Operation(summary = "启动全质粒测序流程", description = "为指定样本启动全质粒测序流程")
    @PostMapping("/plasmid/start")
    public Result<ProcessInstanceResponse> startPlasmidProcess(@Valid @RequestBody ProcessStartRequest request) {
        log.info("启动全质粒测序流程: projectId={}, sampleCount={}", request.getProjectId(), request.getSampleIds().size());
        ProcessInstanceResponse response = processService.startPlasmidProcess(request);
        return Result.success(response);
    }

    /**
     * 启动PCR产物测序流程
     *
     * @param request 流程启动请求
     * @return 流程实例响应
     */
    @Operation(summary = "启动PCR产物测序流程", description = "为指定样本启动PCR产物测序流程")
    @PostMapping("/pcr/start")
    public Result<ProcessInstanceResponse> startPcrProcess(@Valid @RequestBody ProcessStartRequest request) {
        log.info("启动PCR产物测序流程: projectId={}, sampleCount={}", request.getProjectId(), request.getSampleIds().size());
        ProcessInstanceResponse response = processService.startPcrProcess(request);
        return Result.success(response);
    }
}

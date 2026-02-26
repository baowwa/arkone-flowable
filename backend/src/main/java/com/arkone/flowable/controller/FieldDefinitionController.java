package com.arkone.flowable.controller;

import com.arkone.flowable.common.Result;
import com.arkone.flowable.dto.FieldDefinitionResponse;
import com.arkone.flowable.service.FieldDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段定义控制器
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Tag(name = "字段定义", description = "流程节点字段定义的查询")
@Slf4j
@RestController
@RequestMapping("/fields")
@RequiredArgsConstructor
public class FieldDefinitionController {

    private final FieldDefinitionService fieldDefinitionService;

    /**
     * 获取节点字段定义
     *
     * @param nodeId 节点ID
     * @return 字段定义列表
     */
    @Operation(summary = "获取节点字段定义", description = "根据节点ID获取该节点的所有字段定义")
    @Parameter(name = "nodeId", description = "流程节点ID", required = true)
    @GetMapping("/nodes/{nodeId}")
    public Result<List<FieldDefinitionResponse>> getFieldsByNodeId(@PathVariable String nodeId) {
        log.info("获取节点字段定义: nodeId={}", nodeId);
        List<FieldDefinitionResponse> fields = fieldDefinitionService.getFieldsByNodeId(nodeId);
        return Result.success(fields);
    }
}

package com.arkone.flowable.service.impl;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.BatchOperationResult;
import com.arkone.flowable.dto.BatchTaskCompleteRequest;
import com.arkone.flowable.dto.TaskCompleteRequest;
import com.arkone.flowable.entity.FieldDefinition;
import com.arkone.flowable.entity.ProcessNodeData;
import com.arkone.flowable.entity.Sample;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.repository.FieldDefinitionMapper;
import com.arkone.flowable.repository.ProcessNodeDataMapper;
import com.arkone.flowable.repository.SampleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 任务管理服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements com.arkone.flowable.service.TaskService {

    private final org.flowable.engine.TaskService flowableTaskService;
    private final SampleMapper sampleMapper;
    private final ProcessNodeDataMapper processNodeDataMapper;
    private final FieldDefinitionMapper fieldDefinitionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(TaskCompleteRequest request) {
        // 验证任务是否存在
        Task task = flowableTaskService.createTaskQuery()
                .taskId(request.getTaskId())
                .singleResult();

        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 验证样本是否存在
        Sample sample = sampleMapper.selectById(request.getSampleId());
        if (sample == null || sample.getDeleted()) {
            throw new BusinessException(ErrorCode.SAMPLE_NOT_FOUND);
        }

        // 获取节点ID和节点名称
        String nodeId = task.getTaskDefinitionKey();
        String nodeName = task.getName();

        // 查询字段定义并保存快照
        LambdaQueryWrapper<FieldDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldDefinition::getNodeId, nodeId);
        List<FieldDefinition> fieldDefinitions = fieldDefinitionMapper.selectList(wrapper);

        Map<String, Object> fieldSnapshot = new HashMap<>();
        for (FieldDefinition fd : fieldDefinitions) {
            Map<String, Object> fieldInfo = new HashMap<>();
            fieldInfo.put("fieldName", fd.getFieldName());
            fieldInfo.put("fieldLabel", fd.getFieldLabel());
            fieldInfo.put("fieldType", fd.getFieldType());
            fieldInfo.put("unit", fd.getUnit());
            fieldInfo.put("required", fd.getRequired());
            fieldSnapshot.put(fd.getFieldName(), fieldInfo);
        }

        // 保存节点数据
        ProcessNodeData nodeData = ProcessNodeData.builder()
                .sampleId(sample.getId())
                .nodeId(nodeId)
                .nodeName(nodeName)
                .data(request.getData())
                .fieldSnapshot(fieldSnapshot)
                .status("completed")
                .build();

        nodeData.setCreatedBy("system");
        nodeData.setCreatedAt(LocalDateTime.now());
        processNodeDataMapper.insert(nodeData);

        // 完成任务
        Map<String, Object> variables = request.getVariables() != null ?
                request.getVariables() : new HashMap<>();

        try {
            flowableTaskService.complete(request.getTaskId(), variables);
        } catch (Exception e) {
            log.error("任务完成失败: taskId={}", request.getTaskId(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "任务完成失败: " + e.getMessage());
        }

        // 更新样本的当前节点
        sample.setCurrentNodeId(nodeId);
        sample.setUpdatedBy("system");
        sample.setUpdatedAt(LocalDateTime.now());
        sampleMapper.updateById(sample);

        log.info("任务完成成功: taskId={}, sampleId={}, nodeId={}", request.getTaskId(), request.getSampleId(), nodeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult<String> batchCompleteTasks(BatchTaskCompleteRequest request) {
        List<String> successList = new ArrayList<>();
        List<BatchOperationResult.BatchOperationError> failureList = new ArrayList<>();

        boolean isAtomicMode = "atomic".equals(request.getFailureMode());

        for (int i = 0; i < request.getTasks().size(); i++) {
            TaskCompleteRequest taskRequest = request.getTasks().get(i);
            try {
                completeTask(taskRequest);
                successList.add(taskRequest.getTaskId());
            } catch (BusinessException e) {
                log.warn("批量完成任务失败 [索引:{}]: {}", i, e.getMessage());

                BatchOperationResult.BatchOperationError error = BatchOperationResult.BatchOperationError.builder()
                        .index(i)
                        .errorCode(e.getErrorCode().name())
                        .errorMessage(e.getMessage())
                        .data(taskRequest)
                        .build();
                failureList.add(error);

                if (isAtomicMode) {
                    throw new BusinessException(ErrorCode.BATCH_OPERATION_FAILED,
                            String.format("批量完成任务失败(原子模式): 第%d条数据错误 - %s", i + 1, e.getMessage()));
                }
            } catch (Exception e) {
                log.error("批量完成任务异常 [索引:{}]", i, e);

                BatchOperationResult.BatchOperationError error = BatchOperationResult.BatchOperationError.builder()
                        .index(i)
                        .errorCode("INTERNAL_ERROR")
                        .errorMessage(e.getMessage())
                        .data(taskRequest)
                        .build();
                failureList.add(error);

                if (isAtomicMode) {
                    throw new BusinessException(ErrorCode.BATCH_OPERATION_FAILED,
                            String.format("批量完成任务失败(原子模式): 第%d条数据异常", i + 1));
                }
            }
        }

        BatchOperationResult<String> result = BatchOperationResult.<String>builder()
                .total(request.getTasks().size())
                .successCount(successList.size())
                .failureCount(failureList.size())
                .successList(successList)
                .failureList(failureList)
                .build();

        log.info("批量完成任务完成: 总数={}, 成功={}, 失败={}", result.getTotal(), result.getSuccessCount(), result.getFailureCount());
        return result;
    }
}

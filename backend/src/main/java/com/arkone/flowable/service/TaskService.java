package com.arkone.flowable.service;

import com.arkone.flowable.dto.BatchOperationResult;
import com.arkone.flowable.dto.BatchTaskCompleteRequest;
import com.arkone.flowable.dto.TaskCompleteRequest;

/**
 * 任务管理服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface TaskService {

    /**
     * 完成任务
     *
     * @param request 任务完成请求
     */
    void completeTask(TaskCompleteRequest request);

    /**
     * 批量完成任务
     *
     * @param request 批量任务完成请求
     * @return 批量操作结果
     */
    BatchOperationResult<String> batchCompleteTasks(BatchTaskCompleteRequest request);
}

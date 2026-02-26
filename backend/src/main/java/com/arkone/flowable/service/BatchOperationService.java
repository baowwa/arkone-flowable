package com.arkone.flowable.service;

import com.arkone.flowable.dto.BatchCreateRequest;
import com.arkone.flowable.dto.BatchOperationResult;
import com.arkone.flowable.dto.SampleResponse;

import java.util.List;

/**
 * 批量操作服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface BatchOperationService {

    /**
     * 批量创建样本
     *
     * @param request 批量创建请求
     * @return 批量操作结果
     */
    BatchOperationResult<SampleResponse> batchCreateSamples(BatchCreateRequest request);

    /**
     * 批量更新样本状态
     *
     * @param sampleIds 样本ID列表
     * @param status    新状态
     * @return 批量操作结果
     */
    BatchOperationResult<SampleResponse> batchUpdateStatus(List<String> sampleIds, String status);
}

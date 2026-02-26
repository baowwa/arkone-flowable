package com.arkone.flowable.service.impl;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.BatchCreateRequest;
import com.arkone.flowable.dto.BatchOperationResult;
import com.arkone.flowable.dto.SampleCreateRequest;
import com.arkone.flowable.dto.SampleResponse;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.service.BatchOperationService;
import com.arkone.flowable.service.SampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOperationServiceImpl implements BatchOperationService {

    private final SampleService sampleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult<SampleResponse> batchCreateSamples(BatchCreateRequest request) {
        List<SampleResponse> successList = new ArrayList<>();
        List<BatchOperationResult.BatchOperationError> failureList = new ArrayList<>();

        boolean isAtomicMode = "atomic".equals(request.getFailureMode());

        for (int i = 0; i < request.getSamples().size(); i++) {
            SampleCreateRequest sampleRequest = request.getSamples().get(i);
            try {
                SampleResponse response = sampleService.createSample(sampleRequest);
                successList.add(response);
            } catch (BusinessException e) {
                log.warn("批量创建样本失败 [索引:{}]: {}", i, e.getMessage());

                BatchOperationResult.BatchOperationError error = BatchOperationResult.BatchOperationError.builder()
                        .index(i)
                        .errorCode(e.getErrorCode().name())
                        .errorMessage(e.getMessage())
                        .data(sampleRequest)
                        .build();
                failureList.add(error);

                // 原子模式下,任何失败都抛出异常回滚
                if (isAtomicMode) {
                    throw new BusinessException(ErrorCode.BATCH_OPERATION_FAILED,
                            String.format("批量创建失败(原子模式): 第%d条数据错误 - %s", i + 1, e.getMessage()));
                }
            } catch (Exception e) {
                log.error("批量创建样本异常 [索引:{}]", i, e);

                BatchOperationResult.BatchOperationError error = BatchOperationResult.BatchOperationError.builder()
                        .index(i)
                        .errorCode("INTERNAL_ERROR")
                        .errorMessage(e.getMessage())
                        .data(sampleRequest)
                        .build();
                failureList.add(error);

                if (isAtomicMode) {
                    throw new BusinessException(ErrorCode.BATCH_OPERATION_FAILED,
                            String.format("批量创建失败(原子模式): 第%d条数据异常", i + 1));
                }
            }
        }

        BatchOperationResult<SampleResponse> result = BatchOperationResult.<SampleResponse>builder()
                .total(request.getSamples().size())
                .successCount(successList.size())
                .failureCount(failureList.size())
                .successList(successList)
                .failureList(failureList)
                .build();

        log.info("批量创建样本完成: 总数={}, 成功={}, 失败={}", result.getTotal(), result.getSuccessCount(), result.getFailureCount());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult<SampleResponse> batchUpdateStatus(List<String> sampleIds, String status) {
        List<SampleResponse> successList = new ArrayList<>();
        List<BatchOperationResult.BatchOperationError> failureList = new ArrayList<>();

        for (int i = 0; i < sampleIds.size(); i++) {
            String sampleId = sampleIds.get(i);
            try {
                SampleResponse response = sampleService.updateSampleStatus(sampleId, status);
                successList.add(response);
            } catch (BusinessException e) {
                log.warn("批量更新状态失败 [索引:{}]: {}", i, e.getMessage());

                BatchOperationResult.BatchOperationError error = BatchOperationResult.BatchOperationError.builder()
                        .index(i)
                        .errorCode(e.getErrorCode().name())
                        .errorMessage(e.getMessage())
                        .data(sampleId)
                        .build();
                failureList.add(error);
            } catch (Exception e) {
                log.error("批量更新状态异常 [索引:{}]", i, e);

                BatchOperationResult.BatchOperationError error = BatchOperationResult.BatchOperationError.builder()
                        .index(i)
                        .errorCode("INTERNAL_ERROR")
                        .errorMessage(e.getMessage())
                        .data(sampleId)
                        .build();
                failureList.add(error);
            }
        }

        BatchOperationResult<SampleResponse> result = BatchOperationResult.<SampleResponse>builder()
                .total(sampleIds.size())
                .successCount(successList.size())
                .failureCount(failureList.size())
                .successList(successList)
                .failureList(failureList)
                .build();

        log.info("批量更新状态完成: 总数={}, 成功={}, 失败={}", result.getTotal(), result.getSuccessCount(), result.getFailureCount());
        return result;
    }
}

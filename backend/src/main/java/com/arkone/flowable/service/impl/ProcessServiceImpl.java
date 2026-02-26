package com.arkone.flowable.service.impl;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.ProcessInstanceResponse;
import com.arkone.flowable.dto.ProcessStartRequest;
import com.arkone.flowable.dto.SampleResponse;
import com.arkone.flowable.entity.Project;
import com.arkone.flowable.entity.Sample;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.repository.ProjectMapper;
import com.arkone.flowable.repository.SampleMapper;
import com.arkone.flowable.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 流程管理服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final RuntimeService runtimeService;
    private final ProjectMapper projectMapper;
    private final SampleMapper sampleMapper;

    private static final String PLASMID_PROCESS_KEY = "plasmid_sequencing";
    private static final String PCR_PROCESS_KEY = "pcr_sequencing";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstanceResponse startPlasmidProcess(ProcessStartRequest request) {
        return startProcess(request, PLASMID_PROCESS_KEY, "全质粒测序流程");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstanceResponse startPcrProcess(ProcessStartRequest request) {
        return startProcess(request, PCR_PROCESS_KEY, "PCR产物测序流程");
    }

    /**
     * 启动流程
     */
    private ProcessInstanceResponse startProcess(ProcessStartRequest request, String processKey, String processName) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(request.getProjectId());
        if (project == null || project.getDeleted()) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 验证样本是否存在
        List<Sample> samples = new ArrayList<>();
        for (String sampleId : request.getSampleIds()) {
            Sample sample = sampleMapper.selectById(sampleId);
            if (sample == null || sample.getDeleted()) {
                throw new BusinessException(ErrorCode.SAMPLE_NOT_FOUND, "样本不存在: " + sampleId);
            }
            // 验证样本是否属于该项目
            if (!sample.getProjectId().equals(project.getId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "样本不属于该项目: " + sampleId);
            }
            samples.add(sample);
        }

        // 准备流程变量
        Map<String, Object> variables = new HashMap<>();
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }
        variables.put("projectId", request.getProjectId());
        variables.put("sampleIds", request.getSampleIds());
        variables.put("sampleCount", samples.size());

        // 启动流程实例
        String businessKey = request.getBusinessKey() != null ?
                request.getBusinessKey() :
                "PROC-" + System.currentTimeMillis();

        ProcessInstance processInstance;
        try {
            processInstance = runtimeService.startProcessInstanceByKey(
                    processKey,
                    businessKey,
                    variables
            );
        } catch (Exception e) {
            log.error("流程启动失败: processKey={}, businessKey={}", processKey, businessKey, e);
            throw new BusinessException(ErrorCode.PROCESS_START_FAILED, "流程启动失败: " + e.getMessage());
        }

        // 更新样本的流程实例ID
        for (Sample sample : samples) {
            sample.setProcessInstanceId(processInstance.getId());
            sample.setStatus("in_progress");
            sample.setUpdatedBy("system");
            sample.setUpdatedAt(LocalDateTime.now());
            sampleMapper.updateById(sample);
        }

        log.info("{}启动成功: processInstanceId={}, businessKey={}, sampleCount={}",
                processName, processInstance.getId(), businessKey, samples.size());

        // 构建响应
        List<SampleResponse> sampleResponses = samples.stream()
                .map(this::convertToSampleResponse)
                .toList();

        return ProcessInstanceResponse.builder()
                .processInstanceId(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .businessKey(processInstance.getBusinessKey())
                .samples(sampleResponses)
                .startTime(processInstance.getStartTime() != null ?
                        LocalDateTime.ofInstant(processInstance.getStartTime().toInstant(), ZoneId.systemDefault()) :
                        LocalDateTime.now())
                .status("running")
                .build();
    }

    /**
     * 转换为样本响应DTO
     */
    private SampleResponse convertToSampleResponse(Sample sample) {
        return SampleResponse.builder()
                .id(sample.getId() != null ? sample.getId().toString() : null)
                .sampleCode(sample.getSampleCode())
                .sampleName(sample.getSampleName())
                .sampleType(sample.getSampleType())
                .status(sample.getStatus())
                .projectId(sample.getProjectId())
                .processInstanceId(sample.getProcessInstanceId())
                .containerId(sample.getContainerId())
                .position(sample.getPosition())
                .parentSampleId(sample.getParentSampleId())
                .createdBy(sample.getCreatedBy())
                .createdAt(sample.getCreatedAt())
                .updatedBy(sample.getUpdatedBy())
                .updatedAt(sample.getUpdatedAt())
                .build();
    }
}

package com.arkone.flowable.service.impl;

import cn.hutool.core.date.DateUtil;
import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.SampleCreateRequest;
import com.arkone.flowable.dto.SampleQueryRequest;
import com.arkone.flowable.dto.SampleResponse;
import com.arkone.flowable.entity.Container;
import com.arkone.flowable.entity.Project;
import com.arkone.flowable.entity.Sample;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.repository.ContainerMapper;
import com.arkone.flowable.repository.ProjectMapper;
import com.arkone.flowable.repository.SampleMapper;
import com.arkone.flowable.service.SampleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 样本服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SampleServiceImpl implements SampleService {

    private final SampleMapper sampleMapper;
    private final ProjectMapper projectMapper;
    private final ContainerMapper containerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SampleResponse createSample(SampleCreateRequest request) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(request.getProjectId());
        if (project == null || project.getDeleted()) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 如果指定了容器,验证容器和位置
        if (request.getContainerId() != null) {
            validateContainerAndPosition(request.getContainerId(), request.getPosition());
        }

        // 生成样本编码: AKYYYYMMDDnnn
        String sampleCode = generateSampleCode();

        // 创建样本实体
        Sample sample = Sample.builder()
                .sampleCode(sampleCode)
                .sampleName(request.getSampleName())
                .sampleType(request.getSampleType())
                .projectId(request.getProjectId())
                .status("pending")
                .containerId(request.getContainerId())
                .position(request.getPosition())
                .parentSampleId(request.getParentSampleId())
                .build();

        sample.setCreatedBy("system"); // TODO: 从上下文获取当前用户
        sample.setCreatedAt(LocalDateTime.now());

        // 保存样本
        sampleMapper.insert(sample);

        // 如果指定了容器,更新容器使用数
        if (request.getContainerId() != null) {
            updateContainerUsedCount(request.getContainerId(), 1);
        }

        log.info("样本创建成功: {}", sampleCode);
        return convertToResponse(sample);
    }

    @Override
    public Page<SampleResponse> querySamples(SampleQueryRequest request) {
        // 构建分页对象
        Page<Sample> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(request.getSampleCode() != null, Sample::getSampleCode, request.getSampleCode())
                .like(request.getSampleName() != null, Sample::getSampleName, request.getSampleName())
                .eq(request.getSampleType() != null, Sample::getSampleType, request.getSampleType())
                .eq(request.getStatus() != null, Sample::getStatus, request.getStatus())
                .eq(request.getProjectId() != null, Sample::getProjectId, request.getProjectId())
                .eq(request.getContainerId() != null, Sample::getContainerId, request.getContainerId())
                .orderByDesc(Sample::getCreatedAt);

        // 执行查询
        Page<Sample> samplePage = sampleMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        Page<SampleResponse> responsePage = new Page<>(samplePage.getCurrent(), samplePage.getSize(), samplePage.getTotal());
        responsePage.setRecords(samplePage.getRecords().stream()
                .map(this::convertToResponse)
                .toList());

        return responsePage;
    }

    @Override
    public SampleResponse getSampleById(String id) {
        Sample sample = sampleMapper.selectById(id);
        if (sample == null || sample.getDeleted()) {
            throw new BusinessException(ErrorCode.SAMPLE_NOT_FOUND);
        }
        return convertToResponse(sample);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SampleResponse updateSampleStatus(String id, String status) {
        // 查询样本
        Sample sample = sampleMapper.selectById(id);
        if (sample == null || sample.getDeleted()) {
            throw new BusinessException(ErrorCode.SAMPLE_NOT_FOUND);
        }

        // 验证状态值
        if (!isValidStatus(status)) {
            throw new BusinessException(ErrorCode.INVALID_SAMPLE_STATUS);
        }

        // 更新状态
        sample.setStatus(status);
        sample.setUpdatedBy("system"); // TODO: 从上下文获取当前用户
        sample.setUpdatedAt(LocalDateTime.now());

        sampleMapper.updateById(sample);

        log.info("样本状态更新成功: {} -> {}", sample.getSampleCode(), status);
        return convertToResponse(sample);
    }

    /**
     * 验证容器和位置
     */
    private void validateContainerAndPosition(String containerId, String position) {
        // 验证容器是否存在
        Container container = containerMapper.selectById(containerId);
        if (container == null || container.getDeleted()) {
            throw new BusinessException(ErrorCode.CONTAINER_NOT_FOUND);
        }

        // 验证容器是否已满
        if (container.getUsedCount() >= container.getCapacity()) {
            throw new BusinessException(ErrorCode.CONTAINER_FULL);
        }

        // 验证位置是否已被占用
        if (position != null) {
            LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Sample::getContainerId, containerId)
                    .eq(Sample::getPosition, position);
            Long count = sampleMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.POSITION_OCCUPIED);
            }
        }
    }

    /**
     * 更新容器使用数
     */
    private void updateContainerUsedCount(String containerId, int delta) {
        Container container = containerMapper.selectById(containerId);
        if (container != null) {
            container.setUsedCount(container.getUsedCount() + delta);
            // 如果容器已满,更新状态
            if (container.getUsedCount() >= container.getCapacity()) {
                container.setStatus("full");
            }
            containerMapper.updateById(container);
        }
    }

    /**
     * 生成样本编码
     * 格式: AKYYYYMMDDnnn
     */
    private String generateSampleCode() {
        String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
        String prefix = "AK" + dateStr;

        // 查询当天最大序号
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Sample::getSampleCode, prefix)
                .orderByDesc(Sample::getSampleCode)
                .last("LIMIT 1");

        Sample lastSample = sampleMapper.selectOne(wrapper);

        int sequence = 1;
        if (lastSample != null) {
            String lastCode = lastSample.getSampleCode();
            String lastSeq = lastCode.substring(prefix.length());
            sequence = Integer.parseInt(lastSeq) + 1;
        }

        return prefix + String.format("%03d", sequence);
    }

    /**
     * 验证状态值是否有效
     */
    private boolean isValidStatus(String status) {
        return "pending".equals(status) ||
                "in_progress".equals(status) ||
                "completed".equals(status) ||
                "failed".equals(status);
    }

    /**
     * 转换为响应DTO
     */
    private SampleResponse convertToResponse(Sample sample) {
        return SampleResponse.builder()
                .id(sample.getId())
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

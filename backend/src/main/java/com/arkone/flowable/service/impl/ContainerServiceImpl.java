package com.arkone.flowable.service.impl;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.ContainerResponse;
import com.arkone.flowable.entity.Container;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.repository.ContainerMapper;
import com.arkone.flowable.service.ContainerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 容器服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final ContainerMapper containerMapper;

    @Override
    public Page<ContainerResponse> queryContainers(Integer pageNum, Integer pageSize, String containerType, String status) {
        // 构建分页对象
        Page<Container> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Container> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(containerType != null, Container::getContainerType, containerType)
                .eq(status != null, Container::getStatus, status)
                .orderByDesc(Container::getCreatedAt);

        // 执行查询
        Page<Container> containerPage = containerMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        Page<ContainerResponse> responsePage = new Page<>(containerPage.getCurrent(), containerPage.getSize(), containerPage.getTotal());
        responsePage.setRecords(containerPage.getRecords().stream()
                .map(this::convertToResponse)
                .toList());

        return responsePage;
    }

    @Override
    public ContainerResponse getContainerById(String id) {
        Container container = containerMapper.selectById(id);
        if (container == null || container.getDeleted()) {
            throw new BusinessException(ErrorCode.CONTAINER_NOT_FOUND);
        }
        return convertToResponse(container);
    }

    /**
     * 转换为响应DTO
     */
    private ContainerResponse convertToResponse(Container container) {
        return ContainerResponse.builder()
                .id(container.getId())
                .containerCode(container.getContainerCode())
                .containerType(container.getContainerType())
                .capacity(container.getCapacity())
                .usedCount(container.getUsedCount())
                .status(container.getStatus())
                .createdBy(container.getCreatedBy())
                .createdAt(container.getCreatedAt())
                .updatedBy(container.getUpdatedBy())
                .updatedAt(container.getUpdatedAt())
                .build();
    }
}

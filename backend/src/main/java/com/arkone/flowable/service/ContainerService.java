package com.arkone.flowable.service;

import com.arkone.flowable.dto.ContainerResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 容器服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface ContainerService {

    /**
     * 分页查询容器列表
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param containerType 容器类型(可选)
     * @param status        容器状态(可选)
     * @return 分页结果
     */
    Page<ContainerResponse> queryContainers(Integer pageNum, Integer pageSize, String containerType, String status);

    /**
     * 获取容器详情
     *
     * @param id 容器ID
     * @return 容器响应
     */
    ContainerResponse getContainerById(String id);
}

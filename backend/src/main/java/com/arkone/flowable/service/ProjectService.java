package com.arkone.flowable.service;

import com.arkone.flowable.dto.ProjectResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 项目服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface ProjectService {

    /**
     * 分页查询项目列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   项目状态(可选)
     * @return 分页结果
     */
    Page<ProjectResponse> queryProjects(Integer pageNum, Integer pageSize, String status);

    /**
     * 获取项目详情
     *
     * @param id 项目ID
     * @return 项目响应
     */
    ProjectResponse getProjectById(String id);
}

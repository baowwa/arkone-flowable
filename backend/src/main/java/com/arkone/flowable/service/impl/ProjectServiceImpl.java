package com.arkone.flowable.service.impl;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.ProjectResponse;
import com.arkone.flowable.entity.Project;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.repository.ProjectMapper;
import com.arkone.flowable.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 项目服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    @Override
    public Page<ProjectResponse> queryProjects(Integer pageNum, Integer pageSize, String status) {
        // 构建分页对象
        Page<Project> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Project::getStatus, status)
                .orderByDesc(Project::getCreatedAt);

        // 执行查询
        Page<Project> projectPage = projectMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        Page<ProjectResponse> responsePage = new Page<>(projectPage.getCurrent(), projectPage.getSize(), projectPage.getTotal());
        responsePage.setRecords(projectPage.getRecords().stream()
                .map(this::convertToResponse)
                .toList());

        return responsePage;
    }

    @Override
    public ProjectResponse getProjectById(String id) {
        Project project = projectMapper.selectById(id);
        if (project == null || project.getDeleted()) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return convertToResponse(project);
    }

    /**
     * 转换为响应DTO
     */
    private ProjectResponse convertToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectCode(project.getProjectCode())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .processDefinitionId(project.getProcessDefinitionId())
                .status(project.getStatus())
                .createdBy(project.getCreatedBy())
                .createdAt(project.getCreatedAt())
                .updatedBy(project.getUpdatedBy())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}

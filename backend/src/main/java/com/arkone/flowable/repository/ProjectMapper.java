package com.arkone.flowable.repository;

import com.arkone.flowable.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

/**
 * 项目数据访问层
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 根据项目编码查询项目
     *
     * @param projectCode 项目编码
     * @return 项目实体
     */
    default Project selectByProjectCode(String projectCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getProjectCode, projectCode));
    }

    /**
     * 根据流程定义ID查询项目列表
     *
     * @param processDefinitionId 流程定义ID
     * @return 项目列表
     */
    default java.util.List<Project> selectByProcessDefinitionId(String processDefinitionId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getProcessDefinitionId, processDefinitionId));
    }

    /**
     * 根据状态查询项目列表
     *
     * @param status 项目状态
     * @return 项目列表
     */
    default java.util.List<Project> selectByStatus(String status) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, status)
                .orderByDesc(Project::getCreatedAt));
    }
}

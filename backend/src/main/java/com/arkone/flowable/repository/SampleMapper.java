package com.arkone.flowable.repository;

import com.arkone.flowable.entity.Sample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 样本数据访问层
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Mapper
public interface SampleMapper extends BaseMapper<Sample> {

    /**
     * 根据样本编码查询样本
     *
     * @param sampleCode 样本编码
     * @return 样本实体
     */
    default Sample selectBySampleCode(String sampleCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getSampleCode, sampleCode));
    }

    /**
     * 根据项目ID查询样本列表
     *
     * @param projectId 项目ID
     * @return 样本列表
     */
    default java.util.List<Sample> selectByProjectId(String projectId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getProjectId, projectId)
                .orderByDesc(Sample::getCreatedAt));
    }

    /**
     * 根据容器ID查询样本列表
     *
     * @param containerId 容器ID
     * @return 样本列表
     */
    default java.util.List<Sample> selectByContainerId(String containerId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getContainerId, containerId)
                .orderByAsc(Sample::getPosition));
    }

    /**
     * 根据流程实例ID查询样本
     *
     * @param processInstanceId 流程实例ID
     * @return 样本实体
     */
    default Sample selectByProcessInstanceId(String processInstanceId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getProcessInstanceId, processInstanceId));
    }

    /**
     * 根据样本类型查询样本列表
     *
     * @param sampleType 样本类型
     * @return 样本列表
     */
    default java.util.List<Sample> selectBySampleType(String sampleType) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getSampleType, sampleType)
                .orderByDesc(Sample::getCreatedAt));
    }

    /**
     * 根据状态查询样本列表
     *
     * @param status 样本状态
     * @return 样本列表
     */
    default java.util.List<Sample> selectByStatus(String status) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getStatus, status)
                .orderByDesc(Sample::getCreatedAt));
    }

    /**
     * 根据父样本ID查询子样本列表
     *
     * @param parentSampleId 父样本ID
     * @return 子样本列表
     */
    default java.util.List<Sample> selectByParentSampleId(String parentSampleId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sample>()
                .eq(Sample::getParentSampleId, parentSampleId)
                .orderByDesc(Sample::getCreatedAt));
    }
}

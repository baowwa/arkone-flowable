package com.arkone.flowable.repository;

import com.arkone.flowable.entity.ProcessNodeData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程节点数据访问层
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Mapper
public interface ProcessNodeDataMapper extends BaseMapper<ProcessNodeData> {

    /**
     * 根据样本ID查询所有节点数据
     *
     * @param sampleId 样本ID
     * @return 节点数据列表
     */
    default java.util.List<ProcessNodeData> selectBySampleId(String sampleId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessNodeData>()
                .eq(ProcessNodeData::getSampleId, sampleId)
                .orderByAsc(ProcessNodeData::getCreatedAt));
    }

    /**
     * 根据样本ID和节点ID查询节点数据
     *
     * @param sampleId 样本ID
     * @param nodeId   节点ID
     * @return 节点数据实体
     */
    default ProcessNodeData selectBySampleIdAndNodeId(String sampleId, String nodeId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessNodeData>()
                .eq(ProcessNodeData::getSampleId, sampleId)
                .eq(ProcessNodeData::getNodeId, nodeId)
                .orderByDesc(ProcessNodeData::getCreatedAt)
                .last("LIMIT 1"));
    }

    /**
     * 根据节点ID查询所有节点数据
     *
     * @param nodeId 节点ID
     * @return 节点数据列表
     */
    default java.util.List<ProcessNodeData> selectByNodeId(String nodeId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessNodeData>()
                .eq(ProcessNodeData::getNodeId, nodeId)
                .orderByDesc(ProcessNodeData::getCreatedAt));
    }

    /**
     * 根据状态查询节点数据列表
     *
     * @param status 节点数据状态
     * @return 节点数据列表
     */
    default java.util.List<ProcessNodeData> selectByStatus(String status) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessNodeData>()
                .eq(ProcessNodeData::getStatus, status)
                .orderByDesc(ProcessNodeData::getCreatedAt));
    }

    /**
     * 根据样本ID列表批量查询节点数据
     *
     * @param sampleIds 样本ID列表
     * @return 节点数据列表
     */
    default java.util.List<ProcessNodeData> selectBySampleIds(java.util.List<String> sampleIds) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessNodeData>()
                .in(ProcessNodeData::getSampleId, sampleIds)
                .orderByAsc(ProcessNodeData::getSampleId)
                .orderByAsc(ProcessNodeData::getCreatedAt));
    }
}

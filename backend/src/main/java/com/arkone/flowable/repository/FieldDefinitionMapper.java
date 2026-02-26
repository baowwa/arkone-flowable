package com.arkone.flowable.repository;

import com.arkone.flowable.entity.FieldDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字段定义数据访问层
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Mapper
public interface FieldDefinitionMapper extends BaseMapper<FieldDefinition> {

    /**
     * 根据节点ID查询字段定义列表
     *
     * @param nodeId 节点ID
     * @return 字段定义列表
     */
    default java.util.List<FieldDefinition> selectByNodeId(String nodeId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FieldDefinition>()
                .eq(FieldDefinition::getNodeId, nodeId)
                .orderByAsc(FieldDefinition::getDisplayOrder));
    }

    /**
     * 根据节点ID和字段名称查询字段定义
     *
     * @param nodeId    节点ID
     * @param fieldName 字段名称
     * @return 字段定义实体
     */
    default FieldDefinition selectByNodeIdAndFieldName(String nodeId, String fieldName) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FieldDefinition>()
                .eq(FieldDefinition::getNodeId, nodeId)
                .eq(FieldDefinition::getFieldName, fieldName));
    }

    /**
     * 根据字段类型查询字段定义列表
     *
     * @param fieldType 字段类型
     * @return 字段定义列表
     */
    default java.util.List<FieldDefinition> selectByFieldType(String fieldType) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FieldDefinition>()
                .eq(FieldDefinition::getFieldType, fieldType)
                .orderByAsc(FieldDefinition::getNodeId)
                .orderByAsc(FieldDefinition::getDisplayOrder));
    }

    /**
     * 查询必填字段列表
     *
     * @param nodeId 节点ID
     * @return 必填字段列表
     */
    default java.util.List<FieldDefinition> selectRequiredFields(String nodeId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FieldDefinition>()
                .eq(FieldDefinition::getNodeId, nodeId)
                .eq(FieldDefinition::getRequired, true)
                .orderByAsc(FieldDefinition::getDisplayOrder));
    }

    /**
     * 查询包含计算公式的字段列表
     *
     * @param nodeId 节点ID
     * @return 计算字段列表
     */
    default java.util.List<FieldDefinition> selectCalculatedFields(String nodeId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FieldDefinition>()
                .eq(FieldDefinition::getNodeId, nodeId)
                .isNotNull(FieldDefinition::getFormula)
                .orderByAsc(FieldDefinition::getDisplayOrder));
    }
}

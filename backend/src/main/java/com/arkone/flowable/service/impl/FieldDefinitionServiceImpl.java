package com.arkone.flowable.service.impl;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.dto.FieldDefinitionResponse;
import com.arkone.flowable.entity.FieldDefinition;
import com.arkone.flowable.exception.BusinessException;
import com.arkone.flowable.repository.FieldDefinitionMapper;
import com.arkone.flowable.service.FieldDefinitionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字段定义服务实现类
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldDefinitionServiceImpl implements FieldDefinitionService {

    private final FieldDefinitionMapper fieldDefinitionMapper;

    @Override
    public List<FieldDefinitionResponse> getFieldsByNodeId(String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点ID不能为空");
        }

        // 查询字段定义
        LambdaQueryWrapper<FieldDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldDefinition::getNodeId, nodeId)
                .orderByAsc(FieldDefinition::getDisplayOrder);

        List<FieldDefinition> fieldDefinitions = fieldDefinitionMapper.selectList(wrapper);

        log.info("查询节点字段定义: nodeId={}, count={}", nodeId, fieldDefinitions.size());

        // 转换为响应DTO
        return fieldDefinitions.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * 转换为响应DTO
     */
    private FieldDefinitionResponse convertToResponse(FieldDefinition fieldDefinition) {
        return FieldDefinitionResponse.builder()
                .id(fieldDefinition.getId() != null ? fieldDefinition.getId().toString() : null)
                .nodeId(fieldDefinition.getNodeId())
                .fieldName(fieldDefinition.getFieldName())
                .fieldLabel(fieldDefinition.getFieldLabel())
                .fieldType(fieldDefinition.getFieldType())
                .unit(fieldDefinition.getUnit())
                .required(fieldDefinition.getRequired())
                .defaultValue(fieldDefinition.getDefaultValue())
                .validationRule(fieldDefinition.getValidationRule())
                .formula(fieldDefinition.getFormula())
                .options(fieldDefinition.getOptions())
                .displayOrder(fieldDefinition.getDisplayOrder())
                .createdAt(fieldDefinition.getCreatedAt())
                .updatedAt(fieldDefinition.getUpdatedAt())
                .build();
    }
}

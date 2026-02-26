package com.arkone.flowable.service;

import com.arkone.flowable.dto.FieldDefinitionResponse;

import java.util.List;

/**
 * 字段定义服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface FieldDefinitionService {

    /**
     * 根据节点ID获取字段定义列表
     *
     * @param nodeId 节点ID
     * @return 字段定义列表
     */
    List<FieldDefinitionResponse> getFieldsByNodeId(String nodeId);
}

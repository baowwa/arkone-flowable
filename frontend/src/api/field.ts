import { http } from './request'
import type { FieldDefinition, Container, NodeData } from '@/types'

// 字段API
export const fieldApi = {
  // 获取节点字段定义
  getNodeFields(nodeId: string) {
    return http.get<FieldDefinition[]>(`/fields/nodes/${nodeId}`)
  },

  // 获取节点字段定义(别名方法,与后端API对齐)
  getFieldsByNodeId(nodeId: string) {
    return http.get<FieldDefinition[]>(`/fields/nodes/${nodeId}`)
  },

  // 获取字段定义详情
  getFieldById(id: string) {
    return http.get<FieldDefinition>(`/fields/${id}`)
  },

  // 创建字段定义
  createField(data: Partial<FieldDefinition>) {
    return http.post<FieldDefinition>('/fields', data)
  },

  // 更新字段定义
  updateField(id: string, data: Partial<FieldDefinition>) {
    return http.put<FieldDefinition>(`/fields/${id}`, data)
  },

  // 删除字段定义
  deleteField(id: string) {
    return http.delete(`/fields/${id}`)
  },

  // 批量更新字段顺序
  updateFieldsOrder(nodeId: string, fieldIds: string[]) {
    return http.put(`/fields/nodes/${nodeId}/order`, { fieldIds })
  }
}

// 容器API
export const containerApi = {
  // 获取容器列表
  getContainers(params?: {
    containerType?: string
    status?: string
    page?: number
    size?: number
  }) {
    return http.get<Container[]>('/containers', { params })
  },

  // 获取容器详情
  getContainerById(id: string) {
    return http.get<Container>(`/containers/${id}`)
  },

  // 创建容器
  createContainer(data: {
    containerCode: string
    containerType: string
    capacity: number
  }) {
    return http.post<Container>('/containers', data)
  },

  // 更新容器
  updateContainer(id: string, data: Partial<Container>) {
    return http.put<Container>(`/containers/${id}`, data)
  },

  // 删除容器
  deleteContainer(id: string) {
    return http.delete(`/containers/${id}`)
  },

  // 获取容器孔位使用情况
  getContainerPositions(containerId: string) {
    return http.get(`/containers/${containerId}/positions`)
  },

  // 分配孔位
  allocatePosition(containerId: string, position: string, sampleId: string) {
    return http.post(`/containers/${containerId}/positions/${position}/allocate`, {
      sampleId
    })
  },

  // 释放孔位
  releasePosition(containerId: string, position: string) {
    return http.delete(`/containers/${containerId}/positions/${position}`)
  }
}

// 节点数据API
export const nodeDataApi = {
  // 获取样本节点数据
  getSampleNodeData(sampleId: string, nodeId?: string) {
    return http.get<NodeData[]>(`/samples/${sampleId}/node-data`, {
      params: { nodeId }
    })
  },

  // 保存节点数据
  saveNodeData(data: {
    sampleId: string
    nodeId: string
    nodeName: string
    data: Record<string, any>
  }) {
    return http.post<NodeData>('/node-data', data)
  },

  // 更新节点数据
  updateNodeData(id: string, data: Record<string, any>) {
    return http.put<NodeData>(`/node-data/${id}`, { data })
  },

  // 删除节点数据
  deleteNodeData(id: string) {
    return http.delete(`/node-data/${id}`)
  }
}

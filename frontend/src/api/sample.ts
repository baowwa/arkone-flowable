import { http } from './request'
import type {
  Sample,
  PageParams,
  PageResult,
  BatchOperationResult,
  SampleStatus
} from '@/types'

// 样本查询参数
export interface SampleQueryParams extends PageParams {
  sampleCode?: string
  sampleName?: string
  projectId?: string
  sampleType?: string
  status?: SampleStatus
  containerId?: string
  startDate?: string
  endDate?: string
}

// 创建样本参数
export interface CreateSampleParams {
  sampleName: string
  projectId: string
  sampleType: string
  containerId?: string
  position?: string
  description?: string
}

// 更新样本参数
export interface UpdateSampleParams {
  sampleName?: string
  status?: SampleStatus
  containerId?: string
  position?: string
}

// 批量创建样本参数
export interface BatchCreateSampleParams {
  samples: CreateSampleParams[]
  failureMode?: 'partial' | 'atomic'
}

// 批量更新状态参数(已废弃,使用方法参数)
export interface BatchUpdateStatusParams {
  sampleIds: string[]
  status: SampleStatus
  atomic?: boolean
}

// 样本API
export const sampleApi = {
  // 获取样本列表
  getSamples(params: SampleQueryParams) {
    return http.get<PageResult<Sample>>('/samples', { params })
  },

  // 获取样本详情
  getSampleById(id: string) {
    return http.get<Sample>(`/samples/${id}`)
  },

  // 创建样本
  createSample(data: CreateSampleParams) {
    return http.post<Sample>('/samples', data)
  },

  // 更新样本
  updateSample(id: string, data: UpdateSampleParams) {
    return http.put<Sample>(`/samples/${id}`, data)
  },

  // 更新样本状态
  updateSampleStatus(id: string, status: SampleStatus) {
    return http.put<Sample>(`/samples/${id}/status`, null, {
      params: { status }
    })
  },

  // 删除样本
  deleteSample(id: string) {
    return http.delete(`/samples/${id}`)
  },

  // 批量创建样本
  batchCreateSamples(data: BatchCreateSampleParams) {
    return http.post<BatchOperationResult<Sample>>('/batch/samples', data)
  },

  // 批量更新状态
  batchUpdateStatus(sampleIds: string[], status: SampleStatus) {
    return http.put<BatchOperationResult<Sample>>(
      '/batch/samples/status',
      sampleIds,
      { params: { status } }
    )
  },

  // 批量删除样本
  batchDeleteSamples(sampleIds: string[], atomic = false) {
    return http.delete<BatchOperationResult<Sample>>('/batch/samples', {
      data: { sampleIds, atomic }
    })
  },

  // 获取样本历史
  getSampleHistory(id: string) {
    return http.get(`/samples/${id}/history`)
  },

  // 导出样本
  exportSamples(params: SampleQueryParams) {
    return http.get('/samples/export', {
      params,
      responseType: 'blob'
    })
  }
}

import { http } from './request'
import type { Task, PageParams, PageResult, BatchResult } from '@/types'

// 启动流程参数
export interface StartProcessParams {
  processDefinitionKey: string
  businessKey: string
  variables?: Record<string, any>
}

// 完成任务参数
export interface CompleteTaskParams {
  taskId: string
  variables?: Record<string, any>
  data?: Record<string, any>
}

// 批量完成任务参数
export interface BatchCompleteTaskParams {
  tasks: CompleteTaskParams[]
  atomic?: boolean
}

// 任务查询参数
export interface TaskQueryParams extends PageParams {
  processDefinitionKey?: string
  assignee?: string
  candidateUser?: string
  candidateGroup?: string
  processInstanceId?: string
}

// 流程API
export const processApi = {
  // 启动流程
  startProcess(data: StartProcessParams) {
    return http.post('/process/start', data)
  },

  // 获取流程实例
  getProcessInstance(processInstanceId: string) {
    return http.get(`/process/instances/${processInstanceId}`)
  },

  // 获取流程定义列表
  getProcessDefinitions() {
    return http.get('/process/definitions')
  },

  // 获取流程定义详情
  getProcessDefinition(processDefinitionId: string) {
    return http.get(`/process/definitions/${processDefinitionId}`)
  },

  // 挂起流程实例
  suspendProcessInstance(processInstanceId: string) {
    return http.put(`/process/instances/${processInstanceId}/suspend`)
  },

  // 激活流程实例
  activateProcessInstance(processInstanceId: string) {
    return http.put(`/process/instances/${processInstanceId}/activate`)
  },

  // 取消流程实例
  cancelProcessInstance(processInstanceId: string, reason?: string) {
    return http.delete(`/process/instances/${processInstanceId}`, {
      data: { reason }
    })
  }
}

// 任务API
export const taskApi = {
  // 获取任务列表
  getTasks(params: TaskQueryParams) {
    return http.get<PageResult<Task>>('/tasks', { params })
  },

  // 获取任务详情
  getTaskById(taskId: string) {
    return http.get<Task>(`/tasks/${taskId}`)
  },

  // 完成任务
  completeTask(data: CompleteTaskParams) {
    return http.post(`/tasks/${data.taskId}/complete`, data)
  },

  // 批量完成任务
  batchCompleteTasks(data: BatchCompleteTaskParams) {
    return http.post<BatchResult[]>('/batch/tasks/complete', data)
  },

  // 认领任务
  claimTask(taskId: string, userId: string) {
    return http.post(`/tasks/${taskId}/claim`, { userId })
  },

  // 取消认领任务
  unclaimTask(taskId: string) {
    return http.post(`/tasks/${taskId}/unclaim`)
  },

  // 委派任务
  delegateTask(taskId: string, userId: string) {
    return http.post(`/tasks/${taskId}/delegate`, { userId })
  },

  // 转办任务
  assignTask(taskId: string, userId: string) {
    return http.post(`/tasks/${taskId}/assign`, { userId })
  }
}

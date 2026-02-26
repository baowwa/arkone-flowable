import { http } from './request'
import type { Project, PageParams, PageResult } from '@/types'

// 项目查询参数
export interface ProjectQueryParams extends PageParams {
  projectCode?: string
  projectName?: string
  status?: string
}

// 创建项目参数
export interface CreateProjectParams {
  projectCode: string
  projectName: string
  description?: string
  processDefinitionId?: string
}

// 项目API
export const projectApi = {
  // 获取项目列表
  getProjects(params: ProjectQueryParams) {
    return http.get<PageResult<Project>>('/projects', { params })
  },

  // 获取项目详情
  getProjectById(id: string) {
    return http.get<Project>(`/projects/${id}`)
  },

  // 创建项目
  createProject(data: CreateProjectParams) {
    return http.post<Project>('/projects', data)
  },

  // 更新项目
  updateProject(id: string, data: Partial<CreateProjectParams>) {
    return http.put<Project>(`/projects/${id}`, data)
  },

  // 删除项目
  deleteProject(id: string) {
    return http.delete(`/projects/${id}`)
  },

  // 激活项目
  activateProject(id: string) {
    return http.put(`/projects/${id}/activate`)
  },

  // 停用项目
  deactivateProject(id: string) {
    return http.put(`/projects/${id}/deactivate`)
  }
}

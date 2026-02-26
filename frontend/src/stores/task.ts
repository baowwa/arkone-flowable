import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Task } from '@/types'
import { taskApi, type TaskQueryParams } from '@/api/process'

export const useTaskStore = defineStore('task', () => {
  // 状态
  const tasks = ref<Task[]>([])
  const currentTask = ref<Task | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)

  // 计算属性
  const hasTasks = computed(() => tasks.value.length > 0)
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  // 获取任务列表
  async function fetchTasks(params: TaskQueryParams = {}) {
    loading.value = true
    try {
      const response = await taskApi.getTasks({
        page: currentPage.value,
        size: pageSize.value,
        ...params
      })
      tasks.value = response.data.content
      total.value = response.data.totalElements
      currentPage.value = response.data.number
    } catch (error) {
      console.error('获取任务列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取任务详情
  async function fetchTaskById(taskId: string) {
    loading.value = true
    try {
      const response = await taskApi.getTaskById(taskId)
      currentTask.value = response.data
      return response.data
    } catch (error) {
      console.error('获取任务详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 完成任务
  async function completeTask(taskId: string, data?: any) {
    loading.value = true
    try {
      await taskApi.completeTask({
        taskId,
        data
      })
      // 从列表中移除已完成的任务
      tasks.value = tasks.value.filter(t => t.id !== taskId)
      total.value--
      if (currentTask.value?.id === taskId) {
        currentTask.value = null
      }
    } catch (error) {
      console.error('完成任务失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 批量完成任务
  async function batchCompleteTasks(tasksData: any[], atomic = false) {
    loading.value = true
    try {
      const response = await taskApi.batchCompleteTasks({
        tasks: tasksData,
        atomic
      })
      // 从列表中移除已完成的任务
      const completedTaskIds = tasksData.map(t => t.taskId)
      tasks.value = tasks.value.filter(t => !completedTaskIds.includes(t.id))
      total.value -= completedTaskIds.length
      return response.data
    } catch (error) {
      console.error('批量完成任务失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 认领任务
  async function claimTask(taskId: string, userId: string) {
    loading.value = true
    try {
      await taskApi.claimTask(taskId, userId)
      const task = tasks.value.find(t => t.id === taskId)
      if (task) {
        task.assignee = userId
      }
      if (currentTask.value?.id === taskId) {
        currentTask.value.assignee = userId
      }
    } catch (error) {
      console.error('认领任务失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 设置当前页
  function setCurrentPage(page: number) {
    currentPage.value = page
  }

  // 设置每页大小
  function setPageSize(size: number) {
    pageSize.value = size
    currentPage.value = 1
  }

  // 重置状态
  function reset() {
    tasks.value = []
    currentTask.value = null
    loading.value = false
    total.value = 0
    currentPage.value = 1
    pageSize.value = 20
  }

  return {
    // 状态
    tasks,
    currentTask,
    loading,
    total,
    currentPage,
    pageSize,
    // 计算属性
    hasTasks,
    totalPages,
    // 方法
    fetchTasks,
    fetchTaskById,
    completeTask,
    batchCompleteTasks,
    claimTask,
    setCurrentPage,
    setPageSize,
    reset
  }
})

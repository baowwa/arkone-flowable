import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Sample, SampleStatus } from '@/types'
import { sampleApi, type SampleQueryParams } from '@/api/sample'

export const useSampleStore = defineStore('sample', () => {
  // 状态
  const samples = ref<Sample[]>([])
  const currentSample = ref<Sample | null>(null)
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)

  // 计算属性
  const hasSamples = computed(() => samples.value.length > 0)
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  // 获取样本列表
  async function fetchSamples(params: SampleQueryParams = {}) {
    loading.value = true
    try {
      const response = await sampleApi.getSamples({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        ...params
      })
      // MyBatis-Plus Page 对象格式: records, total, current, size
      samples.value = response.data.records || []
      total.value = response.data.total || 0
      currentPage.value = response.data.current || 1
    } catch (error) {
      console.error('获取样本列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取样本详情
  async function fetchSampleById(id: string) {
    loading.value = true
    try {
      const response = await sampleApi.getSampleById(id)
      currentSample.value = response.data
      return response.data
    } catch (error) {
      console.error('获取样本详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建样本
  async function createSample(data: any) {
    loading.value = true
    try {
      const response = await sampleApi.createSample(data)
      samples.value.unshift(response.data)
      total.value++
      return response.data
    } catch (error) {
      console.error('创建样本失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新样本
  async function updateSample(id: string, data: any) {
    loading.value = true
    try {
      const response = await sampleApi.updateSample(id, data)
      const index = samples.value.findIndex(s => s.id === id)
      if (index !== -1) {
        samples.value[index] = response.data
      }
      if (currentSample.value?.id === id) {
        currentSample.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('更新样本失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除样本
  async function deleteSample(id: string) {
    loading.value = true
    try {
      await sampleApi.deleteSample(id)
      samples.value = samples.value.filter(s => s.id !== id)
      total.value--
      if (currentSample.value?.id === id) {
        currentSample.value = null
      }
    } catch (error) {
      console.error('删除样本失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 批量创建样本
  async function batchCreateSamples(samplesData: any[], failureMode: 'partial' | 'atomic' = 'partial') {
    loading.value = true
    try {
      const response = await sampleApi.batchCreateSamples({
        samples: samplesData,
        failureMode
      })
      return response.data
    } catch (error) {
      console.error('批量创建样本失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 批量更新状态
  async function batchUpdateStatus(sampleIds: string[], status: SampleStatus) {
    loading.value = true
    try {
      const response = await sampleApi.batchUpdateStatus(sampleIds, status)
      // 更新本地状态
      samples.value.forEach(sample => {
        if (sampleIds.includes(sample.id)) {
          sample.status = status
        }
      })
      return response.data
    } catch (error) {
      console.error('批量更新状态失败:', error)
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
    samples.value = []
    currentSample.value = null
    loading.value = false
    total.value = 0
    currentPage.value = 1
    pageSize.value = 20
  }

  return {
    // 状态
    samples,
    currentSample,
    loading,
    total,
    currentPage,
    pageSize,
    // 计算属性
    hasSamples,
    totalPages,
    // 方法
    fetchSamples,
    fetchSampleById,
    createSample,
    updateSample,
    deleteSample,
    batchCreateSamples,
    batchUpdateStatus,
    setCurrentPage,
    setPageSize,
    reset
  }
})

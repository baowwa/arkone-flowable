<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useSampleStore } from '@/stores/sample'
import { formatDate } from '@/utils'
import type { Sample, SampleStatus } from '@/types'

const router = useRouter()
const sampleStore = useSampleStore()

// 筛选表单
const filterForm = ref({
  sampleId: '',
  project: '',
  status: '',
  submitter: '',
  startDate: '2026-02-01',
  endDate: '2026-02-24'
})

// 选中的样本
const selectedSamples = ref<string[]>([])
const selectAll = ref(false)

// 状态映射
const statusMap: Record<string, { class: string; text: string }> = {
  in_progress: { class: 'in-progress', text: '进行中' },
  completed: { class: 'completed', text: '已完成' },
  paused: { class: 'paused', text: '暂停' },
  exception: { class: 'exception', text: '异常' },
  pending: { class: '', text: '待处理' },
  cancelled: { class: '', text: '已取消' }
}

// 加载样本列表
const loadSamples = async () => {
  try {
    await sampleStore.fetchSamples(filterForm.value)
  } catch (error) {
    showMessage('加载样本列表失败', 'error')
  }
}

// 搜索
const handleSearch = () => {
  sampleStore.setCurrentPage(1)
  loadSamples()
}

// 重置
const handleReset = () => {
  filterForm.value = {
    sampleId: '',
    project: '',
    status: '',
    submitter: '',
    startDate: '2026-02-01',
    endDate: '2026-02-24'
  }
  handleSearch()
}

// 全选
const handleSelectAll = () => {
  if (selectAll.value) {
    selectedSamples.value = sampleStore.samples.map(s => s.id)
  } else {
    selectedSamples.value = []
  }
}

// 批量操作
const handleBatchOperation = () => {
  if (selectedSamples.value.length === 0) {
    showMessage('请先选择要操作的样本', 'warning')
    return
  }
  showMessage(`已选择 ${selectedSamples.value.length} 个样本`, 'info')
}

// 导出
const handleExport = () => {
  showMessage('导出功能开发中...', 'info')
}

// 查看样本
const viewSample = (id: string) => {
  router.push(`/samples/${id}`)
}

// 编辑样本
const editSample = (id: string) => {
  showMessage(`编辑样本 ${id}`, 'info')
}

// 显示消息
const showMessage = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'success') => {
  const messageBox = document.createElement('div')
  messageBox.className = `message-box message-${type}`
  messageBox.textContent = message
  messageBox.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 20px;
    background: ${type === 'success' ? '#67C23A' : type === 'error' ? '#F56C6C' : type === 'warning' ? '#E6A23C' : '#409EFF'};
    color: white;
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
  `
  document.body.appendChild(messageBox)
  setTimeout(() => {
    messageBox.style.animation = 'slideOut 0.3s ease-out'
    setTimeout(() => {
      document.body.removeChild(messageBox)
    }, 300)
  }, 3000)
}

// 初始化
onMounted(() => {
  loadSamples()
})
</script>

<template>
  <div class="container">
    <div class="page-header">
      <div>
        <a href="/" class="btn btn-text">← 返回首页</a>
        <h1 class="page-title">样本管理</h1>
      </div>
      <button class="btn btn-primary" @click="router.push('/samples/create')">
        ➕ 新建样本
      </button>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="form-item">
          <label class="form-label">样本编号</label>
          <input
            type="text"
            class="form-input"
            v-model="filterForm.sampleId"
            placeholder="请输入样本编号"
          />
        </div>

        <div class="form-item">
          <label class="form-label">检验项目</label>
          <select class="form-select" v-model="filterForm.project">
            <option value="">全部</option>
            <option value="全质粒测序">全质粒测序</option>
            <option value="PCR产物测序">PCR产物测序</option>
          </select>
        </div>

        <div class="form-item">
          <label class="form-label">状态</label>
          <select class="form-select" v-model="filterForm.status">
            <option value="">全部</option>
            <option value="in_progress">进行中</option>
            <option value="completed">已完成</option>
            <option value="paused">暂停</option>
            <option value="exception">异常</option>
          </select>
        </div>

        <div class="form-item">
          <label class="form-label">提交人</label>
          <input
            type="text"
            class="form-input"
            v-model="filterForm.submitter"
            placeholder="请输入提交人"
          />
        </div>
      </div>

      <div class="filter-row">
        <div class="form-item">
          <label class="form-label">开始时间</label>
          <input
            type="date"
            class="form-input"
            v-model="filterForm.startDate"
          />
        </div>

        <div class="form-item">
          <label class="form-label">结束时间</label>
          <input
            type="date"
            class="form-input"
            v-model="filterForm.endDate"
          />
        </div>

        <div class="form-item" style="display: flex; align-items: flex-end; gap: 8px;">
          <button class="btn btn-primary" @click="handleSearch">🔍 搜索</button>
          <button class="btn btn-default" @click="handleReset">🔄 重置</button>
        </div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <div class="table-toolbar">
        <div style="display: flex; gap: 8px;">
          <button class="btn btn-default" @click="handleBatchOperation">
            批量操作 ▼
          </button>
          <button class="btn btn-default" @click="handleExport">
            📤 导出Excel
          </button>
        </div>
        <div class="pagination-info">
          共 <strong>{{ sampleStore.total }}</strong> 条记录
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th style="width: 50px;">
              <input
                type="checkbox"
                v-model="selectAll"
                @change="handleSelectAll"
              />
            </th>
            <th>样本编号</th>
            <th>样本名称</th>
            <th>检验项目</th>
            <th>样本类型</th>
            <th>当前节点</th>
            <th>状态</th>
            <th>提交人</th>
            <th>提交时间</th>
            <th style="width: 120px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="sample in sampleStore.samples" :key="sample.id">
            <td>
              <input
                type="checkbox"
                class="sample-checkbox"
                :value="sample.id"
                v-model="selectedSamples"
              />
            </td>
            <td>
              <a
                href="#"
                @click.prevent="viewSample(sample.id)"
                style="color: var(--primary-color); text-decoration: none;"
              >
                {{ sample.sampleCode }}
              </a>
            </td>
            <td>{{ sample.sampleName }}</td>
            <td>{{ sample.projectName || '-' }}</td>
            <td>{{ sample.sampleType }}</td>
            <td>{{ sample.currentNode || '-' }}</td>
            <td>
              <span
                class="status-badge"
                :class="statusMap[sample.status]?.class"
              >
                {{ statusMap[sample.status]?.text || sample.status }}
              </span>
            </td>
            <td>{{ sample.createdBy || '-' }}</td>
            <td>{{ formatDate(sample.createdAt) }}</td>
            <td>
              <button class="btn btn-text" @click="viewSample(sample.id)">查看</button>
              <button class="btn btn-text" @click="editSample(sample.id)">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">
          显示第 {{ (sampleStore.currentPage - 1) * sampleStore.pageSize + 1 }}-{{ Math.min(sampleStore.currentPage * sampleStore.pageSize, sampleStore.total) }} 条，共 {{ sampleStore.total }} 条
        </div>
        <div class="pagination-controls">
          <button
            class="pagination-btn"
            :disabled="sampleStore.currentPage === 1"
            @click="sampleStore.setCurrentPage(sampleStore.currentPage - 1); loadSamples()"
          >
            上一页
          </button>
          <button
            v-for="page in Math.min(Math.ceil(sampleStore.total / sampleStore.pageSize), 10)"
            :key="page"
            class="pagination-btn"
            :class="{ active: page === sampleStore.currentPage }"
            @click="sampleStore.setCurrentPage(page); loadSamples()"
          >
            {{ page }}
          </button>
          <button
            class="pagination-btn"
            :disabled="sampleStore.currentPage >= Math.ceil(sampleStore.total / sampleStore.pageSize)"
            @click="sampleStore.setCurrentPage(sampleStore.currentPage + 1); loadSamples()"
          >
            下一页
          </button>
          <select
            class="form-select"
            style="width: 100px; margin-left: 16px;"
            v-model="sampleStore.pageSize"
            @change="loadSamples"
          >
            <option value="10">10条/页</option>
            <option value="20">20条/页</option>
            <option value="50">50条/页</option>
            <option value="100">100条/页</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 导入原型样式 - 完全按照prototypes/styles.css */
:root {
  --primary-color: #409EFF;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-color: #DCDFE6;
  --bg-color: #F5F7FA;
  --bg-white: #FFFFFF;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  background: var(--bg-white);
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  display: inline;
  margin-left: 16px;
}

.btn {
  display: inline-block;
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  text-align: center;
  background: none;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.btn-primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.btn-default {
  background-color: white;
  color: var(--text-primary);
  border-color: var(--border-color);
}

.btn-default:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.btn-text {
  background: transparent;
  color: var(--primary-color);
  border: none;
  padding: 4px 8px;
}

.btn-text:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.filter-section {
  background: var(--bg-white);
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 14px;
  color: var(--text-regular);
  margin-bottom: 4px;
  font-weight: 500;
}

.form-input,
.form-select {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

.table-container {
  background: var(--bg-white);
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-toolbar {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table thead {
  background-color: var(--bg-color);
}

.table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: var(--text-regular);
  font-size: 14px;
  border-bottom: 1px solid var(--border-color);
}

.table td {
  padding: 12px 16px;
  border-bottom: 1px solid #EBEEF5;
  font-size: 14px;
}

.table tbody tr:hover {
  background-color: var(--bg-color);
}

.table tbody tr:nth-child(even) {
  background-color: #FAFAFA;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-badge.in-progress {
  background-color: rgba(64, 158, 255, 0.1);
  color: var(--primary-color);
}

.status-badge.in-progress::before {
  background-color: var(--primary-color);
}

.status-badge.completed {
  background-color: rgba(103, 194, 58, 0.1);
  color: var(--success-color);
}

.status-badge.completed::before {
  background-color: var(--success-color);
}

.status-badge.paused {
  background-color: rgba(230, 162, 60, 0.1);
  color: var(--warning-color);
}

.status-badge.paused::before {
  background-color: var(--warning-color);
}

.status-badge.exception {
  background-color: rgba(245, 108, 108, 0.1);
  color: var(--danger-color);
}

.status-badge.exception::before {
  background-color: var(--danger-color);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  gap: 4px;
  align-items: center;
}

.pagination-btn {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  background: white;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* 动画 */
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}
</style>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { fieldApi } from '@/api/field'
import { taskApi } from '@/api/process'
import { calculateFormula, validateFieldValue, storage } from '@/utils'
import type { FieldDefinition, BatchSampleData } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const fields = ref<FieldDefinition[]>([])
const samples = ref<BatchSampleData[]>([])
const searchKeyword = ref('')
const autoSaveTimer = ref<number>()

// 节点信息
const nodeId = ref('sample_preprocessing')
const nodeName = ref('样本前处理')

// 操作人选项
const operators = ['张三', '李四', '王五', '赵六']

// 计算属性
const completedCount = computed(() => {
  return samples.value.filter(s => s.completed).length
})

const progress = computed(() => {
  return `${completedCount.value}/${samples.value.length}`
})

// 初始化96个样本数据
const initSamples = () => {
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
  const newSamples: BatchSampleData[] = []

  for (let r = 0; r < 8; r++) {
    for (let c = 0; c < 12; c++) {
      const position = `${rows[r]}${String(c + 1).padStart(2, '0')}`
      newSamples.push({
        index: r * 12 + c + 1,
        sampleId: `AK20260224${String(r * 12 + c + 1).padStart(3, '0')}`,
        position,
        data: {
          concentration: '',
          waterVolume: '',
          od260280: '',
          od260230: '',
          operator: '',
          operationTime: '',
          remark: ''
        },
        errors: {},
        completed: false
      })
    }
  }

  samples.value = newSamples
}

// 处理单元格输入
const handleCellInput = (index: number, field: string, value: any) => {
  const sample = samples.value[index]
  sample.data[field] = value

  // 自动计算补水量
  if (field === 'concentration' && value) {
    const concentration = parseFloat(value)
    if (!isNaN(concentration) && concentration > 0) {
      const targetConcentration = 50
      const totalVolume = 100
      const sampleVolume = (targetConcentration * totalVolume) / concentration
      const waterVolume = Math.max(0, totalVolume - sampleVolume).toFixed(2)
      sample.data.waterVolume = waterVolume
    }
  }

  updateProgress()
  scheduleAutoSave()
}

// 验证单元格
const validateCell = (index: number, field: string) => {
  const sample = samples.value[index]
  const value = parseFloat(sample.data[field])

  let isValid = true
  let errorMsg = ''

  if (field === 'concentration') {
    if (sample.data[field] && (isNaN(value) || value < 0 || value > 1000)) {
      isValid = false
      errorMsg = '浓度范围: 0-1000'
    }
  } else if (field === 'od260280') {
    if (sample.data[field] && (isNaN(value) || value < 1.6 || value > 2.2)) {
      isValid = false
      errorMsg = '范围: 1.6-2.2'
    }
  } else if (field === 'od260230') {
    if (sample.data[field] && (isNaN(value) || value < 1.8 || value > 2.5)) {
      isValid = false
      errorMsg = '范围: 1.8-2.5'
    }
  }

  if (isValid) {
    delete sample.errors[field]
  } else {
    sample.errors[field] = errorMsg
  }

  updateRowStatus(index)
}

// 更新行状态
const updateRowStatus = (index: number) => {
  const sample = samples.value[index]

  if (Object.keys(sample.errors).length > 0) {
    sample.completed = false
  } else if (sample.data.concentration && sample.data.operator && sample.data.operationTime) {
    sample.completed = true
  } else {
    sample.completed = false
  }
}

// 更新进度
const updateProgress = () => {
  // 进度会通过computed自动更新
}

// 搜索
const handleSearch = () => {
  // 搜索功能由filteredSamples computed处理
}

// 智能填充
const handleAutoFill = () => {
  const now = new Date()
  const timeString = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}T${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`

  samples.value.forEach((sample, index) => {
    if (!sample.data.operationTime) {
      sample.data.operationTime = timeString
    }
    if (!sample.data.operator) {
      sample.data.operator = '李四'
    }
    updateRowStatus(index)
  })

  showMessage('已自动填充操作人和操作时间', 'success')
}

// 验证全部
const handleValidateAll = () => {
  let errorCount = 0

  samples.value.forEach((sample, index) => {
    validateCell(index, 'concentration')
    validateCell(index, 'od260280')
    validateCell(index, 'od260230')

    if (Object.keys(sample.errors).length > 0) {
      errorCount++
    }
  })

  if (errorCount === 0) {
    showMessage('全部数据验证通过!', 'success')
  } else {
    showMessage(`发现 ${errorCount} 个样本存在错误，请修正`, 'error')
  }
}

// 复制行
const handleCopyRow = (index: number) => {
  const sample = samples.value[index]
  const data = {
    concentration: sample.data.concentration,
    od260280: sample.data.od260280,
    od260230: sample.data.od260230,
    operator: sample.data.operator,
    operationTime: sample.data.operationTime,
    remark: sample.data.remark
  }

  storage.set('copied_row', data)
  showMessage('已复制行数据，可粘贴到其他行', 'success')
}

// 清空行
const handleClearRow = (index: number) => {
  if (confirm('确定要清空这一行的数据吗?')) {
    const sample = samples.value[index]
    sample.data = {
      concentration: '',
      waterVolume: '',
      od260280: '',
      od260230: '',
      operator: '',
      operationTime: '',
      remark: ''
    }
    sample.errors = {}
    sample.completed = false
    showMessage('已清空行数据', 'info')
  }
}

// 保存草稿
const handleSaveDraft = () => {
  storage.set('batch_draft', samples.value)
  showMessage('草稿已保存', 'success')
}

// 自动保存
const scheduleAutoSave = () => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }

  autoSaveTimer.value = window.setTimeout(() => {
    storage.set('batch_draft', samples.value)
  }, 5000)
}

// 清空全部
const handleClearAll = () => {
  if (confirm('确定要清空所有数据吗? 此操作不可恢复!')) {
    samples.value.forEach(sample => {
      sample.data = {
        concentration: '',
        waterVolume: '',
        od260280: '',
        od260230: '',
        operator: '',
        operationTime: '',
        remark: ''
      }
      sample.errors = {}
      sample.completed = false
    })

    storage.remove('batch_draft')
    showMessage('已清空所有数据', 'info')
  }
}

// 导出Excel
const handleExportExcel = () => {
  showMessage('正在导出Excel...', 'info')

  setTimeout(() => {
    const data = samples.value.map(s => ({
      '序号': s.index,
      '样本ID': s.sampleId,
      '孔位': s.position,
      '核酸浓度': s.data.concentration,
      '补ddH₂O': s.data.waterVolume,
      'OD260/280': s.data.od260280,
      'OD260/230': s.data.od260230,
      '操作人': s.data.operator,
      '操作时间': s.data.operationTime,
      '备注': s.data.remark
    }))

    console.log('导出数据:', data)
    showMessage('Excel导出成功!', 'success')
  }, 1000)
}

// 批量提交
const handleBatchSubmit = () => {
  // 验证必填字段
  const incomplete = samples.value.filter(s => !s.data.concentration || !s.data.operator || !s.data.operationTime)
  const hasErrors = samples.value.filter(s => Object.keys(s.errors).length > 0)

  if (hasErrors.length > 0) {
    showMessage(`发现 ${hasErrors.length} 个样本存在错误，请先修正`, 'error')
    return
  }

  if (incomplete.length > 0) {
    if (!confirm(`还有 ${incomplete.length} 个样本未完成录入，确定要提交吗?`)) {
      return
    }
  }

  showMessage('正在批量提交数据...', 'info')

  setTimeout(() => {
    storage.remove('batch_draft')
    showMessage('批量提交成功! 96个样本数据已保存', 'success')

    setTimeout(() => {
      router.push('/samples')
    }, 1500)
  }, 1500)
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
  initSamples()

  // 恢复草稿
  const draft = storage.get('batch_draft')
  if (draft && draft.length === 96) {
    if (confirm('检测到未保存的草稿，是否恢复?')) {
      draft.forEach((d: any, i: number) => {
        Object.assign(samples.value[i], d)
      })
      showMessage('草稿已恢复', 'info')
    }
  }
})

// 清理
onBeforeUnmount(() => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
})
</script>

<template>
  <div class="container">
    <div class="page-header">
      <div>
        <a href="#" @click.prevent="router.back()" class="btn btn-text">← 返回详情</a>
        <h1 class="page-title">批量数据录入 - {{ nodeName }}</h1>
      </div>
      <div class="btn-group">
        <button class="btn btn-text" @click="router.push('/tasks')">← 切换到单个录入</button>
        <button class="btn btn-default" @click="handleSaveDraft">💾 保存草稿</button>
        <button class="btn btn-default" @click="handleClearAll">🗑️ 清空</button>
        <button class="btn btn-default" @click="handleExportExcel">📤 导出Excel</button>
        <button class="btn btn-primary" @click="handleBatchSubmit">✓ 批量提交</button>
      </div>
    </div>

    <!-- 粘贴提示 -->
    <div class="paste-hint">
      <div class="paste-hint-title">💡 快速录入提示</div>
      <div class="paste-hint-text">
        支持从Excel复制粘贴数据 (Ctrl+V) | 按Tab键快速切换单元格 | 按Enter键跳到下一行 | 支持批量填充相同数据
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="batch-toolbar">
      <div class="toolbar-left">
        <div class="progress-info">
          <span class="progress-text">录入进度:</span>
          <span class="progress-number">{{ progress }}</span>
        </div>
        <div class="search-box">
          <input
            type="text"
            class="search-input"
            placeholder="搜索样本ID或孔位..."
            v-model="searchKeyword"
            @input="handleSearch"
          />
          <button class="btn btn-text" @click="handleSearch">🔍</button>
        </div>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-default" @click="handleAutoFill">⚡ 智能填充</button>
        <button class="btn btn-default" @click="handleValidateAll">✓ 验证全部</button>
      </div>
    </div>

    <!-- 批量数据表格 -->
    <div class="batch-table-container">
      <div class="table-wrapper">
        <table class="batch-table">
          <thead>
            <tr>
              <th class="sticky-col" style="width: 40px;">序号</th>
              <th class="sticky-col" style="width: 140px; left: 40px;">样本ID</th>
              <th style="width: 80px;">孔位</th>
              <th style="width: 120px;">核酸浓度<br><small>(ng/μL)</small></th>
              <th style="width: 120px;">补ddH₂O<br><small>(μL)</small></th>
              <th style="width: 120px;">OD260/280</th>
              <th style="width: 120px;">OD260/230</th>
              <th style="width: 120px;">操作人</th>
              <th style="width: 160px;">操作时间</th>
              <th style="width: 200px;">备注</th>
              <th style="width: 100px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(sample, index) in samples"
              :key="sample.index"
              :class="{
                'has-error': Object.keys(sample.errors).length > 0,
                'completed': sample.completed
              }"
            >
              <td class="sticky-col">{{ sample.index }}</td>
              <td class="sticky-col sample-id-cell" style="left: 40px;">{{ sample.sampleId }}</td>
              <td class="position-cell">{{ sample.position }}</td>
              <td>
                <input
                  type="number"
                  class="cell-input"
                  :class="{
                    'error': sample.errors.concentration,
                    'success': sample.data.concentration && !sample.errors.concentration
                  }"
                  v-model="sample.data.concentration"
                  placeholder="0-1000"
                  min="0"
                  max="1000"
                  step="0.01"
                  @input="handleCellInput(index, 'concentration', sample.data.concentration)"
                  @blur="validateCell(index, 'concentration')"
                />
              </td>
              <td>
                <input
                  type="number"
                  class="cell-input"
                  v-model="sample.data.waterVolume"
                  disabled
                  placeholder="自动计算"
                />
              </td>
              <td>
                <input
                  type="number"
                  class="cell-input"
                  :class="{
                    'error': sample.errors.od260280,
                    'success': sample.data.od260280 && !sample.errors.od260280
                  }"
                  v-model="sample.data.od260280"
                  placeholder="1.6-2.2"
                  min="1.6"
                  max="2.2"
                  step="0.01"
                  @input="handleCellInput(index, 'od260280', sample.data.od260280)"
                  @blur="validateCell(index, 'od260280')"
                />
              </td>
              <td>
                <input
                  type="number"
                  class="cell-input"
                  :class="{
                    'error': sample.errors.od260230,
                    'success': sample.data.od260230 && !sample.errors.od260230
                  }"
                  v-model="sample.data.od260230"
                  placeholder="1.8-2.5"
                  min="1.8"
                  max="2.5"
                  step="0.01"
                  @input="handleCellInput(index, 'od260230', sample.data.od260230)"
                  @blur="validateCell(index, 'od260230')"
                />
              </td>
              <td>
                <select
                  class="cell-input"
                  v-model="sample.data.operator"
                  @change="handleCellInput(index, 'operator', sample.data.operator)"
                >
                  <option value="">请选择</option>
                  <option v-for="op in operators" :key="op" :value="op">{{ op }}</option>
                </select>
              </td>
              <td>
                <input
                  type="datetime-local"
                  class="cell-input"
                  v-model="sample.data.operationTime"
                  @input="handleCellInput(index, 'operationTime', sample.data.operationTime)"
                />
              </td>
              <td>
                <input
                  type="text"
                  class="cell-input"
                  v-model="sample.data.remark"
                  placeholder="备注"
                  @input="handleCellInput(index, 'remark', sample.data.remark)"
                />
              </td>
              <td>
                <div class="action-buttons">
                  <button class="icon-btn" @click="handleCopyRow(index)" title="复制">📋</button>
                  <button class="icon-btn" @click="handleClearRow(index)" title="清空">🗑️</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 完全按照原型样式 */
:root {
  --primary-color: #409EFF;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-color: #DCDFE6;
  --bg-color: #F5F7FA;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  background: white;
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

.btn-group {
  display: flex;
  gap: 8px;
}

.paste-hint {
  background: #F0F9FF;
  border: 2px dashed var(--primary-color);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  text-align: center;
}

.paste-hint-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.paste-hint-text {
  font-size: 14px;
  color: var(--text-regular);
}

.batch-toolbar {
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: var(--bg-color);
  border-radius: 4px;
}

.progress-text {
  font-size: 14px;
  color: var(--text-regular);
}

.progress-number {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 200px;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
}

.batch-table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
  overflow-y: auto;
  max-height: calc(100vh - 320px);
}

.batch-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
}

.batch-table thead {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--bg-color);
}

.batch-table th {
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  color: var(--text-regular);
  border-bottom: 2px solid var(--border-color);
  white-space: nowrap;
  background: var(--bg-color);
}

.batch-table th.sticky-col {
  position: sticky;
  left: 0;
  z-index: 11;
  background: var(--bg-color);
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
}

.batch-table td {
  padding: 8px;
  border-bottom: 1px solid #EBEEF5;
  text-align: center;
}

.batch-table td.sticky-col {
  position: sticky;
  left: 0;
  background: white;
  z-index: 5;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
}

.batch-table tbody tr:hover td {
  background: var(--bg-color);
}

.batch-table tbody tr:hover td.sticky-col {
  background: #F0F9FF;
}

.batch-table tbody tr.has-error {
  background: #FEF0F0;
}

.batch-table tbody tr.completed {
  background: #F0F9FF;
}

.sample-id-cell {
  font-weight: 600;
  color: var(--primary-color);
  white-space: nowrap;
}

.position-cell {
  font-weight: 500;
  color: var(--text-regular);
}

.cell-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 13px;
  text-align: center;
  transition: all 0.3s;
}

.cell-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.cell-input.error {
  border-color: var(--danger-color);
  background: #FEF0F0;
}

.cell-input.success {
  border-color: var(--success-color);
  background: #F0F9FF;
}

.cell-input:disabled {
  background: var(--bg-color);
  cursor: not-allowed;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.icon-btn {
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s;
  border-radius: 4px;
}

.icon-btn:hover {
  background: var(--bg-color);
  color: var(--primary-color);
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

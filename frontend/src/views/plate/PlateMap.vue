<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { containerApi } from '@/api/field'
import { generatePosition } from '@/utils'
import type { WellPosition, Container } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const containers = ref<Container[]>([])
const selectedContainerId = ref('')
const currentContainer = ref<Container | null>(null)
const positions = ref<WellPosition[]>([])
const selectedPosition = ref<string | null>(null)
const plateNumber = ref('')
const scale = ref(1)

// Canvas
const canvasRef = ref<HTMLCanvasElement>()
let ctx: CanvasRenderingContext2D | null = null

// 板位配置
const plateConfig = ref({
  type: '96孔板',
  rows: 8,
  cols: 12,
  wellSize: 40,
  gap: 10,
  offsetX: 80,
  offsetY: 60
})

// 计算属性
const usedCount = computed(() => {
  return positions.value.filter(p => p.status === 'used').length
})

const emptyCount = computed(() => {
  return positions.value.filter(p => p.status === 'empty').length
})

const usageRate = computed(() => {
  const total = positions.value.length
  return total > 0 ? ((usedCount.value / total) * 100).toFixed(1) : '0.0'
})

const selectedPositionInfo = computed(() => {
  if (!selectedPosition.value) return null
  return positions.value.find(p => p.position === selectedPosition.value)
})

// 加载容器列表
const loadContainers = async () => {
  try {
    const response = await containerApi.getContainers({
      containerType: '96孔板',
      status: 'active'
    })
    containers.value = response.data
    if (containers.value.length > 0 && !selectedContainerId.value) {
      selectedContainerId.value = containers.value[0].id
      await loadContainerData()
    }
  } catch (error) {
    showMessage('加载容器列表失败', 'error')
  }
}

// 加载容器数据
const loadContainerData = async () => {
  if (!selectedContainerId.value) return

  try {
    loading.value = true

    // 获取容器详情
    const containerResponse = await containerApi.getContainerById(selectedContainerId.value)
    currentContainer.value = containerResponse.data

    // 获取孔位使用情况
    const positionsResponse = await containerApi.getContainerPositions(selectedContainerId.value)
    initPositions(positionsResponse.data)

    drawPlate()
  } catch (error) {
    showMessage('加载容器数据失败', 'error')
  } finally {
    loading.value = false
  }
}

// 初始化孔位数据
const initPositions = (data: any[]) => {
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
  const newPositions: WellPosition[] = []

  for (let r = 0; r < plateConfig.value.rows; r++) {
    for (let c = 0; c < plateConfig.value.cols; c++) {
      const position = generatePosition(r, c)
      const posData = data.find(d => d.position === position)

      newPositions.push({
        position,
        row: r,
        col: c,
        status: posData ? 'used' : 'empty',
        sampleId: posData?.sampleId,
        sampleName: posData?.sampleName,
        currentNode: posData?.currentNode
      })
    }
  }

  positions.value = newPositions
}

// 绘制板位图
const drawPlate = () => {
  if (!ctx || !canvasRef.value) return

  const canvas = canvasRef.value
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  ctx.save()
  ctx.scale(scale.value, scale.value)

  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

  // 绘制列标签
  ctx.fillStyle = '#909399'
  ctx.font = '14px Arial'
  ctx.textAlign = 'center'
  for (let c = 0; c < plateConfig.value.cols; c++) {
    const x =
      plateConfig.value.offsetX +
      c * (plateConfig.value.wellSize + plateConfig.value.gap) +
      plateConfig.value.wellSize / 2
    ctx.fillText(String(c + 1), x, plateConfig.value.offsetY - 20)
  }

  // 绘制行标签
  ctx.textAlign = 'right'
  ctx.textBaseline = 'middle'
  for (let r = 0; r < plateConfig.value.rows; r++) {
    const y =
      plateConfig.value.offsetY +
      r * (plateConfig.value.wellSize + plateConfig.value.gap) +
      plateConfig.value.wellSize / 2
    ctx.fillText(rows[r], plateConfig.value.offsetX - 20, y)
  }

  // 绘制孔位
  positions.value.forEach(pos => {
    const x =
      plateConfig.value.offsetX + pos.col * (plateConfig.value.wellSize + plateConfig.value.gap)
    const y =
      plateConfig.value.offsetY + pos.row * (plateConfig.value.wellSize + plateConfig.value.gap)

    // 绘制圆形孔位
    ctx.beginPath()
    ctx.arc(
      x + plateConfig.value.wellSize / 2,
      y + plateConfig.value.wellSize / 2,
      plateConfig.value.wellSize / 2 - 2,
      0,
      Math.PI * 2
    )

    // 根据状态填充颜色
    if (selectedPosition.value === pos.position) {
      ctx.fillStyle = '#67C23A'
    } else if (pos.status === 'used') {
      ctx.fillStyle = '#409EFF'
    } else if (pos.status === 'exception') {
      ctx.fillStyle = '#E6A23C'
    } else {
      ctx.fillStyle = '#F5F7FA'
    }
    ctx.fill()

    // 绘制边框
    ctx.strokeStyle = '#DCDFE6'
    ctx.lineWidth = 1
    ctx.stroke()

    // 绘制位置标签
    ctx.fillStyle = pos.status === 'empty' ? '#909399' : '#FFFFFF'
    ctx.font = '12px Arial'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(
      pos.position,
      x + plateConfig.value.wellSize / 2,
      y + plateConfig.value.wellSize / 2
    )
  })

  ctx.restore()
}

// 处理Canvas点击
const handleCanvasClick = (event: MouseEvent) => {
  if (!canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = (event.clientX - rect.left) / scale.value
  const y = (event.clientY - rect.top) / scale.value

  // 查找点击的孔位
  for (const pos of positions.value) {
    const wellX =
      plateConfig.value.offsetX +
      pos.col * (plateConfig.value.wellSize + plateConfig.value.gap) +
      plateConfig.value.wellSize / 2
    const wellY =
      plateConfig.value.offsetY +
      pos.row * (plateConfig.value.wellSize + plateConfig.value.gap) +
      plateConfig.value.wellSize / 2

    const distance = Math.sqrt((x - wellX) ** 2 + (y - wellY) ** 2)

    if (distance <= plateConfig.value.wellSize / 2) {
      handlePositionClick(pos)
      break
    }
  }
}

// 处理孔位点击
const handlePositionClick = (pos: WellPosition) => {
  selectedPosition.value = pos.position
  drawPlate()
  showMessage(`已选择孔位: ${pos.position}`, 'info')
}

// 容器切换
const handleContainerChange = () => {
  loadContainerData()
}

// 刷新
const handleRefresh = () => {
  loadContainerData()
  showMessage('数据已刷新', 'success')
}

// 导出
const handleExport = () => {
  showMessage('导出功能开发中...', 'info')
}

// 放大
const handleZoomIn = () => {
  scale.value = Math.min(scale.value + 0.1, 2)
  drawPlate()
}

// 缩小
const handleZoomOut = () => {
  scale.value = Math.max(scale.value - 0.1, 0.5)
  drawPlate()
}

// 重置
const handleReset = () => {
  scale.value = 1
  drawPlate()
}

// 批量选择
const handleBatchSelect = () => {
  showMessage('批量选择功能开发中...', 'info')
}

// 清除选择
const handleClearSelection = () => {
  selectedPosition.value = null
  const selectedInfo = document.getElementById('selectedInfo')
  if (selectedInfo) {
    selectedInfo.style.display = 'none'
  }
  drawPlate()
  showMessage('已清除选择', 'info')
}

// 查找空位
const handleFindEmpty = () => {
  const emptyPositions = positions.value.filter(p => p.status === 'empty')
  if (emptyPositions.length > 0) {
    handlePositionClick(emptyPositions[0])
    showMessage(`找到 ${emptyPositions.length} 个空位，已定位到第一个`, 'success')
  } else {
    showMessage('没有空闲孔位', 'warning')
  }
}

// 查看样本详情
const viewSampleDetail = () => {
  const pos = selectedPositionInfo.value
  if (pos && pos.sampleId) {
    window.open(`/samples/${pos.sampleId}`, '_blank')
  }
}

// 一键填充所有孔位
const handleBatchFillAll = async () => {
  if (!plateNumber.value.trim()) {
    showMessage('请输入96孔板号', 'error')
    return
  }

  if (!confirm(`确定要为所有96个孔位填充板号 "${plateNumber.value}" 吗?`)) {
    return
  }

  showMessage('正在填充...', 'info')

  // 创建绿色波纹动画效果
  let delay = 0
  positions.value.forEach((pos, index) => {
    setTimeout(() => {
      // 更新孔位状态
      pos.status = 'used'
      pos.sampleId = `AK2026022400${String(index + 1).padStart(2, '0')}`
      pos.sampleName = `Sample-${index + 1}`
      pos.currentNode = '核酸提取'

      // 重绘板位图
      drawPlate()

      // 最后一个孔位填充完成
      if (index === positions.value.length - 1) {
        setTimeout(() => {
          showMessage(`成功填充96个孔位! 板号: ${plateNumber.value}`, 'success')
        }, 100)
      }
    }, delay)

    delay += 30 // 每个孔位延迟30ms，总共约3秒完成
  })
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

// 初始化Canvas
const initCanvas = () => {
  if (!canvasRef.value) return
  ctx = canvasRef.value.getContext('2d')
  drawPlate()
}

// 初始化
onMounted(async () => {
  await loadContainers()
  initCanvas()
})
</script>

<template>
  <div class="container">
    <div class="page-header">
      <div>
        <a href="/" class="btn btn-text" @click.prevent="router.back()">← 返回首页</a>
        <h1 class="page-title">板位图可视化</h1>
      </div>
      <div class="btn-group">
        <select
          class="form-select"
          v-model="selectedContainerId"
          @change="handleContainerChange"
          style="width: 200px;"
        >
          <option value="">选择容器</option>
          <option
            v-for="container in containers"
            :key="container.id"
            :value="container.id"
          >
            {{ container.containerCode }} ({{ container.containerType }})
          </option>
        </select>
        <button class="btn btn-default" @click="handleRefresh">🔄 刷新</button>
        <button class="btn btn-primary" @click="handleExport">📤 导出</button>
      </div>
    </div>

    <div class="plate-container">
      <!-- 板位图画布 -->
      <div class="plate-canvas-wrapper">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <div>
            <h3 style="margin: 0; color: var(--text-primary);">
              {{ currentContainer?.containerType || '96孔板' }} - {{ currentContainer?.containerCode || 'P096-001' }}
            </h3>
            <p style="margin: 4px 0 0 0; color: var(--text-secondary); font-size: 14px;">
              使用率: <strong style="color: var(--primary-color);">{{ usedCount }}/{{ positions.length }} ({{ usageRate }}%)</strong>
            </p>
          </div>
          <div class="btn-group">
            <button class="btn btn-text" @click="handleZoomIn">🔍+</button>
            <button class="btn btn-text" @click="handleZoomOut">🔍-</button>
            <button class="btn btn-text" @click="handleReset">↻</button>
          </div>
        </div>

        <canvas
          ref="canvasRef"
          id="plateCanvas"
          width="800"
          height="600"
          @click="handleCanvasClick"
        ></canvas>

        <div class="legend">
          <div class="legend-item">
            <div class="legend-color" style="background: #F5F7FA;"></div>
            <span>空闲</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background: #409EFF;"></div>
            <span>已使用</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background: #67C23A;"></div>
            <span>当前选中</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background: #E6A23C;"></div>
            <span>异常</span>
          </div>
        </div>
      </div>

      <!-- 信息面板 -->
      <div class="plate-info-panel">
        <h3 style="margin: 0 0 16px 0; color: var(--text-primary);">容器信息</h3>

        <div class="info-row">
          <div class="info-label">容器编号:</div>
          <div class="info-value">{{ currentContainer?.containerCode || 'P096-001' }}</div>
        </div>

        <div class="info-row">
          <div class="info-label">容器类型:</div>
          <div class="info-value">{{ currentContainer?.containerType || '96孔板' }}</div>
        </div>

        <div class="info-row">
          <div class="info-label">创建时间:</div>
          <div class="info-value">{{ currentContainer?.createdAt || '2026-02-20' }}</div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ usedCount }}</div>
            <div class="stat-label">已使用</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ emptyCount }}</div>
            <div class="stat-label">空闲</div>
          </div>
        </div>

        <!-- 数据录入模式 -->
        <div style="margin-top: 24px; padding: 16px; background: #F0F9FF; border-radius: 8px; border: 2px solid var(--primary-color);">
          <h4 style="margin: 0 0 12px 0; color: var(--primary-color);">📝 数据录入模式</h4>
          <div style="margin-bottom: 12px;">
            <label style="display: block; font-size: 12px; color: var(--text-secondary); margin-bottom: 4px;">96孔板号</label>
            <input
              type="text"
              v-model="plateNumber"
              class="form-input"
              placeholder="输入板号，如: P096-001"
              style="width: 100%;"
            />
          </div>
          <button class="btn btn-primary" @click="handleBatchFillAll" style="width: 100%;">
            ⚡ 一键填充所有孔位
          </button>
          <div style="margin-top: 8px; font-size: 12px; color: var(--text-secondary); text-align: center;">
            将自动为96个孔位填充板号和孔位信息
          </div>
        </div>

        <div id="selectedInfo" v-if="selectedPositionInfo" style="margin-top: 24px;">
          <h4 style="margin: 0 0 12px 0; color: var(--text-primary);">选中孔位</h4>
          <div class="selected-position">
            <div style="font-size: 18px; font-weight: 600; color: var(--primary-color); margin-bottom: 12px;">
              孔位: <span>{{ selectedPositionInfo.position }}</span>
            </div>
            <div style="font-size: 14px; color: var(--text-regular); line-height: 1.8;">
              <div>样本编号: <strong>{{ selectedPositionInfo.sampleId || '-' }}</strong></div>
              <div>样本名称: <strong>{{ selectedPositionInfo.sampleName || '-' }}</strong></div>
              <div>当前节点: <strong>{{ selectedPositionInfo.currentNode || '-' }}</strong></div>
              <div>
                状态:
                <span
                  class="status-badge"
                  :class="selectedPositionInfo.status === 'used' ? 'in-progress' : ''"
                >
                  {{ selectedPositionInfo.status === 'used' ? '进行中' : '空闲' }}
                </span>
              </div>
            </div>
            <button
              v-if="selectedPositionInfo.sampleId"
              class="btn btn-primary"
              @click="viewSampleDetail"
              style="width: 100%; margin-top: 12px;"
            >
              查看详情
            </button>
          </div>
        </div>

        <div style="margin-top: 24px;">
          <h4 style="margin: 0 0 12px 0; color: var(--text-primary);">快捷操作</h4>
          <div style="display: flex; flex-direction: column; gap: 8px;">
            <button class="btn btn-default" @click="handleBatchSelect" style="width: 100%;">
              批量选择
            </button>
            <button class="btn btn-default" @click="handleClearSelection" style="width: 100%;">
              清除选择
            </button>
            <button class="btn btn-default" @click="handleFindEmpty" style="width: 100%;">
              查找空位
            </button>
          </div>
        </div>
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

.plate-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.plate-canvas-wrapper {
  flex: 1;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.plate-info-panel {
  width: 320px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

#plateCanvas {
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  display: block;
  margin: 0 auto;
}

.legend {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.info-value {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 14px;
}

.selected-position {
  background: #F0F9FF;
  padding: 16px;
  border-radius: 8px;
  border: 2px solid var(--primary-color);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.stat-card {
  background: var(--bg-color);
  padding: 12px;
  border-radius: 4px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
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

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Check } from '@element-plus/icons-vue'
import { taskApi } from '@/api/process'
import { formatDate } from '@/utils'
import type { Task } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const tasks = ref<Task[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索表单
const searchForm = ref({
  processDefinitionKey: '',
  assignee: ''
})

// 加载任务列表
const loadTasks = async () => {
  try {
    loading.value = true
    const response = await taskApi.getTasks({
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm.value
    })
    tasks.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadTasks()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    processDefinitionKey: '',
    assignee: ''
  }
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  loadTasks()
  ElMessage.success('刷新成功')
}

// 查看详情
const handleView = (row: Task) => {
  router.push(`/tasks/${row.id}`)
}

// 完成任务
const handleComplete = async (row: Task) => {
  try {
    await taskApi.completeTask({
      taskId: row.id
    })
    ElMessage.success('任务完成')
    loadTasks()
  } catch (error) {
    ElMessage.error('完成任务失败')
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadTasks()
}

// 每页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadTasks()
}

// 初始化
onMounted(() => {
  loadTasks()
})
</script>

<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="mb-3">
      <el-form :model="searchForm" inline>
        <el-form-item label="流程定义">
          <el-input
            v-model="searchForm.processDefinitionKey"
            placeholder="请输入流程定义Key"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="处理人">
          <el-input
            v-model="searchForm.assignee"
            placeholder="请输入处理人"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="mb-3">
      <div class="toolbar">
        <div class="toolbar-left">
          <h3 style="margin: 0">任务列表</h3>
        </div>
        <div class="toolbar-right">
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table v-loading="loading" :data="tasks" stripe>
        <el-table-column prop="name" label="任务名称" width="200" />
        <el-table-column prop="processDefinitionId" label="流程定义ID" width="200" />
        <el-table-column prop="taskDefinitionKey" label="任务Key" width="150" />
        <el-table-column prop="assignee" label="处理人" width="120" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.suspended ? 'warning' : 'success'">
              {{ row.suspended ? '已挂起' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button
              type="success"
              link
              :icon="Check"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

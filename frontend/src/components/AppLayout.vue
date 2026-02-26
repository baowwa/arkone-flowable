<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  Menu as IconMenu,
  Setting,
  List,
  DataAnalysis,
  Grid
} from '@element-plus/icons-vue'

const router = useRouter()
const isCollapse = ref(false)

const menuItems = [
  {
    index: '/samples',
    title: '样本管理',
    icon: Document,
    children: [
      { index: '/samples', title: '样本列表' },
      { index: '/samples/create', title: '创建样本' }
    ]
  },
  {
    index: '/batch-entry',
    title: '批量录入',
    icon: Grid
  },
  {
    index: '/plate-map',
    title: '板位图',
    icon: DataAnalysis
  },
  {
    index: '/tasks',
    title: '任务管理',
    icon: List
  },
  {
    index: '/projects',
    title: '项目管理',
    icon: Setting
  }
]

const handleMenuSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="logo-container">
        <h1 v-if="!isCollapse" class="logo-title">ArkOne LIMS</h1>
        <h1 v-else class="logo-title-mini">A</h1>
      </div>

      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        @select="handleMenuSelect"
        class="layout-menu"
      >
        <template v-for="item in menuItems" :key="item.index">
          <el-sub-menu v-if="item.children" :index="item.index">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.index"
              :index="child.index"
            >
              {{ child.title }}
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item v-else :index="item.index">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>

      <div class="collapse-btn" @click="isCollapse = !isCollapse">
        <el-icon><IconMenu /></el-icon>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><Setting /></el-icon>
              <span class="user-name">管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="layout-main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-aside {
  background: #304156;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b3a4a;
}

.logo-title {
  color: white;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.logo-title-mini {
  color: white;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.layout-menu {
  flex: 1;
  border: none;
  background: #304156;
}

.layout-menu :deep(.el-menu-item),
.layout-menu :deep(.el-sub-menu__title) {
  color: #bfcbd9;
}

.layout-menu :deep(.el-menu-item:hover),
.layout-menu :deep(.el-sub-menu__title:hover) {
  background: #263445 !important;
  color: white;
}

.layout-menu :deep(.el-menu-item.is-active) {
  background: #409EFF !important;
  color: white;
}

.collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfcbd9;
  cursor: pointer;
  border-top: 1px solid #263445;
}

.collapse-btn:hover {
  background: #263445;
  color: white;
}

.layout-header {
  background: white;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: var(--bg-color);
}

.user-name {
  font-size: 14px;
  color: var(--text-primary);
}

.layout-main {
  background: var(--bg-color);
  padding: 0;
}
</style>

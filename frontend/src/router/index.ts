import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/samples'
  },
  {
    path: '/samples',
    name: 'SampleList',
    component: () => import('@/views/sample/SampleList.vue'),
    meta: { title: '样本列表' }
  },
  {
    path: '/samples/:id',
    name: 'SampleDetail',
    component: () => import('@/views/sample/SampleDetail.vue'),
    meta: { title: '样本详情' }
  },
  {
    path: '/samples/create',
    name: 'SampleCreate',
    component: () => import('@/views/sample/SampleCreate.vue'),
    meta: { title: '创建样本' }
  },
  {
    path: '/batch-entry',
    name: 'BatchEntry',
    component: () => import('@/views/batch/BatchEntry.vue'),
    meta: { title: '批量数据录入' }
  },
  {
    path: '/plate-map',
    name: 'PlateMap',
    component: () => import('@/views/plate/PlateMap.vue'),
    meta: { title: '板位图可视化' }
  },
  {
    path: '/tasks',
    name: 'TaskList',
    component: () => import('@/views/task/TaskList.vue'),
    meta: { title: '任务列表' }
  },
  {
    path: '/tasks/:id',
    name: 'TaskDetail',
    component: () => import('@/views/task/TaskDetail.vue'),
    meta: { title: '任务详情' }
  },
  {
    path: '/projects',
    name: 'ProjectList',
    component: () => import('@/views/project/ProjectList.vue'),
    meta: { title: '项目列表' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - ArkOne LIMS`
  }
  next()
})

export default router

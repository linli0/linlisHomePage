import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/blog',
        name: 'Blog',
        component: () => import('@/views/blog/BlogList.vue'),
        meta: { title: '博客' }
      },
      {
        path: '/blog/:id',
        name: 'BlogDetail',
        component: () => import('@/views/blog/BlogDetail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: '/files',
        name: 'Files',
        component: () => import('@/views/files/FileManager.vue'),
        meta: { title: '文件管理' }
      },
      {
        path: '/tools',
        name: 'Tools',
        component: () => import('@/views/tools/ToolsPanel.vue'),
        meta: { title: '工具箱' }
      },
      {
        path: '/kimi',
        name: 'Kimi',
        component: () => import('@/views/tools/KimiTool.vue'),
        meta: { title: 'Kimi AI' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - Personal Homepage` : 'Personal Homepage'
})

export default router

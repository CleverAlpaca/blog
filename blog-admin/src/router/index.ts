import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题 */
    title?: string
    /** 面包屑（默认取 title） */
    crumbs?: string[]
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '数据看板' }
        },
        {
          path: 'articles',
          name: 'articles',
          component: () => import('@/views/article/List.vue'),
          meta: { title: '文章管理' }
        },
        {
          path: 'articles/edit',
          name: 'article-edit',
          component: () => import('@/views/article/Edit.vue'),
          meta: { title: '写文章', crumbs: ['文章管理', '写文章'] }
        },
        {
          path: 'categories',
          name: 'categories',
          component: () => import('@/views/Category.vue'),
          meta: { title: '分类管理' }
        },
        {
          path: 'tags',
          name: 'tags',
          component: () => import('@/views/Tag.vue'),
          meta: { title: '标签管理' }
        },
        {
          path: 'comments',
          name: 'comments',
          component: () => import('@/views/Comment.vue'),
          meta: { title: '评论管理' }
        },
        {
          path: 'messages',
          name: 'messages',
          component: () => import('@/views/Message.vue'),
          meta: { title: '留言管理' }
        },
        {
          path: 'friend-links',
          name: 'friend-links',
          component: () => import('@/views/FriendLink.vue'),
          meta: { title: '友链管理' }
        },
        {
          path: 'site-config',
          name: 'site-config',
          component: () => import('@/views/SiteConfig.vue'),
          meta: { title: '站点配置' }
        },
        {
          path: 'review',
          name: 'review',
          component: () => import('@/views/Review.vue'),
          meta: { title: '投稿审核' }
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/UserManage.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/Profile.vue'),
          meta: { title: '个人资料' }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ]
})

// 路由守卫：无 token 一律去 /login；已登录访问 /login 去 /dashboard
router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (!token && to.path !== '/login') {
    return {
      path: '/login',
      query: to.fullPath === '/' ? undefined : { redirect: to.fullPath }
    }
  }
  if (token && to.path === '/login') {
    return { path: '/dashboard' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 博客后台` : '博客后台'
})

export default router

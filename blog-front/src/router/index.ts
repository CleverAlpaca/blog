import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/article/:id',
    name: 'article-detail',
    component: () => import('@/views/ArticleDetail.vue'),
    props: true
  },
  {
    path: '/archive',
    name: 'archive',
    component: () => import('@/views/Archive.vue'),
    meta: { title: '归档' }
  },
  {
    path: '/categories',
    name: 'categories',
    component: () => import('@/views/Categories.vue'),
    meta: { title: '分类' }
  },
  {
    path: '/category/:id',
    name: 'category-detail',
    component: () => import('@/views/CategoryDetail.vue'),
    props: true
  },
  {
    path: '/tags',
    name: 'tags',
    component: () => import('@/views/Tags.vue'),
    meta: { title: '标签' }
  },
  {
    path: '/tag/:id',
    name: 'tag-detail',
    component: () => import('@/views/TagDetail.vue'),
    props: true
  },
  {
    path: '/message',
    name: 'message',
    component: () => import('@/views/Message.vue'),
    meta: { title: '留言板' }
  },
  {
    path: '/friends',
    name: 'friends',
    component: () => import('@/views/Friends.vue'),
    meta: { title: '友链' }
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('@/views/About.vue'),
    meta: { title: '关于' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/write',
    name: 'write',
    component: () => import('@/views/Write.vue'),
    meta: { title: '写文章', requiresAuth: true }
  },
  {
    path: '/my/articles',
    name: 'my-articles',
    component: () => import('@/views/MyArticles.vue'),
    meta: { title: '我的投稿', requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'user-home',
    component: () => import('@/views/UserHome.vue'),
    props: true
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  }
})

// 登录守卫：requiresAuth 的路由需要前台登录态
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('front_token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router

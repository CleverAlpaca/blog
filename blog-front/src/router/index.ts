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

export default router

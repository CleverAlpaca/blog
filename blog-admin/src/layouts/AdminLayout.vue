<template>
  <el-container class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="layout-aside">
      <div class="logo-area">
        <span class="logo-title">博客管理后台</span>
      </div>
      <el-menu
        class="layout-menu"
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="layout-right">
      <!-- 顶栏 -->
      <el-header class="layout-header" height="56px">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item v-for="(crumb, index) in crumbs" :key="index">
            {{ crumb }}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-entry">
            <el-avatar :size="32" :src="userStore.user?.avatar || undefined">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username || '管理员' }}</span>
            <el-icon class="user-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人资料
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>

      <!-- 底部版权 -->
      <el-footer class="layout-footer" height="40px">
        © {{ new Date().getFullYear() }} My Blog Admin · Powered by Vue 3 + Spring Boot 3
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getProfile, logout } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { path: '/dashboard', title: '数据看板', icon: 'Odometer' },
  { path: '/articles', title: '文章管理', icon: 'Document' },
  { path: '/categories', title: '分类管理', icon: 'FolderOpened' },
  { path: '/tags', title: '标签管理', icon: 'PriceTag' },
  { path: '/comments', title: '评论管理', icon: 'ChatDotRound' },
  { path: '/messages', title: '留言管理', icon: 'ChatLineSquare' },
  { path: '/friend-links', title: '友链管理', icon: 'Link' },
  { path: '/profile', title: '个人资料', icon: 'User' }
]

const activeMenu = computed(() => {
  const path = route.path
  // 文章编辑页高亮「文章管理」
  if (path.startsWith('/articles')) return '/articles'
  return path
})

const crumbs = computed<string[]>(() => {
  if (route.meta.crumbs?.length) return route.meta.crumbs
  return route.meta.title ? [route.meta.title] : ['后台管理']
})

// 刷新后 store 中无用户信息时，拉取一次资料
onMounted(async () => {
  if (!userStore.user) {
    try {
      const profile = await getProfile()
      userStore.setUser(profile)
    } catch {
      // 拦截器已处理错误（401 会跳登录页）
    }
  }
})

function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await logout()
  } catch {
    // 即使接口失败也继续清理本地状态
  }
  userStore.clear()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.layout-aside {
  display: flex;
  flex-direction: column;
  background-color: #001529;
}

.logo-area {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-title {
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 2px;
}

.layout-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
}

.layout-menu :deep(.el-menu-item.is-active) {
  background-color: #1677ff;
}

.layout-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08);
}

.layout-right {
  min-width: 0;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.breadcrumb {
  font-size: 14px;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.user-arrow {
  font-size: 12px;
  color: #909399;
}

.layout-main {
  padding: 16px 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

.layout-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
  background-color: #f0f2f5;
  border-top: 1px solid #e4e7ed;
}
</style>

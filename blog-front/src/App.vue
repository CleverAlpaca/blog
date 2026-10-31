<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSiteStore } from '@/stores/site'
import { useTheme } from '@/composables/useTheme'
import AppIcon from '@/components/AppIcon.vue'
import AppToast from '@/components/AppToast.vue'

const { theme, toggleTheme } = useTheme()

const route = useRoute()
const router = useRouter()
const siteStore = useSiteStore()

onMounted(() => {
  siteStore.load()
})

const navItems = [
  { label: '首页', to: '/', match: (p: string) => p === '/' },
  { label: '归档', to: '/archive', match: (p: string) => p.startsWith('/archive') },
  { label: '分类', to: '/categories', match: (p: string) => p.startsWith('/categor') },
  { label: '标签', to: '/tags', match: (p: string) => p.startsWith('/tag') },
  { label: '留言板', to: '/message', match: (p: string) => p.startsWith('/message') },
  { label: '友链', to: '/friends', match: (p: string) => p.startsWith('/friends') },
  { label: '关于', to: '/about', match: (p: string) => p.startsWith('/about') }
]

const keyword = ref('')
const menuOpen = ref(false)

// 路由变化时同步搜索框内容与关闭移动端菜单
watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : ''
  },
  { immediate: true }
)

watch(
  () => route.fullPath,
  async () => {
    if (menuOpen.value) {
      menuOpen.value = false
      await nextTick()
    }
  }
)

const searching = ref(false)

function handleSearch(): void {
  const value = keyword.value.trim()
  searching.value = true
  router
    .push({ path: '/', query: value ? { keyword: value } : {} })
    .catch(() => undefined)
    .finally(() => {
      searching.value = false
    })
}

const activeNav = computed<string>(() => {
  const path = route.path
  const item = navItems.find((i) => i.match(path))
  return item?.to ?? ''
})

const year = new Date().getFullYear()
</script>

<template>
  <header class="site-header">
    <div class="container header-inner">
      <RouterLink to="/" class="logo">
        <span class="logo-mark">A</span>
        <span class="logo-text">{{ siteStore.siteName }}</span>
      </RouterLink>

      <nav class="nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: activeNav === item.to }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <form class="search-box" role="search" @submit.prevent="handleSearch">
        <AppIcon name="search" :size="15" />
        <input v-model="keyword" type="search" placeholder="搜索文章，回车确认" aria-label="搜索文章" />
      </form>

      <button
        class="theme-toggle"
        type="button"
        :aria-label="theme === 'dark' ? '切换为亮色模式' : '切换为暗色模式'"
        :title="theme === 'dark' ? '切换为亮色模式' : '切换为暗色模式'"
        @click="toggleTheme"
      >
        <AppIcon :name="theme === 'dark' ? 'sun' : 'moon'" :size="17" />
      </button>

      <button class="menu-toggle" type="button" :aria-expanded="menuOpen" aria-label="打开菜单" @click="menuOpen = !menuOpen">
        <AppIcon :name="menuOpen ? 'x' : 'menu'" :size="20" />
      </button>
    </div>

    <!-- 移动端抽屉 -->
    <Transition name="drawer">
      <div v-if="menuOpen" class="mobile-menu">
        <form class="search-box mobile-search" role="search" @submit.prevent="handleSearch">
          <AppIcon name="search" :size="15" />
          <input v-model="keyword" type="search" placeholder="搜索文章，回车确认" aria-label="搜索文章" />
        </form>
        <nav class="mobile-nav">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="mobile-link"
            :class="{ active: activeNav === item.to }"
          >
            {{ item.label }}
            <AppIcon name="chevron-right" :size="15" />
          </RouterLink>
        </nav>
      </div>
    </Transition>
  </header>

  <main class="page-main">
    <div class="container">
      <RouterView v-slot="{ Component }">
        <Transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </RouterView>
    </div>
  </main>

  <footer class="site-footer">
    <div class="container footer-inner">
      <p class="footer-line">
        © {{ year }} <RouterLink to="/" class="footer-link">{{ siteStore.siteName }}</RouterLink> · 已运行
        <strong>{{ siteStore.stats?.runDays ?? '--' }}</strong> 天
      </p>
      <p class="footer-line muted">
        <template v-if="siteStore.icp">
          <span class="beian">{{ siteStore.icp }}</span>
          <span class="divider">|</span>
        </template>
        <a class="footer-rss" href="/api/front/rss.xml" target="_blank" title="RSS 订阅">
          <AppIcon name="rss" :size="12" />
          RSS
        </a>
        <span class="divider">|</span>
        <span>Powered by Vue 3 &amp; Spring Boot 3</span>
      </p>
    </div>
  </footer>

  <AppToast />
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: var(--header-bg);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
}

.header-inner {
  height: var(--header-height);
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-shrink: 0;
}

.logo-mark {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: linear-gradient(135deg, #4f6ef7, #7a5cf0);
  color: #fff;
  font-size: 17px;
  font-weight: 800;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 2px;
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.nav::-webkit-scrollbar {
  display: none;
}

.nav-link {
  position: relative;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 14.5px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.nav-link.active {
  color: var(--color-primary);
  font-weight: 600;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -2px;
  transform: translateX(-50%);
  width: 16px;
  height: 3px;
  border-radius: 2px;
  background: var(--color-primary);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 220px;
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-bg);
  color: var(--color-text-tertiary);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.search-box:focus-within {
  border-color: var(--color-primary);
  background: var(--color-card);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}

.search-box input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13.5px;
  color: var(--color-text);
}

.search-box input::placeholder {
  color: var(--color-text-tertiary);
}

.menu-toggle {
  display: none;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: var(--color-text-secondary);
}

.theme-toggle {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.theme-toggle:hover {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.menu-toggle:hover {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

/* 移动端抽屉菜单 */
.mobile-menu {
  display: none;
  border-top: 1px solid var(--color-border);
  background: var(--color-card);
  padding: 12px 16px 16px;
}

.mobile-search {
  width: 100%;
  margin-bottom: 8px;
}

.mobile-nav {
  display: flex;
  flex-direction: column;
}

.mobile-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 6px;
  border-bottom: 1px solid var(--color-border);
  font-size: 15px;
  color: var(--color-text);
}

.mobile-link:last-child {
  border-bottom: none;
}

.mobile-link.active {
  color: var(--color-primary);
  font-weight: 600;
}

.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 768px) {
  .nav,
  .header-inner > .search-box {
    display: none;
  }

  .menu-toggle {
    display: inline-flex;
    margin-left: auto;
  }

  .mobile-menu {
    display: block;
  }

  .header-inner {
    justify-content: space-between;
  }
}

/* 页脚 */
.site-footer {
  border-top: 1px solid var(--color-border);
  background: var(--color-card);
  padding: 22px 0;
  margin-top: auto;
}

.footer-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.footer-link {
  color: var(--color-primary);
  font-weight: 600;
}

.footer-line.muted {
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.divider {
  color: var(--color-border);
}

.footer-rss {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-secondary);
  transition: color 0.2s ease;
}

.footer-rss:hover {
  color: var(--color-primary);
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePageTitle } from '@/composables/usePageTitle'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('GitHub 登录')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const error = ref('')

async function exchange(): Promise<void> {
  const code = typeof route.query.code === 'string' ? route.query.code : ''
  const state = typeof route.query.state === 'string' ? route.query.state : ''
  if (!code || !state) {
    error.value = '回调参数缺失，请从登录页重新发起 GitHub 登录'
    loading.value = false
    return
  }
  try {
    await userStore.githubLogin(code, state)
    // 绑定流程回设置页；普通登录回来源或首页
    const stored = sessionStorage.getItem('github_redirect')
    sessionStorage.removeItem('github_redirect')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : stored || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e instanceof Error && e.message ? e.message : 'GitHub 登录失败，请重试'
    loading.value = false
  }
}

onMounted(exchange)
</script>

<template>
  <div class="auth-page">
    <div class="card auth-card">
      <h1 class="auth-title">GitHub 登录</h1>
      <p v-if="loading" class="auth-desc loading-line">
        <AppIcon name="link" :size="15" />
        正在通过 GitHub 换取登录态…
      </p>
      <template v-else>
        <p class="auth-error">{{ error }}</p>
        <p class="auth-switch">
          <RouterLink class="switch-link" to="/login">返回登录页重试</RouterLink>
        </p>
      </template>
      <p class="auth-back">
        <RouterLink to="/" class="back-link"><AppIcon name="home" :size="13" /> 返回首页</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 32px 28px;
}

.auth-title {
  font-size: 22px;
  font-weight: 700;
}

.auth-desc {
  margin-top: 14px;
  font-size: 13.5px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.loading-line :deep(svg) {
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.auth-error {
  margin-top: 14px;
  color: var(--color-danger);
  font-size: 13.5px;
}

.auth-switch {
  margin-top: 16px;
  font-size: 13.5px;
  color: var(--color-text-secondary);
}

.switch-link {
  color: var(--color-primary);
  font-weight: 600;
}

.auth-back {
  margin-top: 10px;
  font-size: 13px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-tertiary);
}

.back-link:hover {
  color: var(--color-primary);
}
</style>

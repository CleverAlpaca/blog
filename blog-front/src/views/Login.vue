<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePageTitle } from '@/composables/usePageTitle'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('登录')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function handleSubmit(): Promise<void> {
  if (!username.value.trim() || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await userStore.login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } catch (e) {
    error.value = e instanceof Error && e.message ? e.message : '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="card auth-card">
      <h1 class="auth-title">登录</h1>
      <p class="auth-desc">登录后可评论、留言与投稿</p>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label class="field-label" for="username">用户名</label>
          <input id="username" v-model="username" class="form-input" type="text" autocomplete="username" />
        </div>
        <div class="field">
          <label class="field-label" for="password">密码</label>
          <input id="password" v-model="password" class="form-input" type="password" autocomplete="current-password" />
        </div>
        <p v-if="error" class="auth-error">{{ error }}</p>
        <button class="btn btn-primary auth-submit" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="auth-switch">
        还没有账号？
        <RouterLink class="switch-link" :to="{ path: '/register', query: route.query }">立即注册</RouterLink>
      </p>
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
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.auth-form {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.auth-error {
  color: var(--color-danger);
  font-size: 13px;
}

.auth-submit {
  width: 100%;
  padding: 10px 0;
}

.auth-switch {
  margin-top: 18px;
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

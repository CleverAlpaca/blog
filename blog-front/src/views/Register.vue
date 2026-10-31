<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { sendEmailCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { usePageTitle } from '@/composables/usePageTitle'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('注册')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '', confirm: '', nickname: '', email: '', emailCode: '' })
const loading = ref(false)
const error = ref('')
const codeSending = ref(false)
const countdown = ref(0)
let timer: number | undefined

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function startCountdown(): void {
  countdown.value = 60
  timer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && timer) {
      window.clearInterval(timer)
      timer = undefined
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})

async function handleSendCode(): Promise<void> {
  const email = form.value.email.trim()
  if (!EMAIL_RE.test(email)) {
    error.value = '请输入正确的邮箱地址'
    return
  }
  codeSending.value = true
  error.value = ''
  try {
    await sendEmailCode({ scene: 'register', email })
    startCountdown()
  } catch (e) {
    error.value = e instanceof Error && e.message ? e.message : '验证码发送失败，请稍后重试'
  } finally {
    codeSending.value = false
  }
}

async function handleSubmit(): Promise<void> {
  const username = form.value.username.trim()
  const nickname = form.value.nickname.trim()
  const email = form.value.email.trim()
  if (!username || !form.value.password) {
    error.value = '请输入用户名和密码'
    return
  }
  if (form.value.password !== form.value.confirm) {
    error.value = '两次输入的密码不一致'
    return
  }
  if (!EMAIL_RE.test(email)) {
    error.value = '请输入正确的邮箱地址'
    return
  }
  if (!form.value.emailCode.trim()) {
    error.value = '请输入邮箱验证码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await userStore.register({
      username,
      password: form.value.password,
      nickname: nickname || undefined,
      email,
      emailCode: form.value.emailCode.trim()
    })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } catch (e) {
    error.value = e instanceof Error && e.message ? e.message : '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="card auth-card">
      <h1 class="auth-title">注册</h1>
      <p class="auth-desc">注册成为社区作者，登录后可评论、留言与投稿</p>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label class="field-label" for="reg-username">用户名（字母/数字/下划线，4~32 位）</label>
          <input id="reg-username" v-model="form.username" class="form-input" type="text" autocomplete="username" />
        </div>
        <div class="field">
          <label class="field-label" for="reg-password">密码（6~32 位）</label>
          <input id="reg-password" v-model="form.password" class="form-input" type="password" autocomplete="new-password" />
        </div>
        <div class="field">
          <label class="field-label" for="reg-confirm">确认密码</label>
          <input id="reg-confirm" v-model="form.confirm" class="form-input" type="password" autocomplete="new-password" />
        </div>
        <div class="field">
          <label class="field-label" for="reg-nickname">昵称（选填，默认同用户名）</label>
          <input id="reg-nickname" v-model="form.nickname" class="form-input" type="text" maxlength="32" />
        </div>
        <div class="field">
          <label class="field-label" for="reg-email">邮箱（用于登录与找回密码）</label>
          <input id="reg-email" v-model="form.email" class="form-input" type="email" maxlength="128" />
        </div>
        <div class="field">
          <label class="field-label" for="reg-code">邮箱验证码</label>
          <div class="code-row">
            <input id="reg-code" v-model="form.emailCode" class="form-input" type="text" maxlength="6" inputmode="numeric" autocomplete="one-time-code" />
            <button
              type="button"
              class="btn btn-ghost code-btn"
              :disabled="codeSending || countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s 后重发` : codeSending ? '发送中…' : '获取验证码' }}
            </button>
          </div>
        </div>
        <p v-if="error" class="auth-error">{{ error }}</p>
        <button class="btn btn-primary auth-submit" type="submit" :disabled="loading">
          {{ loading ? '注册中…' : '注册并登录' }}
        </button>
      </form>

      <p class="auth-switch">
        已有账号？
        <RouterLink class="switch-link" :to="{ path: '/login', query: route.query }">直接登录</RouterLink>
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

.code-row {
  display: flex;
  gap: 8px;
}

.code-row .form-input {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex-shrink: 0;
  white-space: nowrap;
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

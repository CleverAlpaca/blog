<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { forgotPassword, sendEmailCode } from '@/api/auth'
import { usePageTitle } from '@/composables/usePageTitle'
import { showSuccess } from '@/utils/toast'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('找回密码')

const route = useRoute()
const router = useRouter()

const form = ref({ email: '', code: '', newPassword: '', confirm: '' })
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
    // 防枚举：账号不存在或未绑定邮箱时后端也返回成功，但不会真的发信
    await sendEmailCode({ scene: 'reset', email })
    startCountdown()
  } catch (e) {
    error.value = e instanceof Error && e.message ? e.message : '验证码发送失败，请稍后重试'
  } finally {
    codeSending.value = false
  }
}

async function handleSubmit(): Promise<void> {
  const email = form.value.email.trim()
  if (!EMAIL_RE.test(email)) {
    error.value = '请输入正确的邮箱地址'
    return
  }
  if (!form.value.code.trim()) {
    error.value = '请输入邮箱验证码'
    return
  }
  if (form.value.newPassword.length < 6 || form.value.newPassword.length > 32) {
    error.value = '密码长度需在 6~32 位之间'
    return
  }
  if (form.value.newPassword !== form.value.confirm) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await forgotPassword({
      email,
      code: form.value.code.trim(),
      newPassword: form.value.newPassword
    })
    showSuccess('密码已重置，请使用新密码登录')
    router.push({ path: '/login', query: route.query })
  } catch (e) {
    error.value = e instanceof Error && e.message ? e.message : '重置失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="card auth-card">
      <h1 class="auth-title">找回密码</h1>
      <p class="auth-desc">输入注册邮箱获取验证码，重置后请使用新密码登录</p>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label class="field-label" for="fp-email">注册邮箱</label>
          <input id="fp-email" v-model="form.email" class="form-input" type="email" maxlength="128" autocomplete="email" />
        </div>
        <div class="field">
          <label class="field-label" for="fp-code">邮箱验证码</label>
          <div class="code-row">
            <input id="fp-code" v-model="form.code" class="form-input" type="text" maxlength="6" inputmode="numeric" autocomplete="one-time-code" />
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
        <div class="field">
          <label class="field-label" for="fp-password">新密码（6~32 位）</label>
          <input id="fp-password" v-model="form.newPassword" class="form-input" type="password" autocomplete="new-password" />
        </div>
        <div class="field">
          <label class="field-label" for="fp-confirm">确认新密码</label>
          <input id="fp-confirm" v-model="form.confirm" class="form-input" type="password" autocomplete="new-password" />
        </div>
        <p v-if="error" class="auth-error">{{ error }}</p>
        <button class="btn btn-primary auth-submit" type="submit" :disabled="loading">
          {{ loading ? '重置中…' : '重置密码' }}
        </button>
      </form>

      <p class="auth-switch">
        想起密码了？
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

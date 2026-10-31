<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile, sendEmailCode, updateEmail, updatePassword, updateProfile } from '@/api/auth'
import { uploadMyImage } from '@/api/my'
import { usePageTitle } from '@/composables/usePageTitle'
import { useUserStore } from '@/stores/user'
import { showSuccess } from '@/utils/toast'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('个人设置')

const router = useRouter()
const userStore = useUserStore()

// ---------- 基本资料 ----------
const profileForm = ref({
  nickname: userStore.user?.nickname ?? '',
  avatar: userStore.user?.avatar ?? '',
  intro: userStore.user?.intro ?? ''
})
const profileSaving = ref(false)
const profileError = ref('')
const avatarUploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

function pickAvatar(): void {
  fileInput.value?.click()
}

async function handleAvatarChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) {
    profileError.value = '头像仅支持 jpg/png/gif/webp 格式'
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    profileError.value = '头像图片不能超过 5MB'
    return
  }
  avatarUploading.value = true
  profileError.value = ''
  try {
    const { url } = await uploadMyImage(file)
    profileForm.value.avatar = url
    showSuccess('头像已上传，记得保存资料')
  } catch {
    /* 拦截器已提示 */
  } finally {
    avatarUploading.value = false
  }
}

async function saveProfile(): Promise<void> {
  const nickname = profileForm.value.nickname.trim()
  if (!nickname) {
    profileError.value = '昵称不能为空'
    return
  }
  if (nickname.length > 32) {
    profileError.value = '昵称最长 32 字'
    return
  }
  profileSaving.value = true
  profileError.value = ''
  try {
    await updateProfile({
      nickname,
      avatar: profileForm.value.avatar,
      intro: profileForm.value.intro.trim()
    })
    userStore.updateUser(await getProfile())
    showSuccess('资料已保存')
  } catch {
    /* 拦截器已提示 */
  } finally {
    profileSaving.value = false
  }
}

// ---------- 修改密码 ----------
const pwdForm = ref({ oldPassword: '', newPassword: '', confirm: '' })
const pwdSaving = ref(false)
const pwdError = ref('')

async function savePassword(): Promise<void> {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    pwdError.value = '请输入原密码和新密码'
    return
  }
  if (pwdForm.value.newPassword.length < 6 || pwdForm.value.newPassword.length > 32) {
    pwdError.value = '新密码长度需在 6~32 位之间'
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirm) {
    pwdError.value = '两次输入的新密码不一致'
    return
  }
  pwdSaving.value = true
  pwdError.value = ''
  try {
    await updatePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    showSuccess('密码已修改，请使用新密码重新登录')
    // 后端已踢掉全部会话，本地直接清理登录态并回到登录页
    userStore.clearSession()
    router.push('/login')
  } catch {
    /* 拦截器已提示 */
  } finally {
    pwdSaving.value = false
  }
}

// ---------- 换绑邮箱 ----------
const emailForm = ref({ email: '', code: '' })
const emailSaving = ref(false)
const emailError = ref('')
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
  const email = emailForm.value.email.trim()
  if (!EMAIL_RE.test(email)) {
    emailError.value = '请输入正确的新邮箱地址'
    return
  }
  codeSending.value = true
  emailError.value = ''
  try {
    await sendEmailCode({ scene: 'bind', email })
    startCountdown()
  } catch {
    /* 拦截器已提示 */
  } finally {
    codeSending.value = false
  }
}

async function saveEmail(): Promise<void> {
  const email = emailForm.value.email.trim()
  if (!EMAIL_RE.test(email)) {
    emailError.value = '请输入正确的新邮箱地址'
    return
  }
  if (!emailForm.value.code.trim()) {
    emailError.value = '请输入邮箱验证码'
    return
  }
  emailSaving.value = true
  emailError.value = ''
  try {
    await updateEmail({ email, code: emailForm.value.code.trim() })
    userStore.updateUser(await getProfile())
    emailForm.value.email = ''
    emailForm.value.code = ''
    showSuccess('邮箱已换绑')
  } catch {
    /* 拦截器已提示 */
  } finally {
    emailSaving.value = false
  }
}
</script>

<template>
  <div class="settings-page">
    <h1 class="page-title">个人设置</h1>

    <!-- 基本资料 -->
    <section class="card settings-card">
      <h2 class="card-title">基本资料</h2>
      <div class="profile-layout">
        <div class="avatar-col">
          <button type="button" class="avatar-btn" :disabled="avatarUploading" @click="pickAvatar">
            <img
              v-if="profileForm.avatar"
              :src="profileForm.avatar"
              alt="头像"
              class="avatar-img"
              @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
            />
            <span v-else class="avatar-fallback">
              <AppIcon name="user" :size="30" />
            </span>
            <span class="avatar-mask">{{ avatarUploading ? '上传中…' : '更换头像' }}</span>
          </button>
          <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp" class="visually-hidden" @change="handleAvatarChange" />
          <p class="avatar-hint">jpg/png/gif/webp，≤5MB</p>
        </div>
        <div class="field-col">
          <div class="field">
            <label class="field-label" for="st-nickname">昵称</label>
            <input id="st-nickname" v-model="profileForm.nickname" class="form-input" type="text" maxlength="32" />
          </div>
          <div class="field">
            <label class="field-label" for="st-intro">个人简介</label>
            <textarea id="st-intro" v-model="profileForm.intro" class="form-input intro-input" rows="3" maxlength="200" placeholder="一句话介绍自己（个人主页展示）"></textarea>
          </div>
        </div>
      </div>
      <p v-if="profileError" class="form-error">{{ profileError }}</p>
      <div class="card-actions">
        <button class="btn btn-primary" type="button" :disabled="profileSaving" @click="saveProfile">
          {{ profileSaving ? '保存中…' : '保存资料' }}
        </button>
      </div>
    </section>

    <!-- 换绑邮箱 -->
    <section class="card settings-card">
      <h2 class="card-title">绑定邮箱</h2>
      <p class="current-line">
        当前邮箱：
        <strong>{{ userStore.user?.email || '未绑定' }}</strong>
        <span class="current-hint">（用于登录与找回密码）</span>
      </p>
      <div class="field">
        <label class="field-label" for="st-email">新邮箱</label>
        <input id="st-email" v-model="emailForm.email" class="form-input" type="email" maxlength="128" />
      </div>
      <div class="field">
        <label class="field-label" for="st-email-code">邮箱验证码</label>
        <div class="code-row">
          <input id="st-email-code" v-model="emailForm.code" class="form-input" type="text" maxlength="6" inputmode="numeric" />
          <button type="button" class="btn btn-ghost code-btn" :disabled="codeSending || countdown > 0" @click="handleSendCode">
            {{ countdown > 0 ? `${countdown}s 后重发` : codeSending ? '发送中…' : '获取验证码' }}
          </button>
        </div>
      </div>
      <p v-if="emailError" class="form-error">{{ emailError }}</p>
      <div class="card-actions">
        <button class="btn btn-primary" type="button" :disabled="emailSaving" @click="saveEmail">
          {{ emailSaving ? '换绑中…' : '确认换绑' }}
        </button>
      </div>
    </section>

    <!-- 修改密码 -->
    <section class="card settings-card">
      <h2 class="card-title">修改密码</h2>
      <p class="current-hint block-hint">修改成功后所有登录会话失效，需使用新密码重新登录</p>
      <div class="field">
        <label class="field-label" for="st-old-pwd">原密码</label>
        <input id="st-old-pwd" v-model="pwdForm.oldPassword" class="form-input" type="password" autocomplete="current-password" />
      </div>
      <div class="field">
        <label class="field-label" for="st-new-pwd">新密码（6~32 位）</label>
        <input id="st-new-pwd" v-model="pwdForm.newPassword" class="form-input" type="password" autocomplete="new-password" />
      </div>
      <div class="field">
        <label class="field-label" for="st-confirm-pwd">确认新密码</label>
        <input id="st-confirm-pwd" v-model="pwdForm.confirm" class="form-input" type="password" autocomplete="new-password" />
      </div>
      <p v-if="pwdError" class="form-error">{{ pwdError }}</p>
      <div class="card-actions">
        <button class="btn btn-primary" type="button" :disabled="pwdSaving" @click="savePassword">
          {{ pwdSaving ? '提交中…' : '修改密码' }}
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 0 32px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 18px;
}

.settings-card {
  padding: 22px 24px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16.5px;
  font-weight: 700;
  margin-bottom: 16px;
}

.profile-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.avatar-col {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-btn {
  position: relative;
  width: 84px;
  height: 84px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: none;
  cursor: pointer;
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.avatar-img,
.avatar-fallback {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.avatar-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 4px 0;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-btn:hover .avatar-mask {
  opacity: 1;
}

.avatar-btn:disabled {
  cursor: wait;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}

.avatar-hint {
  font-size: 11.5px;
  color: var(--color-text-tertiary);
}

.field-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
}

.field + .field {
  margin-top: 14px;
}

.field-label {
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.form-input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-small);
  background: var(--color-bg);
  color: var(--color-text);
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.intro-input {
  resize: vertical;
  min-height: 72px;
  line-height: 1.6;
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

.current-line {
  font-size: 13.5px;
  color: var(--color-text-secondary);
  margin-bottom: 14px;
}

.current-line strong {
  color: var(--color-text);
}

.current-hint {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.block-hint {
  margin: -6px 0 14px;
}

.form-error {
  margin-top: 10px;
  color: var(--color-danger);
  font-size: 13px;
}

.card-actions {
  margin-top: 16px;
}

@media (max-width: 560px) {
  .profile-layout {
    flex-direction: column;
    align-items: center;
  }

  .field-col {
    width: 100%;
  }
}
</style>

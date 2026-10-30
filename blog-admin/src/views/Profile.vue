<template>
  <div class="page-container profile-page">
    <!-- 个人资料 -->
    <el-card shadow="never" class="profile-card">
      <template #header>
        <span class="card-title">个人资料</span>
      </template>

      <el-form ref="profileFormRef" :model="profileForm" label-width="80px" class="profile-form">
        <el-form-item label="头像">
          <div class="avatar-row">
            <ImageUpload v-model="profileForm.avatar" width="96px" height="96px" tip="上传头像" />
            <el-avatar v-if="profileForm.avatar" :size="96" :src="profileForm.avatar" class="avatar-preview" />
          </div>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input :model-value="userStore.user?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model.trim="profileForm.nickname" placeholder="站点展示昵称" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="profileForm.email" placeholder="邮箱（选填）" maxlength="128" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input
            v-model="profileForm.intro"
            type="textarea"
            :rows="4"
            maxlength="512"
            show-word-limit
            placeholder="个人简介（选填）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card shadow="never" class="profile-card">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>

      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="profile-form">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model.trim="pwdForm.oldPassword" type="password" show-password placeholder="原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model.trim="pwdForm.newPassword"
            type="password"
            show-password
            placeholder="新密码（6~32 位）"
            maxlength="32"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model.trim="pwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入新密码"
            maxlength="32"
            @keyup.enter="handleChangePassword"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import ImageUpload from '@/components/ImageUpload.vue'
import { getProfile, updateProfile, updatePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import type { UserVO } from '@/api/types'

const router = useRouter()
const userStore = useUserStore()

const profileFormRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const profileSaving = ref(false)
const pwdSaving = ref(false)

const profileForm = reactive({
  nickname: '',
  avatar: '',
  email: '',
  intro: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度需在 6~32 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function fillProfileForm() {
  const u = userStore.user
  profileForm.nickname = u?.nickname ?? ''
  profileForm.avatar = u?.avatar ?? ''
  profileForm.email = u?.email ?? ''
  profileForm.intro = u?.intro ?? ''
}

onMounted(async () => {
  // 刷新后 store 可能为空，拉取一次最新资料
  if (!userStore.user) {
    try {
      userStore.setUser(await getProfile())
    } catch {
      // 拦截器已统一提示
    }
  }
  fillProfileForm()
})

/** 保存资料并同步 store */
async function handleSaveProfile() {
  if (!profileForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  profileSaving.value = true
  try {
    await updateProfile({
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      email: profileForm.email,
      intro: profileForm.intro
    })
    ElMessage.success('资料已更新')
    const updated: UserVO = {
      ...(userStore.user as UserVO),
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      email: profileForm.email,
      intro: profileForm.intro
    }
    userStore.setUser(updated)
  } catch {
    // 拦截器已统一提示
  } finally {
    profileSaving.value = false
  }
}

/** 修改密码：成功后所有会话失效，跳转登录页 */
async function handleChangePassword() {
  if (!pwdFormRef.value) return
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await ElMessageBox.confirm('修改密码后需要重新登录，确定继续吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  pwdSaving.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码已修改，请重新登录')
    userStore.clear()
    router.replace('/login')
  } catch {
    // 拦截器已统一提示
  } finally {
    pwdSaving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-start;
}

.profile-card {
  width: 560px;
  max-width: 100%;
}

.profile-form {
  max-width: 420px;
}

.card-title {
  font-weight: 600;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  flex-shrink: 0;
}
</style>

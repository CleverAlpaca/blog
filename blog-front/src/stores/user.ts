import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, register as registerApi } from '@/api/auth'
import type { FrontLogin, UserVO } from '@/types/api'

const TOKEN_KEY = 'front_token'
const USER_KEY = 'front_user'
const ROLE_KEY = 'front_role'

function readUser(): UserVO | null {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null') as UserVO | null
  } catch {
    return null
  }
}

/**
 * 前台登录态：token / 角色 / 用户信息（localStorage 持久化）。
 * 游客功能（浏览/点赞）不受影响；评论、留言、投稿需要登录。
 */
export const useUserStore = defineStore('frontUser', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const role = ref(localStorage.getItem(ROLE_KEY) || '')
  const user = ref<UserVO | null>(readUser())

  const isLoggedIn = computed(() => Boolean(token.value))
  const isAdmin = computed(() => role.value === 'ADMIN')

  function setSession(data: FrontLogin): void {
    token.value = data.token
    role.value = data.role
    user.value = data.user
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(ROLE_KEY, data.role)
    localStorage.setItem(USER_KEY, JSON.stringify(data.user))
  }

  function clear(): void {
    token.value = ''
    role.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(username: string, password: string): Promise<void> {
    setSession(await loginApi({ username, password }))
  }

  async function register(body: {
    username: string
    password: string
    nickname?: string
    email: string
    emailCode: string
  }): Promise<void> {
    setSession(await registerApi(body))
  }

  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } catch {
      /* 会话已失效也照常清理本地 */
    }
    clear()
  }

  /** 会话失效（401）时由请求拦截器调用 */
  function expire(): void {
    clear()
  }

  return { token, role, user, isLoggedIn, isAdmin, login, register, logout, expire }
})

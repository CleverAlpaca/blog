import { ref } from 'vue'
import { defineStore } from 'pinia'
import { TOKEN_KEY, USER_KEY } from '@/utils/request'
import type { UserVO } from '@/api/types'

function safeParseUser(raw: string | null): UserVO | null {
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserVO
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserVO | null>(safeParseUser(localStorage.getItem(USER_KEY)))

  /** 登录成功：写入 token 与用户信息 */
  function setLogin(nextToken: string, nextUser: UserVO) {
    token.value = nextToken
    user.value = nextUser
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }

  /** 资料修改后同步 store */
  function setUser(nextUser: UserVO) {
    user.value = nextUser
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }

  /** 退出 / 401：清空本地登录态 */
  function clear() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, setLogin, setUser, clear }
})

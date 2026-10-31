import { get, post, put } from '@/utils/request'
import type { FrontLogin, UserVO } from '@/types/api'

export interface RegisterBody {
  username: string
  password: string
  nickname?: string
  email?: string
}

/** 注册（成功即自动登录） */
export function register(body: RegisterBody): Promise<FrontLogin> {
  return post<FrontLogin>('/auth/register', body)
}

/** 登录 */
export function login(body: { username: string; password: string }): Promise<FrontLogin> {
  return post<FrontLogin>('/auth/login', body)
}

/** 登出 */
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

/** 当前登录用户资料 */
export function getProfile(): Promise<UserVO> {
  return get<UserVO>('/auth/profile')
}

/** 修改资料 */
export function updateProfile(body: { nickname?: string; avatar?: string; email?: string; intro?: string }): Promise<void> {
  return put<void>('/auth/profile', body)
}

/** 修改密码（成功后所有会话失效，需重新登录） */
export function updatePassword(body: { oldPassword: string; newPassword: string }): Promise<void> {
  return put<void>('/auth/password', body)
}

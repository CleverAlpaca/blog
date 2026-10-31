import { get, post, put } from '@/utils/request'
import type { FrontLogin, UserVO } from '@/types/api'

export interface RegisterBody {
  username: string
  password: string
  nickname?: string
  /** v2.0 起必填，需先获取邮箱验证码 */
  email: string
  emailCode: string
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

/** 修改资料（邮箱变更不走此处，见 updateEmail） */
export function updateProfile(body: { nickname?: string; avatar?: string; intro?: string }): Promise<void> {
  return put<void>('/auth/profile', body)
}

/** 修改密码（成功后所有会话失效，需重新登录） */
export function updatePassword(body: { oldPassword: string; newPassword: string }): Promise<void> {
  return put<void>('/auth/password', body)
}

/** 发送邮箱验证码（scene：register 注册 / reset 找回密码 / bind 换绑） */
export function sendEmailCode(body: { scene: string; email: string }): Promise<void> {
  return post<void>('/auth/send-email-code', body)
}

/** 找回密码（成功后旧会话全部失效） */
export function forgotPassword(body: { email: string; code: string; newPassword: string }): Promise<void> {
  return post<void>('/auth/forgot-password', body)
}

/** 已登录换绑邮箱（需先向新邮箱发送 scene=bind 验证码） */
export function updateEmail(body: { email: string; code: string }): Promise<void> {
  return put<void>('/auth/email', body)
}

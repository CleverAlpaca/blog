import request from '@/utils/request'
import type { LoginParams, LoginResult, PasswordUpdateParams, ProfileUpdateParams, UserVO } from './types'

/** 登录 */
export function login(data: LoginParams) {
  return request.post<LoginResult>('/admin/login', data)
}

/** 退出登录 */
export function logout() {
  return request.post<void>('/admin/logout')
}

/** 当前管理员信息 */
export function getProfile() {
  return request.get<UserVO>('/admin/profile')
}

/** 修改资料（昵称/头像/邮箱/简介） */
export function updateProfile(data: ProfileUpdateParams) {
  return request.put<void>('/admin/profile', data)
}

/** 修改密码（成功后所有会话失效，需重新登录） */
export function updatePassword(data: PasswordUpdateParams) {
  return request.put<void>('/admin/password', data)
}

import request from '@/utils/request'
import type { PageResult, UserManageVO } from './types'

export type { PageResult, UserManageVO }

/** 用户分页（keyword 匹配用户名/昵称） */
export function getUsers(params: { pageNum: number; pageSize: number; keyword?: string; status?: number }) {
  return request.get<PageResult<UserManageVO>>('/admin/users/page', { params })
}

/** 封禁 / 解封（status: 0 封禁 / 1 解封） */
export function updateUserStatus(id: number, status: number) {
  return request.put<void>(`/admin/users/${id}/status?status=${status}`)
}

/** 重置密码（返回一次性新密码） */
export function resetUserPassword(id: number) {
  return request.put<string>(`/admin/users/${id}/reset-password`)
}

import request from '@/utils/request'
import type { FriendLinkItem, FriendLinkSaveParams } from './types'

/** 友链列表（后台全部） */
export function listFriendLinks() {
  return request.get<FriendLinkItem[]>('/admin/friend-link/list')
}

/** 新增友链 */
export function createFriendLink(data: FriendLinkSaveParams) {
  return request.post<void>('/admin/friend-link', data)
}

/** 修改友链 */
export function updateFriendLink(id: number, data: FriendLinkSaveParams) {
  return request.put<void>(`/admin/friend-link/${id}`, data)
}

/** 删除友链 */
export function deleteFriendLink(id: number) {
  return request.delete<void>(`/admin/friend-link/${id}`)
}

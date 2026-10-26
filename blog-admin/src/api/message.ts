import request from '@/utils/request'
import type { AdminComment, MessagePageParams, PageResult } from './types'

/** 留言分页（nickname 可选） */
export function pageMessages(params: MessagePageParams) {
  return request.get<PageResult<AdminComment>>('/admin/message/page', { params })
}

/** 显示 / 隐藏留言（status: 1 或 0） */
export function updateMessageStatus(id: number, status: number) {
  return request.put<void>(`/admin/message/${id}/status`, null, { params: { status } })
}

/** 删除留言 */
export function deleteMessage(id: number) {
  return request.delete<void>(`/admin/message/${id}`)
}

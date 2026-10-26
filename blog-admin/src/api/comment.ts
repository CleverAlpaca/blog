import request from '@/utils/request'
import type { AdminComment, CommentPageParams, PageResult } from './types'

/** 评论分页（articleTitle/nickname 可选） */
export function pageComments(params: CommentPageParams) {
  return request.get<PageResult<AdminComment>>('/admin/comment/page', { params })
}

/** 显示 / 隐藏评论（status: 1 或 0） */
export function updateCommentStatus(id: number, status: number) {
  return request.put<void>(`/admin/comment/${id}/status`, null, { params: { status } })
}

/** 删除评论 */
export function deleteComment(id: number) {
  return request.delete<void>(`/admin/comment/${id}`)
}

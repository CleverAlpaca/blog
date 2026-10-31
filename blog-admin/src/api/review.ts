import request from '@/utils/request'
import type { AdminReviewVO, PageResult } from './types'

export type { AdminReviewVO, PageResult }

/** 投稿审核分页（status: 3 待审核 / 4 已退回，可空=全部） */
export function getReviewPage(params: { pageNum: number; pageSize: number; status?: number; keyword?: string }) {
  return request.get<PageResult<AdminReviewVO>>('/admin/review/page', { params })
}

/** 审核通过：待审核 → 已发布 */
export function approveReview(id: number) {
  return request.put<void>(`/admin/review/${id}/approve`)
}

/** 退回：待审核 → 已退回（原因必填） */
export function rejectReview(id: number, reason: string) {
  return request.put<void>(`/admin/review/${id}/reject`, { reason })
}

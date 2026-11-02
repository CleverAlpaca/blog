import { get, put } from '@/utils/request'
import type { NotificationItem, PageResult } from '@/types/api'

/** 我的通知分页 */
export function getNotifications(params: { pageNum: number; pageSize: number }): Promise<PageResult<NotificationItem>> {
  return get<PageResult<NotificationItem>>('/notifications/page', params)
}

/** 未读数（页头徽标） */
export function getUnreadCount(): Promise<number> {
  return get<number>('/notifications/unread-count')
}

/** 单条标记已读 */
export function markNotificationRead(id: number): Promise<void> {
  return put<void>(`/notifications/${id}/read`)
}

/** 全部已读 */
export function markAllNotificationsRead(): Promise<void> {
  return put<void>('/notifications/read-all')
}

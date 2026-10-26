import { get } from '@/utils/request'
import type { FriendLink } from '@/types/api'

/** 友链列表（仅显示中） */
export function getFriendLinkList(): Promise<FriendLink[]> {
  return get<FriendLink[]>('/friend-link/list')
}

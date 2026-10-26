import { get, post } from '@/utils/request'
import type { CommentNode, CommentPost } from '@/types/api'

/** 留言板两级留言树（仅显示中） */
export function getMessageTree(): Promise<CommentNode[]> {
  return get<CommentNode[]>('/message/list')
}

/** 发表留言（parentId 传顶级留言 id 即为楼内回复） */
export function postMessage(body: CommentPost): Promise<void> {
  return post<void>('/message', body)
}

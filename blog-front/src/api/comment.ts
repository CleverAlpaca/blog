import { get, post } from '@/utils/request'
import type { CommentNode, CommentPost } from '@/types/api'

/** 某文章的两级评论树（仅显示中） */
export function getCommentTree(articleId: number | string): Promise<CommentNode[]> {
  return get<CommentNode[]>(`/article/${articleId}/comment/list`)
}

/** 发表评论（parentId 传顶级评论 id 即为楼内回复） */
export function postComment(articleId: number | string, body: CommentPost): Promise<void> {
  return post<void>(`/article/${articleId}/comment`, body)
}

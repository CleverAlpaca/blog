import { get } from '@/utils/request'
import type { ArticleCard, PageResult, UserCommentItem, UserHome } from '@/types/api'

/** 用户公开资料（个人主页头卡） */
export function getUserHome(id: number): Promise<UserHome> {
  return get<UserHome>(`/user/${id}`)
}

/** 该用户已发布的文章分页 */
export function getUserArticles(id: number, params: { pageNum: number; pageSize: number }): Promise<PageResult<ArticleCard>> {
  return get<PageResult<ArticleCard>>(`/user/${id}/articles`, params)
}

/** 该用户的公开评论分页（仅显示中且文章已发布） */
export function getUserComments(id: number, params: { pageNum: number; pageSize: number }): Promise<PageResult<UserCommentItem>> {
  return get<PageResult<UserCommentItem>>(`/user/${id}/comments`, params)
}

import { get, post } from '@/utils/request'
import type { ArticleCard, ArticleDetail, ArchiveYear, ArticleQuery, CommentNode, CommentPost, LikeResult, PageResult } from '@/types/api'

/** 文章分页列表（置顶优先、时间倒序，仅已发布） */
export function getArticleList(query: ArticleQuery): Promise<PageResult<ArticleCard>> {
  return get<PageResult<ArticleCard>>('/article/list', { ...query })
}

/** 热门文章（浏览量 Top 5） */
export function getHotArticles(): Promise<ArticleCard[]> {
  return get<ArticleCard[]>('/article/hot')
}

/** 归档：按年份分组 */
export function getArchives(): Promise<ArchiveYear[]> {
  return get<ArchiveYear[]>('/article/archives')
}

/** 文章详情（调用即浏览量 +1） */
export function getArticleDetail(id: number | string): Promise<ArticleDetail> {
  return get<ArticleDetail>(`/article/${id}`)
}

/** 点赞开关：未赞则赞、已赞则取消 */
export function toggleLike(id: number | string): Promise<LikeResult> {
  return post<LikeResult>(`/article/${id}/like`)
}

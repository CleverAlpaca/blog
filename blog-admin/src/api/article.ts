import request from '@/utils/request'
import type { ArticleCard, ArticleDetail, ArticlePageParams, ArticleSaveParams, PageResult } from './types'

/** 文章分页（keyword/status/categoryId 可选） */
export function pageArticles(params: ArticlePageParams) {
  return request.get<PageResult<ArticleCard>>('/admin/article/page', { params })
}

/** 文章详情（编辑回显，含 tagIds 对应的 tags 列表） */
export function getArticle(id: number) {
  return request.get<ArticleDetail>(`/admin/article/${id}`)
}

/** 新增文章 */
export function createArticle(data: ArticleSaveParams) {
  return request.post<number>('/admin/article', data)
}

/** 更新文章 */
export function updateArticle(id: number, data: ArticleSaveParams) {
  return request.put<void>(`/admin/article/${id}`, data)
}

/** 删除文章（逻辑删除） */
export function deleteArticle(id: number) {
  return request.delete<void>(`/admin/article/${id}`)
}

/** 置顶 / 取消置顶（isTop: 1 或 0） */
export function setArticleTop(id: number, isTop: number) {
  return request.put<void>(`/admin/article/${id}/top`, null, { params: { isTop } })
}

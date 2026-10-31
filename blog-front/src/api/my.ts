import { del, get, post, put } from '@/utils/request'
import type { ArticleCard, ArticleDetail, MyArticleSave, MyArticleStats, PageResult } from '@/types/api'

/** 投稿 / 存草稿（返回文章ID） */
export function createMyArticle(body: MyArticleSave): Promise<number> {
  return post<number>('/my/articles', body)
}

/** 我的投稿分页（status: 0 草稿 / 3 待审核 / 4 已退回 / 1 已发布，可空） */
export function getMyArticles(params: { pageNum: number; pageSize: number; status?: number }): Promise<PageResult<ArticleCard>> {
  return get<PageResult<ArticleCard>>('/my/articles', params)
}

/** 我的投稿各状态数量 */
export function getMyStats(): Promise<MyArticleStats> {
  return get<MyArticleStats>('/my/articles/stats')
}

/** 编辑回显（含退回原因） */
export function getMyArticle(id: number): Promise<ArticleDetail> {
  return get<ArticleDetail>(`/my/articles/${id}`)
}

/** 更新（action 决定存草稿还是重新提交审核） */
export function updateMyArticle(id: number, body: MyArticleSave): Promise<void> {
  return put<void>(`/my/articles/${id}`, body)
}

/** 删除（已发布文章不可自删） */
export function deleteMyArticle(id: number): Promise<void> {
  return del<void>(`/my/articles/${id}`)
}

/** 投稿插图上传（返回图片 URL） */
export function uploadMyImage(file: File): Promise<{ url: string }> {
  const formData = new FormData()
  formData.append('file', file)
  return post<{ url: string }>('/my/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

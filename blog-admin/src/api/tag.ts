import request from '@/utils/request'
import type { TagItem } from './types'

/** 标签列表（含 count） */
export function listTags() {
  return request.get<TagItem[]>('/admin/tag/list')
}

/** 新增标签 */
export function createTag(data: { name: string }) {
  return request.post<void>('/admin/tag', data)
}

/** 修改标签 */
export function updateTag(id: number, data: { name: string }) {
  return request.put<void>(`/admin/tag/${id}`, data)
}

/** 删除标签 */
export function deleteTag(id: number) {
  return request.delete<void>(`/admin/tag/${id}`)
}

import request from '@/utils/request'
import type { CategoryItem, CategorySaveParams } from './types'

/** 分类列表（含 articleCount） */
export function listCategories() {
  return request.get<CategoryItem[]>('/admin/category/list')
}

/** 新增分类 */
export function createCategory(data: CategorySaveParams) {
  return request.post<void>('/admin/category', data)
}

/** 修改分类 */
export function updateCategory(id: number, data: CategorySaveParams) {
  return request.put<void>(`/admin/category/${id}`, data)
}

/** 删除分类 */
export function deleteCategory(id: number) {
  return request.delete<void>(`/admin/category/${id}`)
}

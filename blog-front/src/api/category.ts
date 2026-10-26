import { get } from '@/utils/request'
import type { Category } from '@/types/api'

/** 分类列表（含各分类已发布文章数） */
export function getCategoryList(): Promise<Category[]> {
  return get<Category[]>('/category/list')
}

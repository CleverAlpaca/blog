import { get } from '@/utils/request'
import type { Tag } from '@/types/api'

/** 标签列表（含各标签已发布文章数） */
export function getTagList(): Promise<Tag[]> {
  return get<Tag[]>('/tag/list')
}

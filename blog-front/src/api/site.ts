import { get } from '@/utils/request'
import type { About, SiteStats } from '@/types/api'

/** 关于页数据：博主信息 + 「关于我」Markdown 正文（可能为 null） */
export function getAbout(): Promise<About> {
  return get<About>('/about')
}

/** 站点统计：文章/分类/标签/评论数与运行天数 */
export function getSiteStats(): Promise<SiteStats> {
  return get<SiteStats>('/stats')
}

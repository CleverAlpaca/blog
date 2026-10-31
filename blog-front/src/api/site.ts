import { get } from '@/utils/request'
import type { About, SiteStats } from '@/types/api'

/** 站点公开配置（键值对：site_name / site_description / announcement / icp） */
export type SiteConfigMap = Record<string, string>

/** 关于页数据：博主信息 + 「关于我」Markdown 正文（可能为 null） */
export function getAbout(): Promise<About> {
  return get<About>('/about')
}

/** 站点统计：文章/分类/标签/评论数与运行天数 */
export function getSiteStats(): Promise<SiteStats> {
  return get<SiteStats>('/stats')
}

/** 站点公开配置（Logo/页脚/公告/备案号的数据源） */
export function getSiteConfig(): Promise<SiteConfigMap> {
  return get<SiteConfigMap>('/site-config')
}

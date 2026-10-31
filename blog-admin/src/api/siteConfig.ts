import request from '@/utils/request'

export type SiteConfigMap = Record<string, string>

/** 站点配置（全部键值对） */
export function getSiteConfig() {
  return request.get<SiteConfigMap>('/admin/site-config')
}

/** 批量保存站点配置 */
export function updateSiteConfig(data: SiteConfigMap) {
  return request.put<void>('/admin/site-config', data)
}

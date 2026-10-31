import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getAbout, getSiteConfig, getSiteStats } from '@/api/site'
import type { About, SiteStats } from '@/types/api'

/**
 * 站点全局信息：关于页数据、统计、站点配置（站点名/公告/备案号）。
 * App 挂载时加载一次，供顶栏、页脚、首页侧栏、关于页共用；失败静默，各处自行兜底。
 */
export const useSiteStore = defineStore('site', () => {
  const about = ref<About | null>(null)
  const stats = ref<SiteStats | null>(null)
  const config = ref<Record<string, string>>({})
  const loaded = ref(false)

  // 站点名称/公告/备案号的响应式便捷读取（未配置时回退默认值）
  const siteName = computed(() => config.value.site_name || "Alpaca's Blog")
  const announcement = computed(() => config.value.announcement || '')
  const icp = computed(() => config.value.icp || '')

  async function load(): Promise<void> {
    const [aboutResult, statsResult, configResult] = await Promise.allSettled([
      getAbout(),
      getSiteStats(),
      getSiteConfig()
    ])
    if (aboutResult.status === 'fulfilled') about.value = aboutResult.value
    if (statsResult.status === 'fulfilled') stats.value = statsResult.value
    if (configResult.status === 'fulfilled') config.value = configResult.value
    loaded.value = true
  }

  return { about, stats, config, loaded, load, siteName, announcement, icp }
})

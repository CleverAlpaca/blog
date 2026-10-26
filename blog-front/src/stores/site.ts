import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAbout, getSiteStats } from '@/api/site'
import type { About, SiteStats } from '@/types/api'

/**
 * 站点全局信息：关于页数据（博主昵称/头像/简介）与统计（运行天数、文章数等）。
 * App 挂载时加载一次，供顶栏、页脚、首页侧栏、关于页共用；失败静默，各处自行兜底。
 */
export const useSiteStore = defineStore('site', () => {
  const about = ref<About | null>(null)
  const stats = ref<SiteStats | null>(null)
  const loaded = ref(false)

  async function load(): Promise<void> {
    const [aboutResult, statsResult] = await Promise.allSettled([getAbout(), getSiteStats()])
    if (aboutResult.status === 'fulfilled') about.value = aboutResult.value
    if (statsResult.status === 'fulfilled') stats.value = statsResult.value
    loaded.value = true
  }

  return { about, stats, loaded, load }
})

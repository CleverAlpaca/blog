import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { useSiteStore } from '@/stores/site'
import { upsertMeta } from './useMeta'

const SITE_NAME = "Alpaca's Blog"

/** 设置页面标题（离开页面时恢复站点标题） */
export function usePageTitle(title?: string | (() => string | undefined)): void {
  const route = useRoute()

  const apply = (): void => {
    // 站点名优先取站点配置表（后台可视化维护），未加载/未配置时回退默认值
    const siteName = useSiteStore().config?.site_name || SITE_NAME
    const value = typeof title === 'function' ? title() : title
    document.title = value ? `${value} - ${siteName}` : siteName
    // og:title 供分享卡片使用
    upsertMeta('property', 'og:title', document.title)
  }

  // getter 形式（如 () => detail.value?.title）时监听其响应式依赖：
  // 异步数据（文章标题等）到达后自动重新求值。
  // 放入 queueMicrotask：watch 首次求值 getter 发生在 setup 期间，
  // 而 getter 可能引用调用点之后才声明的 ref（TDZ），推迟注册可同时消除两者问题
  if (typeof title === 'function') {
    queueMicrotask(() => watch(title, apply))
  }
  queueMicrotask(apply)
  // 命中同组件的路由参数变化（如 /article/1 → /article/2）时同步标题
  watch(() => route.params.id, apply)
}

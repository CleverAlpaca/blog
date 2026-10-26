import { watch } from 'vue'
import { useRoute } from 'vue-router'

const SITE_TITLE = "Alpaca's Blog"

/** 设置页面标题（离开页面时恢复站点标题） */
export function usePageTitle(title?: string | (() => string | undefined)): void {
  const route = useRoute()

  const apply = (): void => {
    const value = typeof title === 'function' ? title() : title
    document.title = value ? `${value} - ${SITE_TITLE}` : SITE_TITLE
  }

  // getter 可能引用调用点之后才声明的 ref（如文章标题），推迟到 setup 执行完毕后再求值
  queueMicrotask(apply)
  // 命中同组件的路由参数变化（如 /article/1 → /article/2）时同步标题
  watch(() => route.params.id, apply)
}

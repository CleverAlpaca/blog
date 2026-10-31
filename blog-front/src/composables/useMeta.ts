import { watch } from 'vue'

/** 向 <head> 插入或更新一个 meta 标签 */
export function upsertMeta(attr: 'name' | 'property', key: string, content: string): void {
  let el = document.head.querySelector<HTMLMetaElement>(`meta[${attr}="${key}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

export interface PageMeta {
  /** 页面描述：meta description 与 og:description 共用 */
  description?: string | (() => string | undefined)
}

/** 设置页面 SEO meta（description / og:description） */
export function useMeta(meta: PageMeta = {}): void {
  const apply = (): void => {
    const d = typeof meta.description === 'function' ? meta.description() : meta.description
    if (d) {
      upsertMeta('name', 'description', d)
      upsertMeta('property', 'og:description', d)
    }
  }
  // getter 形式（如 () => detail.value?.summary）时监听其响应式依赖：异步数据到达后自动更新。
  // 放入 queueMicrotask：watch 首次求值 getter 发生在 setup 期间，
  // 而 getter 可能引用调用点之后才声明的 ref（TDZ），推迟注册可同时消除两者问题
  if (typeof meta.description === 'function') {
    queueMicrotask(() => watch(meta.description as () => string | undefined, apply))
  }
  // getter 可能引用调用点之后才声明的 ref（与 usePageTitle 同理），推迟到 setup 执行完毕后再求值
  queueMicrotask(apply)
}

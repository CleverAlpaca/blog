import type { TocItem } from '@/types/api'

/**
 * 与 MdPreview 渲染时保持一致的标题 id 生成规则：
 * DOM 中第 N 个（1-based、全层级）heading 的 id 为 h-N。
 * TOC 列表由 onGetCatalog 事件取得后按同规则回填 id，保证锚点可跳转。
 */
export function mdHeadingId(_text: string, _level: number, index: number): string {
  return `h-${index}`
}

interface CatalogHead {
  text: string
  level: number
}

/**
 * 将 MdPreview 的 onGetCatalog 列表转为 TOC 数据：
 * 先按全局序号生成锚点 id，再过滤出 h1~h3。
 */
export function buildToc(list: CatalogHead[]): TocItem[] {
  if (!Array.isArray(list)) return []
  return list
    .map((item, i) => ({ ...item, id: mdHeadingId(item.text, item.level, i + 1) }))
    .filter((item) => item.level >= 1 && item.level <= 3)
    .map((item) => ({ id: item.id, text: item.text, level: item.level as 1 | 2 | 3 }))
}

/**
 * 轻量 HTML 过滤：移除 script/style/iframe 等标签与 on* 事件属性、
 * javascript: 链接，防文章 Markdown 中混入的可执行内容。
 */
export function sanitizeHtml(html: string): string {
  if (typeof window === 'undefined' || typeof DOMParser === 'undefined') return html
  try {
    const doc = new DOMParser().parseFromString(html, 'text/html')
    doc.querySelectorAll('script, iframe, object, embed, form, link, meta').forEach((el) => el.remove())
    doc.querySelectorAll('*').forEach((el) => {
      for (const attr of Array.from(el.attributes)) {
        const name = attr.name.toLowerCase()
        const value = attr.value.trim().toLowerCase()
        if (name.startsWith('on')) {
          el.removeAttribute(attr.name)
        } else if ((name === 'href' || name === 'src' || name === 'xlink:href') && value.startsWith('javascript:')) {
          el.removeAttribute(attr.name)
        }
      }
    })
    return doc.body.innerHTML
  } catch {
    return html
  }
}

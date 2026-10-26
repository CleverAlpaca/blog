/**
 * 时间格式化：后端返回 ISO 本地时间字符串（如 2026-10-26T09:30:00），
 * 直接做字符串切片，避免 new Date() 带来的时区偏移。
 */

/** 2026-10-26T09:30:00 -> 2026-10-26 */
export function formatDate(iso?: string | null): string {
  if (!iso) return ''
  return iso.slice(0, 10)
}

/** 2026-10-26T09:30:00 -> 2026-10-26 09:30 */
export function formatDateTime(iso?: string | null): string {
  if (!iso) return ''
  return iso.slice(0, 16).replace('T', ' ')
}

/** 2026-10-26T09:30:00 -> 10-26 */
export function formatShortDate(iso?: string | null): string {
  if (!iso) return ''
  return iso.slice(5, 10)
}

/** 数字展示：超过 1 万显示 x.xw */
export function formatCount(n?: number | null): string {
  const value = n ?? 0
  if (value >= 10000) {
    const w = value / 10000
    return `${w >= 100 ? Math.round(w) : Math.round(w * 10) / 10}w`
  }
  return String(value)
}

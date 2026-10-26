/** 格式化后端 ISO 时间字符串：2026-10-26T09:30:00 -> 2026-10-26 09:30:00 */
export function formatDateTime(value?: string | null): string {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

/** 文章状态：0 草稿 / 1 已发布 / 2 已下架 */
export const ARTICLE_STATUS_MAP: Record<number, { label: string; type: 'info' | 'success' | 'warning' }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已下架', type: 'warning' }
}

export const ARTICLE_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下架', value: 2 }
]

export function statusLabel(status?: number): string {
  return ARTICLE_STATUS_MAP[status ?? -1]?.label ?? '未知'
}

export function statusTagType(status?: number): 'info' | 'success' | 'warning' {
  return ARTICLE_STATUS_MAP[status ?? -1]?.type ?? 'info'
}

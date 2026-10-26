const VISITOR_KEY = 'visitor_id'

/** 生成 UUID v4（优先使用 Web Crypto） */
function generateUUID(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  const bytes = new Uint8Array(16)
  if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
    crypto.getRandomValues(bytes)
  } else {
    for (let i = 0; i < 16; i++) bytes[i] = Math.floor(Math.random() * 256)
  }
  // 设置 version 4 与 variant 位
  bytes[6] = (bytes[6] & 0x0f) | 0x40
  bytes[8] = (bytes[8] & 0x3f) | 0x80
  const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('')
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
}

/**
 * 获取访客标识：首次访问生成 UUID 并存入 localStorage，之后复用。
 * 后端依据该标识（X-Visitor-Id 请求头）实现点赞防重。
 */
export function getVisitorId(): string {
  try {
    let id = localStorage.getItem(VISITOR_KEY)
    if (!id) {
      id = generateUUID()
      localStorage.setItem(VISITOR_KEY, id)
    }
    return id
  } catch {
    // localStorage 不可用（隐私模式等）时退化为会话内临时 ID
    const g = globalThis as { __visitorId?: string }
    if (!g.__visitorId) g.__visitorId = generateUUID()
    return g.__visitorId
  }
}

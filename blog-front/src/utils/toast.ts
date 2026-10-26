import { reactive } from 'vue'

export type ToastType = 'info' | 'success' | 'error'

export interface ToastItem {
  id: number
  message: string
  type: ToastType
}

/** 全局 toast 状态（由 components/AppToast.vue 渲染） */
export const toastState = reactive<{ list: ToastItem[] }>({ list: [] })

let seed = 0

/** 弹出轻量提示（无 UI 库依赖的自制实现） */
export function showToast(message: string, type: ToastType = 'error', duration = 3000): void {
  if (!message) return
  const id = ++seed
  toastState.list.push({ id, message, type })
  window.setTimeout(() => {
    const index = toastState.list.findIndex((t) => t.id === id)
    if (index > -1) toastState.list.splice(index, 1)
  }, duration)
}

/** 成功提示 */
export function showSuccess(message: string): void {
  showToast(message, 'success')
}

/** 错误提示 */
export function showError(message: string): void {
  showToast(message, 'error')
}

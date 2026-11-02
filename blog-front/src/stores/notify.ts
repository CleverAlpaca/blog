import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getUnreadCount } from '@/api/notify'

/**
 * 站内通知未读数：页头铃铛徽标。
 * App 挂载与路由切换时刷新（登录态才拉取），通知页操作后可手动同步。
 */
export const useNotifyStore = defineStore('notify', () => {
  const unread = ref(0)

  async function refresh(): Promise<void> {
    try {
      unread.value = await getUnreadCount()
    } catch {
      /* 未登录或网络异常时静默归零 */
      unread.value = 0
    }
  }

  function clear(): void {
    unread.value = 0
  }

  return { unread, refresh, clear }
})

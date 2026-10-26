import { ref, shallowRef, onMounted, type Ref, type ShallowRef } from 'vue'

export interface UseRequestReturn<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<string>
  run: () => Promise<void>
}

/**
 * 通用异步数据加载：统一管理 loading / error / data，
 * 组件内 `onMounted(run)` 由本 composable 自动完成。
 */
export function useRequest<T>(fetcher: () => Promise<T>, immediate = true): UseRequestReturn<T> {
  const data = ref<T | null>(null) as Ref<T | null>
  const loading = ref(false)
  const error = ref('')

  async function run(): Promise<void> {
    loading.value = true
    error.value = ''
    try {
      data.value = await fetcher()
    } catch (e) {
      error.value = e instanceof Error && e.message ? e.message : '加载失败，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    onMounted(run)
  }

  return { data, loading, error, run }
}

/** 浅响应版本：适合大对象（如文章详情） */
export function useShallowRequest<T>(fetcher: () => Promise<T>): { data: ShallowRef<T | null>; loading: Ref<boolean>; error: Ref<string>; run: () => Promise<void> } {
  const data = shallowRef<T | null>(null)
  const loading = ref(false)
  const error = ref('')

  async function run(): Promise<void> {
    loading.value = true
    error.value = ''
    try {
      data.value = await fetcher()
    } catch (e) {
      error.value = e instanceof Error && e.message ? e.message : '加载失败，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  onMounted(run)
  return { data, loading, error, run }
}

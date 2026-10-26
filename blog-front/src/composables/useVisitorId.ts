import { getVisitorId } from '@/utils/visitor'

/** 访客标识 composable：返回（并惰性创建）当前访客的 UUID */
export function useVisitorId(): string {
  return getVisitorId()
}

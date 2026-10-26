import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { getVisitorId } from './visitor'
import { showToast } from './toast'

/** 后端统一响应体 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/** 创建前台 API 实例：统一携带 X-Visitor-Id、统一解包 Result */
const request = axios.create({
  baseURL: '/api/front',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  config.headers['X-Visitor-Id'] = getVisitorId()
  return config
})

function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiResult<unknown>>
    const body = axiosError.response?.data
    if (body && typeof body === 'object' && typeof body.message === 'string' && body.message) {
      return body.message
    }
    if (error.code === 'ECONNABORTED') return '请求超时，请稍后重试'
    if (axiosError.response) return `请求失败（${axiosError.response.status}）`
    return '网络异常，请检查网络后重试'
  }
  return error instanceof Error && error.message ? error.message : '未知错误'
}

request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown> | undefined
    // 统一响应体解包；非 200 时 toast 提示并 reject
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data as never
      }
      const message = body.message || '请求失败'
      showToast(message)
      return Promise.reject(new Error(message))
    }
    // 非标准响应体（理论上不出现），原样返回
    return body as never
  },
  (error: unknown) => {
    const message = extractErrorMessage(error)
    showToast(message)
    return Promise.reject(error instanceof Error ? error : new Error(message))
  }
)

/** GET 请求（响应拦截器已解包 data） */
export async function get<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig): Promise<T> {
  return request.get(url, { params, ...config }) as Promise<T>
}

/** POST 请求（响应拦截器已解包 data） */
export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return request.post(url, data, config) as Promise<T>
}

export default request

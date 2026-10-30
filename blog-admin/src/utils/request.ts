import axios, { type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import router from '@/router'

export const TOKEN_KEY = 'admin_token'
export const USER_KEY = 'admin_user'

/** 后端统一响应体 { code, message, data } */
interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 Authorization: Bearer <token>
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let lastUnauthorizedAt = 0

/** 401：清空本地登录态并跳转登录页 */
function handleUnauthorized(message?: string) {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  const now = Date.now()
  if (now - lastUnauthorizedAt > 2000) {
    lastUnauthorizedAt = now
    ElMessage.error(message || '登录已失效，请重新登录')
  }
  if (router.currentRoute.value.path !== '/login') {
    router.replace('/login')
  }
}

// 响应拦截器：code===200 直接返回 data；其余统一报错
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data as ApiResult | undefined
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data as never
      }
      if (res.code === 401) {
        handleUnauthorized(res.message)
        return Promise.reject(new Error(res.message || '未登录或登录已失效'))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 非标准响应体，原样返回
    return response.data as never
  },
  (error) => {
    const res = error?.response?.data as ApiResult | undefined
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 401) {
        handleUnauthorized(res.message)
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res?.message || '请求失败'))
    }
    const status = error?.response?.status as number | undefined
    if (status === 401) {
      handleUnauthorized()
      return Promise.reject(new Error('未登录或登录已失效'))
    }
    ElMessage.error(status ? `请求失败（HTTP ${status}）` : '网络异常，请检查后端服务是否已启动')
    return Promise.reject(error)
  }
)

// 拦截器已将响应解包为 data，这里重新声明类型方便业务层使用
interface Request {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
}

const request = instance as unknown as Request

export default request

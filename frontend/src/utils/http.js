import axios from 'axios'

const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') !== 'false'
const BASE_API = import.meta.env.VITE_BASE_API || '/api'

// 全局变量，用于存储router实例
let routerInstance = null

/**
 * 设置router实例（在main.js中调用）
 */
export function setRouter(router) {
  routerInstance = router
}

const http = axios.create({
  baseURL: BASE_API,
  timeout: 15000,
  withCredentials: false,
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
})

// 请求拦截：自动注入 Token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理后端的 {code,message,data,timestamp}
http.interceptors.response.use(
  (response) => {
    // 二进制下载等直接放行
    const rt = response.request?.responseType
    const ct = response.headers?.['content-type'] || ''
    if (rt === 'blob' || rt === 'arraybuffer' || ct.includes('octet-stream')) {
      return response
    }

    const payload = response.data
    // 按后端统一结构解包
    if (payload && typeof payload === 'object' && 'code' in payload && 'data' in payload) {
      if (payload.code === 0) return payload.data
      const err = new Error(payload.message || '业务失败')
      err.code = payload.code
      err.response = response
      throw err
    }
    // 非统一结构（极少数兼容场景）
    return payload
  },
  (error) => {
    // 401 未授权或Token失效，清除 token 并跳转到登录页
    if (error.response?.status === 401) {
      // 清除本地存储的token和用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      
      // Token失效时自动跳转登录页
      // 只有在浏览器环境中才跳转
      if (typeof window !== 'undefined') {
        // 优先使用router实例跳转（更优雅），如果没有则使用window.location
        const currentPath = window.location.pathname
        if (currentPath !== '/' && currentPath !== '/login') {
          // 不在登录页时才跳转
          if (routerInstance) {
            // 使用router实例跳转
            routerInstance.push('/')
          } else {
            // 使用window.location跳转（兜底方案）
            window.location.href = '/'
          }
        }
      }
    }

    if (error.response) {
      const p = error.response.data
      const status = error.response.status
      
      // 403 权限不足，返回明确的错误信息
      if (status === 403) {
        const msg = p?.message || '权限不足：您没有执行此操作的权限'
        const err = new Error(msg)
        err.code = 403
        err.response = error.response
        throw err
      }
      
      // 其他错误
      const msg = p?.message || `HTTP ${status}`
      const err = new Error(msg)
      err.code = p?.code ?? status
      err.response = error.response
      throw err
    } else if (error.request) {
      throw new Error('网络不可用或服务器无响应')
    } else {
      throw new Error(error.message || '请求发生错误')
    }
  }
)

export { http, USE_MOCK }

// 为非 axios 请求（如原生 fetch / SSE）提供统一的认证与基础地址
export function getAuthHeaders() {
  const token = localStorage.getItem('token')
  const headers = {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
  if (token) headers['Authorization'] = `Bearer ${token}`
  return headers
}

export function getBaseApi() {
  return BASE_API
}


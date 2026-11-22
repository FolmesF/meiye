import axios from 'axios'

const TOKEN_KEY = 'meiye_booking_token'

let onError = () => {}
let onUnauthorized = () => {}

export function setHttpHandlers(handlers) {
  onError = handlers.onError || onError
  onUnauthorized = handlers.onUnauthorized || onUnauthorized
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function asRecords(data) {
  if (Array.isArray(data)) {
    return { records: data, total: data.length, page: 1, size: data.length || 20 }
  }
  if (data && typeof data === 'object') {
    const records = data.records || data.list || data.rows || []
    return {
      records: Array.isArray(records) ? records : [],
      total: Number(data.total ?? records.length ?? 0),
      page: Number(data.page || data.current || 1),
      size: Number(data.size || 20)
    }
  }
  return { records: [], total: 0, page: 1, size: 20 }
}

function errorMessage(error) {
  if (error.code === 'ECONNABORTED') return '请求超时，请稍后重试'
  if (!error.response) return '无法连接服务，请确认后端已启动'
  const data = error.response.data
  if (data && typeof data === 'object' && data.message) return data.message
  if (error.response.status === 404) return '接口不存在'
  if (error.response.status >= 500) return '服务暂时不可用'
  return '网络异常'
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: Number(import.meta.env.VITE_API_TIMEOUT || 15000)
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (!body || typeof body.code !== 'number') {
      return body
    }
    if (body.code === 0) {
      return body.data
    }
    if (body.code === 401) {
      setToken('')
      onUnauthorized()
    }
    if (!response.config.silent) {
      onError(body.message || '请求失败')
    }
    const error = new Error(body.message || '请求失败')
    error.code = body.code
    return Promise.reject(error)
  },
  (error) => {
    const message = errorMessage(error)
    if (error.response?.status === 401) {
      setToken('')
      onUnauthorized()
    }
    error.message = message
    if (!error.config?.silent) {
      onError(message)
    }
    return Promise.reject(error)
  }
)

export default http

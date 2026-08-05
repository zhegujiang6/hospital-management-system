import axios from 'axios'
import { clearAuth, getToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 8000,
})

request.interceptors.request.use((config) => {
  const token = getToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data

    if (result?.code !== 200) {
      return Promise.reject(new Error(result?.message || '请求失败'))
    }

    return result.data
  },
  (error) => {
    const status = error.response?.status
    const isLoginRequest = error.config?.url === '/auth/login'

    if (status === 401 && !isLoginRequest) {
      clearAuth()
      window.dispatchEvent(new Event('auth-expired'))
    }

    const message =
      error.response?.data?.message ||
      (status === 401
        ? '登录状态已失效，请重新登录'
        : status === 403
          ? '当前账号没有权限访问此功能'
        : error.code === 'ECONNABORTED'
          ? '请求超时，请稍后重试'
          : '无法连接后端服务')

    return Promise.reject(new Error(message))
  },
)

export default request

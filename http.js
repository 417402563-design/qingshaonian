import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '../utils/auth'
import router from '../router'

const http = axios.create({ baseURL: '/api', timeout: 90000 })

http.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  res => {
    const body = res.data
    if (body && body.code !== undefined && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body && body.code === 0 ? body.data : body
  },
  err => {
    const status = err.response?.status
    if (status === 401) {
      clearAuth()
      ElMessage.error('登录已失效，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(err.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(err)
  }
)

export default http

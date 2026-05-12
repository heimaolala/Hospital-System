import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      if (data.code === 401) {
        localStorage.clear()
        router.push('/login')
      }
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.clear()
        router.push('/login')
      }
      ElMessage.error(error.response.data?.message || '请求失败')
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('无法连接服务器，请确认后端已启动')
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

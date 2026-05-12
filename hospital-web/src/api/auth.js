import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function changePassword(data) {
  return request.put('/auth/change-password', data)
}

export function forceChangePassword(data) {
  return request.post('/auth/force-change-password', data)
}

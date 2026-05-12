import request from './request'

export function getCurrentUser() {
  return request.get('/users/me')
}

export function updateCurrentUser(data) {
  return request.put('/users/me', data)
}

export function getUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

export function approveUser(id, action) {
  return request.post(`/admin/users/${id}/approve`, null, { params: { action } })
}

export function batchApproveUsers(data) {
  return request.post('/admin/users/batch-approve', data)
}

export function getAutoApproveUserConfig() {
  return request.get('/admin/auto-audit-config')
}

export function updateAutoApproveUserConfig(data) {
  return request.put('/admin/auto-audit-config', data)
}

export function getAuditLogs(params) {
  return request.get('/admin/audit-logs', { params })
}

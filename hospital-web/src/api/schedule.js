import request from './request'

export function createSchedule(data) {
  return request.post('/schedules', data)
}

export function updateSchedule(id, data) {
  return request.put(`/schedules/${id}`, data)
}

export function deleteSchedule(id) {
  return request.delete(`/schedules/${id}`)
}

export function getMySchedules(params) {
  return request.get('/schedules/my', { params })
}

export function getAvailableSchedules(params) {
  return request.get('/schedules/available', { params })
}

export function getPendingSchedules(params) {
  return request.get('/admin/schedules/pending', { params })
}

export function auditSchedule(id, action, comment) {
  return request.post(`/admin/schedules/${id}/audit`, null, { params: { action, comment } })
}

export function getAutoAuditConfig() {
  return request.get('/admin/auto-audit-config')
}

export function updateAutoAuditConfig(data) {
  return request.put('/admin/auto-audit-config', data)
}

export function batchAuditSchedules(data) {
  return request.post('/admin/schedules/batch-audit', data)
}

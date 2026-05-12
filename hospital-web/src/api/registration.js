import request from './request'

export function registerBooking(data) {
  return request.post('/registrations', data)
}

export function payRegistration(id) {
  return request.post(`/registrations/${id}/pay`)
}

export function cancelRegistration(id) {
  return request.delete(`/registrations/${id}`)
}

export function getMyRegistrations(params) {
  return request.get('/registrations/my', { params })
}

export function getRemainingQuota(scheduleId) {
  return request.get(`/registrations/quota/${scheduleId}`)
}

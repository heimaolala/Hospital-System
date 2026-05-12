import request from './request'

export function getMyRecords() {
  return request.get('/medical-records/my')
}

export function getPatientRecords(patientId) {
  return request.get(`/medical-records/patient/${patientId}`)
}

export function searchPatients(params) {
  return request.get('/medical-records/search', { params })
}

export function createRecord(data) {
  return request.post('/medical-records', data)
}

export function updateRecord(id, data) {
  return request.put(`/medical-records/${id}`, data)
}

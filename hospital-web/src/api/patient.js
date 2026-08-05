import request from '@/utils/request'

export function getPatientList() {
  return request.get('/patients')
}

export function getPatientDetail(id) {
  return request.get(`/patients/${id}`)
}

export function createPatient(data) {
  return request.post('/patients', data)
}

export function updatePatient(id, data) {
  return request.put(`/patients/${id}`, data)
}

export function deletePatient(id) {
  return request.delete(`/patients/${id}`)
}

import request from '@/utils/request'

export function getInpatientAdmissionList() {
  return request.get('/inpatient-admissions')
}

export function getInpatientAdmissionDetail(id) {
  return request.get(`/inpatient-admissions/${id}`)
}

export function createInpatientAdmission(data) {
  return request.post('/inpatient-admissions', data)
}

export function dischargeInpatientAdmission(id) {
  return request.put(`/inpatient-admissions/${id}/discharge`)
}

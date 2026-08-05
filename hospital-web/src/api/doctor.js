import request from '@/utils/request'

export function getDoctorList() {
  return request.get('/doctors')
}

export function getDoctorDetail(id) {
  return request.get(`/doctors/${id}`)
}

export function createDoctor(data) {
  return request.post('/doctors', data)
}

export function updateDoctor(id, data) {
  return request.put(`/doctors/${id}`, data)
}

export function deleteDoctor(id) {
  return request.delete(`/doctors/${id}`)
}

import request from '@/utils/request'

export function getRegistrationList() {
  return request.get('/registrations')
}

export function getRegistrationDetail(id) {
  return request.get(`/registrations/${id}`)
}

export function createRegistration(data) {
  return request.post('/registrations', data)
}

export function cancelRegistration(id, data) {
  return request.post(`/registrations/${id}/cancel`, data)
}

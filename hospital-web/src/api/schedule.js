import request from '@/utils/request'

export function getScheduleList() {
  return request.get('/schedules')
}

export function getMyScheduleList() {
  return request.get('/doctor-schedules')
}

export function getScheduleDetail(id) {
  return request.get(`/schedules/${id}`)
}

export function createSchedule(data) {
  return request.post('/schedules', data)
}

export function updateSchedule(id, data) {
  return request.put(`/schedules/${id}`, data)
}

export function deleteSchedule(id) {
  return request.delete(`/schedules/${id}`)
}

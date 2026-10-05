import request from '@/utils/request'

export function getBedList() {
  return request.get('/beds')
}

export function getBedDetail(id) {
  return request.get(`/beds/${id}`)
}

export function createBed(data) {
  return request.post('/beds', data)
}

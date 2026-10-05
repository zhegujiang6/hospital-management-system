import request from '@/utils/request'

export function getWardList() {
  return request.get('/wards')
}

export async function getWardOptions() {
  const wards = (await getWardList()) || []
  return wards.filter((ward) => ward.status === 1)
}

export function getWardDetail(id) {
  return request.get(`/wards/${id}`)
}

export function createWard(data) {
  return request.post('/wards', data)
}

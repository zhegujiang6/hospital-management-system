import request from '@/utils/request'

export function getPendingVisits() {
  return request.get('/visit-records/pending')
}

export function createVisitRecord(data) {
  return request.post('/visit-records', data)
}

export function getVisitRecordList() {
  return request.get('/visit-records')
}

export function getVisitRecordDetail(id) {
  return request.get(`/visit-records/${id}`)
}

// 管理员查看全院就诊记录
export function getAdminVisitRecordList() {
  return request.get('/admin/visit-records')
}

// 管理员查看任意一条就诊记录的详情
export function getAdminVisitRecordDetail(id) {
  return request.get(`/admin/visit-records/${id}`)
}

import request from '@/utils/request'

export function getDepartmentList(params = {}) {
  return request.get('/departments', { params })
}

// 医生表单中的科室下拉框只需要启用状态的基础数据
export async function getDepartmentOptions() {
  const page = await getDepartmentList({
    pageNo: 1,
    pageSize: 100,
    status: 1,
  })

  return page?.records || []
}

export function getDepartmentDetail(id) {
  return request.get(`/departments/${id}`)
}

export function createDepartment(data) {
  return request.post('/departments', data)
}

export function updateDepartment(id, data) {
  return request.put(`/departments/${id}`, data)
}

export function deleteDepartment(id) {
  return request.delete(`/departments/${id}`)
}

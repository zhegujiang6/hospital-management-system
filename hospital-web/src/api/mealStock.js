import request from '@/utils/request'

export function getMealStockList(filters = {}) {
  const params = Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== '' && value != null),
  )

  return request.get('/meal/admin/stocks', { params })
}

export function createMealStock(data) {
  return request.post('/meal/admin/stocks', data)
}

export function updateMealStock(id, data) {
  return request.put(`/meal/admin/stocks/${id}`, data)
}

export function adjustMealStock(id, data) {
  return request.patch(`/meal/admin/stocks/${id}/adjust`, data)
}

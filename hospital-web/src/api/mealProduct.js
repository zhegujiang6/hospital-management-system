import request from '@/utils/request'

export function getMealProductList(filters = {}) {
  const params = Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== '' && value != null),
  )

  return request.get('/meal/admin/products', { params })
}

export function createMealProduct(data) {
  return request.post('/meal/admin/products', data)
}

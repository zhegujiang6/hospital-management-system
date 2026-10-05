import request from '@/utils/request'

export function getMealStoreList() {
  return request.get('/meal/admin/stores')
}

export function createMealStore(data) {
  return request.post('/meal/admin/stores', data)
}

import request from '@/utils/request'

export function getMealCategoryList(storeId) {
  return request.get('/meal/admin/categories', {
    params: storeId ? { storeId } : {},
  })
}

export function createMealCategory(data) {
  return request.post('/meal/admin/categories', data)
}

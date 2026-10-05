import request from '@/utils/request'

export function getMealPatientDeliveryInfo() {
  return request.get('/meal/patient/delivery-info')
}

export function createMealOrder(data) {
  return request.post('/meal/patient/orders', data)
}

export function getPatientMealOrders(params) {
  return request.get('/meal/patient/orders', { params })
}

export function getPatientMealOrderDetail(orderId) {
  return request.get(`/meal/patient/orders/${orderId}`)
}

export function simulateMealOrderPayment(orderId) {
  return request.post(`/meal/patient/orders/${orderId}/simulate-payment`)
}

export function cancelMealOrder(orderId) {
  return request.post(`/meal/patient/orders/${orderId}/cancel`)
}

export function getAdminMealOrders(params) {
  return request.get('/meal/admin/orders', { params })
}

export function getAdminMealOrderDetail(orderId) {
  return request.get(`/meal/admin/orders/${orderId}`)
}

export function prepareAdminMealOrder(orderId) {
  return request.post(`/meal/admin/orders/${orderId}/prepare`)
}

export function deliverAdminMealOrder(orderId) {
  return request.post(`/meal/admin/orders/${orderId}/deliver`)
}

export function completeAdminMealOrder(orderId) {
  return request.post(`/meal/admin/orders/${orderId}/complete`)
}

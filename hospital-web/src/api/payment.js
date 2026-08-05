import request from '@/utils/request'

export function createPayment(data) {
  return request.post('/payments', data)
}

export function mockPaymentSuccess(id) {
  return request.post(`/payments/${id}/mock-success`)
}

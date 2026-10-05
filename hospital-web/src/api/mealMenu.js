import request from '@/utils/request'

export function getPatientMealMenu(params) {
  return request.get('/meal/patient/menu', { params })
}

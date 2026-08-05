import request from '@/utils/request'

// 查询管理员数据概览
export function getDashboardOverview() {
  return request.get('/admin/dashboard')
}

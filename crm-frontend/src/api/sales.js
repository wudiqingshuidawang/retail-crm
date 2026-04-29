import request from '../utils/request'

export function getOrders(params) {
  return request.get('/orders', { params })
}

export function getOrder(id) {
  return request.get(`/orders/${id}`)
}

export function createOrder(data) {
  return request.post('/orders', data)
}

export function getSalesFunnel() {
  return request.get('/sales/funnel')
}

export function getFollowUps(params) {
  return request.get('/follow-ups', { params })
}

export function createFollowUp(data) {
  return request.post('/follow-ups', data)
}

export function updateFollowUp(id, data) {
  return request.put(`/follow-ups/${id}`, data)
}

export function updateFollowUpStatus(id, data) {
  return request.put(`/follow-ups/${id}/status`, data)
}

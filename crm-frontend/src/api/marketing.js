import request from '../utils/request'
export function getCampaigns(params) { return request.get('/campaigns', { params }) }
export function getCampaign(id) { return request.get(`/campaigns/${id}`) }
export function createCampaign(data) { return request.post('/campaigns', data) }
export function updateCampaign(id, data) { return request.put(`/campaigns/${id}`, data) }
export function deleteCampaign(id) { return request.delete(`/campaigns/${id}`) }
export function distributeCoupons(id) { return request.post(`/campaigns/${id}/distribute`) }
export function getCouponStats(id) { return request.get(`/campaigns/${id}/coupon-stats`) }
export function getCoupons(id) { return request.get(`/campaigns/${id}/coupons`) }
export function createCoupon(id, data) { return request.post(`/campaigns/${id}/coupons`, data) }
export function useCoupon(recordId) { return request.put(`/coupon-records/${recordId}/use`) }
export function getCustomerCoupons(customerId) { return request.get(`/customers/${customerId}/coupons`) }

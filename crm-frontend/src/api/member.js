import request from '../utils/request'
export function getMembers(params) { return request.get('/members', { params }) }
export function registerMember(data) { return request.post('/members/register', data) }
export function updatePoints(id, data) { return request.put(`/members/${id}/points`, data) }
export function getPointRecords(id) { return request.get(`/members/${id}/points`) }
export function getMemberProfile(id) { return request.get(`/members/${id}/profile`) }

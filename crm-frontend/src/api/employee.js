import request from '../utils/request'
export function getPerformance(month) { return request.get('/employees/performance', { params: { month } }) }
export function getTodos(params) { return request.get('/employees/todos', { params }) }
export function completeTodo(id) { return request.put(`/employees/todos/${id}/complete`) }

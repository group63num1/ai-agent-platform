import { http } from '@/utils/http'

// 工作流 API 封装
export function createWorkflow(data) {
  return http.post('/workflows', data)
}

export function getWorkflows(params = {}) {
  return http.get('/workflows', { params })
}

export function getWorkflow(id) {
  return http.get(`/workflows/${id}`)
}

export function deleteWorkflow(id) {
  return http.delete(`/workflows/${id}`)
}

export function saveWorkflowOrder(id, payload = {}) {
  return http.post(`/workflows/${id}/save`, payload)
}

export function executeWorkflow(id, payload = {}) {
  return http.post(`/workflows/${id}/execute`, payload)
}

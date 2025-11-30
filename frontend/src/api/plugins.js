import { http } from '@/utils/http'

export function fetchPlugins() {
  return http.get('/plugins')
}

export function fetchPluginDetail(id) {
  return http.get(`/plugins/${id}`)
}

export function createPlugin(payload) {
  return http.post('/plugins', payload)
}

export function updatePlugin(id, payload) {
  return http.put(`/plugins/${id}`, payload)
}

export function deletePlugin(id) {
  return http.delete(`/plugins/${id}`)
}

export function togglePlugin(id, enable) {
  return http.post(`/plugins/${id}/toggle`, null, { params: { enable } })
}

export function publishPlugin(id) {
  return http.post(`/plugins/${id}/publish`)
}

export function testPlugin(payload) {
  return http.post('/plugins/test', payload)
}

export function importPluginTemplate(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/plugins/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}


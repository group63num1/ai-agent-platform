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
/**
 * 获取插件列表
 * 兼容返回数组或 { items: [] }
 * @returns {Promise<Array>} [{ label, value }]
 */
export async function getPlugins() {
  const data = await http.get('/plugins/getlist')
  const list = Array.isArray(data) ? data : (data?.items || [])
  // 兼容字符串数组或对象数组
  return list.map(item => {
    if (typeof item === 'string') {
      return { label: item, value: item }
    }
    const label = item.label || item.name || item.id || '插件'
    const value = item.value || item.key || item.name || item.id || label
    return { label, value, raw: item }
  })
}


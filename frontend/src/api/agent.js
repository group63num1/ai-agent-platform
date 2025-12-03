import { http } from '@/utils/http'

/**
 * 创建智能体
 * @param {Object} data - 智能体数据
 * @returns {Promise<Object>}
 */
export function createAgent(data) {
  return http.post('/agents', data)
}

/**
 * 获取智能体列表
 * @param {Object} params - 查询参数（分页/筛选）
 * @returns {Promise<Array>}
 */
export function getAgents(params = {}) {
  return http.get('/agents', { params })
}

/**
 * 获取智能体详情
 * @param {string|number} id
 * @returns {Promise<Object>}
 */
export function getAgent(id) {
  return http.get(`/agents/${id}`)
}

/**
 * 更新智能体
 * @param {string|number} id
 * @param {Object} data
 * @returns {Promise<Object>}
 */
export function updateAgent(id, data) {
  return http.put(`/agents/${id}`, data)
}

/**
 * 删除智能体
 * @param {string|number} id
 * @returns {Promise<void>}
 */
export function deleteAgent(id) {
  return http.delete(`/agents/${id}`)
}

/**
 * 发布智能体
 * @param {string|number} id
 * @returns {Promise<Object>}
 */
export function publishAgent(id) {
  return http.post(`/agents/${id}/publish`)
}

/**
 * 与智能体对话（用于工作台调试）
 * @param {string|number} id
 * @param {Object} data - { message, model, contextRounds, maxTokens }
 * @returns {Promise<Object>} { content, role: 'assistant' }
 */
export function chatWithAgent(id, data) {
  return http.post(`/agents/${id}/chat`, data)
}

/**
 * 获取智能体的聊天历史（真实后端 /agents/:id/chat/messages）
 * 后端返回按时间升序最近消息；limit 可控制数量
 * @param {string|number} id
 * @param {Object} params - { limit }
 * @returns {Promise<Array>} [{ role, content }]
 */
export function getAgentChatMessages(id, params = {}) {
  return http.get(`/agents/${id}/chat/messages`, { params })
}

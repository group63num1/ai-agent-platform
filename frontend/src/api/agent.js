import { http } from '@/utils/http'

// 统一映射前端字段到后端字段（命名约定差异）
function mapAgentData(data = {}) {
  const out = { ...data }
  // 前端使用 knowledgeBases，后端期望 knowledge_base（数组）
  if (Array.isArray(data.knowledgeBases)) {
    out.knowledge_base = data.knowledgeBases
    delete out.knowledgeBases
  }
  return out
}

/**
 * 创建智能体
 * @param {Object} data - 智能体数据
 * @returns {Promise<Object>}
 */
export function createAgent(data) {
  return http.post('/agents', mapAgentData(data))
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
  return http.put(`/agents/${id}`, mapAgentData(data))
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
 * 下架智能体（发布 -> 草稿）
 */
export function unpublishAgent(id) {
  return http.post(`/agents/${id}/unpublish`)
}

/** 获取所有已发布智能体 */
export function getPublishedAgents() {
  return http.get('/agents/published')
}

// 会话管理（产品聊天）
export function createAgentSession(agentId, data = {}) {
  return http.post(`/agents/${agentId}/sessions`, data)
}
export function deleteAgentSession(agentId, sessionId) {
  return http.delete(`/agents/${agentId}/sessions/${sessionId}`)
}
export function getAgentSessions(agentId) {
  return http.get(`/agents/${agentId}/sessions`)
}
export function getAgentSessionMessages(agentId, sessionId, params = { limit: 100 }) {
  return http.get(`/agents/${agentId}/sessions/${sessionId}/messages`, { params })
}
export function updateAgentSessionName(agentId, sessionId, data) {
  return http.put(`/agents/${agentId}/sessions/${sessionId}`, data)
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

// 知识库列表（启用的，按更新时间倒序）
export function getKnowledgeBasesList() {
  return http.get('/knowledge-bases/getlist')
}

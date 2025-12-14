import { http } from '@/utils/http'

// 统一映射前端字段到后端字段（命名约定差异）
function mapAgentData(data = {}) {
  const out = { ...data }
  // 前端使用 knowledgeBases；新文档约定后端字段为 knowledgeBase（数组）
  if (Array.isArray(data.knowledgeBases)) {
    out.knowledgeBase = data.knowledgeBases
    delete out.knowledgeBases
  }
  // 兼容旧字段：若外部传入 knowledge_base，则同样转为新字段
  if (Array.isArray(data.knowledge_base) && !out.knowledgeBase) {
    out.knowledgeBase = data.knowledge_base
    delete out.knowledge_base
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

// 统一的会话聊天（SSE）封装
// 使用方式：chatAgentSessionSSE(agentId, sessionId, message, (chunk) => { ... })
// 当服务端发送 [DONE] 时，函数完成并返回最终累计的字符串
import { getAuthHeaders, getBaseApi } from '@/utils/http'

export async function chatAgentSessionSSE(agentId, sessionId, message, onChunk) {
  const base = getBaseApi()
  const resp = await fetch(`${base}/agents/${agentId}/sessions/${sessionId}/chat`, {
    method: 'POST',
    headers: { ...getAuthHeaders(), 'Accept': 'text/event-stream' },
    body: JSON.stringify({ message })
  })
  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    const chunk = decoder.decode(value, { stream: true })
    const lines = chunk.split(/\r?\n/)
    for (const line of lines) {
      if (!line.startsWith('data:')) continue
      const dataStr = line.slice(5).trim()
      if (dataStr === '[DONE]') {
        return buffer
      }
      try {
        const obj = JSON.parse(dataStr)
        if (obj?.content) {
          buffer += obj.content
          if (typeof onChunk === 'function') onChunk(obj.content)
        }
      } catch {
        // 非 JSON 片段忽略
      }
    }
  }
  return buffer
}

// 知识库列表（启用的，按更新时间倒序）
export function getKnowledgeBasesList() {
  return http.get('/knowledge-bases/getlist')
}

// 获取可用模型列表
// 返回后端原始结构，常用为 data.models（字符串显示名数组）
export function getModels() {
  return http.get('/models')
}

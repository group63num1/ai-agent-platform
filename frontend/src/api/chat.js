import { http } from '@/utils/http'

/**
 * 创建会话
 * @param {Object} data - 会话数据
 * @param {string} data.title - 会话标题（可选）
 * @returns {Promise} 返回会话信息
 */
export function createSession(data = {}) {
  return http.post('/v1/chat/sessions', data)
}

/**
 * 获取用户的会话列表
 * @returns {Promise} 返回会话列表
 */
export function getUserSessions() {
  return http.get('/v1/chat/sessions')
}

/**
 * 获取会话详情
 * @param {number} sessionId - 会话ID
 * @returns {Promise} 返回会话详情
 */
export function getSession(sessionId) {
  return http.get(`/v1/chat/sessions/${sessionId}`)
}

/**
 * 删除会话
 * @param {number} sessionId - 会话ID
 * @returns {Promise} 返回删除结果
 */
export function deleteSession(sessionId) {
  return http.delete(`/v1/chat/sessions/${sessionId}`)
}

/**
 * 更新会话标题
 * @param {number} sessionId - 会话ID
 * @param {string} title - 新标题
 * @returns {Promise} 返回更新后的会话信息
 */
export function updateSessionTitle(sessionId, title) {
  return http.put(`/v1/chat/sessions/${sessionId}/title?title=${encodeURIComponent(title)}`)
}

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {number} data.sessionId - 会话ID
 * @param {string} data.content - 消息内容
 * @returns {Promise} 返回用户消息和AI回复
 */
export function sendMessage(data) {
  return http.post('/v1/chat/messages', data)
}

/**
 * 获取会话的消息列表
 * @param {number} sessionId - 会话ID
 * @param {number} lastMessageId - 最后一条消息ID（用于分页，可选）
 * @param {number} limit - 每页数量（可选，默认20）
 * @returns {Promise} 返回消息列表
 */
export function getSessionMessages(sessionId, lastMessageId = null, limit = 20) {
  const params = { limit }
  if (lastMessageId) {
    params.lastMessageId = lastMessageId
  }
  return http.get(`/v1/chat/sessions/${sessionId}/messages`, { params })
}

/**
 * 获取会话的消息数量
 * @param {number} sessionId - 会话ID
 * @returns {Promise} 返回消息数量
 */
export function getMessageCount(sessionId) {
  return http.get(`/v1/chat/sessions/${sessionId}/message-count`)
}


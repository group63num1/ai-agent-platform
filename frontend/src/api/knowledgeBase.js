import { http } from '@/utils/http'

/**
 * 创建知识库
 * @param {Object} data - 知识库数据
 * @returns {Promise<Object>}
 */
export function createKnowledgeBase(data) {
  return http.post('/knowledge-bases', data)
}

/**
 * 获取知识库列表
 * @param {Object} params - 查询参数（分页/筛选）
 * @returns {Promise<Object>}
 */
export function getKnowledgeBases(params = {}) {
  return http.get('/knowledge-bases', { params })
}

/**
 * 获取知识库详情
 * @param {string|number} id
 * @returns {Promise<Object>}
 */
export function getKnowledgeBase(id) {
  return http.get(`/knowledge-bases/${id}`)
}

/**
 * 更新知识库
 * @param {string|number} id
 * @param {Object} data
 * @returns {Promise<Object>}
 */
export function updateKnowledgeBase(id, data) {
  return http.put(`/knowledge-bases/${id}`, data)
}

/**
 * 删除知识库
 * @param {string|number} id
 * @returns {Promise<void>}
 */
export function deleteKnowledgeBase(id) {
  return http.delete(`/knowledge-bases/${id}`)
}

/**
 * 获取知识库中的文档列表
 * @param {string|number} id - 知识库ID
 * @param {Object} params - 查询参数
 * @returns {Promise<Object>}
 */
export function getDocuments(id, params = {}) {
  return http.get(`/knowledge-bases/${id}/documents`, { params })
}

/**
 * 上传文档到知识库
 * @param {string|number} id - 知识库ID
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise<Object>}
 */
export function uploadDocument(id, formData) {
  return http.post(`/knowledge-bases/${id}/documents`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文档
 * @param {string|number} id - 知识库ID
 * @param {string|number} docId - 文档ID
 * @returns {Promise<void>}
 */
export function deleteDocument(id, docId) {
  return http.delete(`/knowledge-bases/${id}/documents/${docId}`)
}

/**
 * 向量检索测试
 * @param {string|number} id - 知识库ID
 * @param {Object} data - 查询参数 { query, topK, similarityThreshold }
 * @returns {Promise<Object>}
 */
export function vectorSearch(id, data) {
  return http.post(`/knowledge-bases/${id}/search`, data)
}

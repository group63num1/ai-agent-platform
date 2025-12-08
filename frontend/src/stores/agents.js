import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAgents, getAgent, createAgent, updateAgent, deleteAgent, publishAgent, unpublishAgent, getAgentSessions, deleteAgentSession } from '@/api/agent'

export const useAgentsStore = defineStore('agents', () => {
  const list = ref([])
  const total = ref(0)
  const loadingList = ref(false)
  const current = ref(null)
  const loadingDetail = ref(false)
  const saving = ref(false)
  const publishing = ref(false)
  const removing = ref(false)

  async function fetchList(params = {}) {
    loadingList.value = true
    try {
      const data = await getAgents(params)
      // 兼容返回结构：若后端直接返回数组或 {items,total}
      if (Array.isArray(data)) {
        list.value = data.filter(a => a.status !== 'published')
        total.value = data.length
      } else if (data && typeof data === 'object') {
        list.value = (data.items || []).filter(a => a.status !== 'published')
        total.value = data.total ?? list.value.length
      }
    } finally {
      loadingList.value = false
    }
  }

  async function fetchDetail(id) {
    loadingDetail.value = true
    try {
      current.value = await getAgent(id)
    } finally {
      loadingDetail.value = false
    }
  }

  async function create(payload) {
    saving.value = true
    try {
      // 强制在创建时使用固定模型标识
      const created = await createAgent({ model: 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', ...payload })
      // 追加到列表（可选：刷新列表替代）
      list.value.unshift(created)
      return created
    } finally {
      saving.value = false
    }
  }

  async function update(id, payload) {
    saving.value = true
    try {
      const updated = await updateAgent(id, payload)
      current.value = updated
      const idx = list.value.findIndex(a => a.id === id)
      if (idx !== -1) list.value[idx] = { ...list.value[idx], ...updated }
      return updated
    } finally {
      saving.value = false
    }
  }

  async function remove(id) {
    removing.value = true
    try {
      await deleteAgent(id)
      list.value = list.value.filter(a => a.id !== id)
      if (current.value?.id === id) current.value = null
    } finally {
      removing.value = false
    }
  }

  async function publish(id) {
    publishing.value = true
    try {
      const result = await publishAgent(id)
      // 发布后删除调试会话（按约定：删除所有该智能体会话）
      try {
        const sessions = await getAgentSessions(id)
        const list = Array.isArray(sessions) ? sessions : (sessions?.items || [])
        for (const s of list) {
          if (s?.sessionId) {
            await deleteAgentSession(id, s.sessionId)
          }
        }
      } catch (e) {
        console.warn('发布后清理会话失败：', e)
      }
      const idx = list.value.findIndex(a => a.id === id)
      if (idx !== -1) list.value[idx] = { ...list.value[idx], status: 'published', ...result }
      if (current.value?.id === id) current.value = { ...current.value, status: 'published', ...result }
      return result
    } finally {
      publishing.value = false
    }
  }

  async function unpublish(id) {
    publishing.value = true
    try {
      const result = await unpublishAgent(id)
      // 下架时删除所有会话
      try {
        const sessions = await getAgentSessions(id)
        const list = Array.isArray(sessions) ? sessions : (sessions?.items || [])
        for (const s of list) {
          if (s?.sessionId) {
            await deleteAgentSession(id, s.sessionId)
          }
        }
      } catch (e) {
        console.warn('下架清理会话失败：', e)
      }
      const idx = list.value.findIndex(a => a.id === id)
      if (idx !== -1) list.value[idx] = { ...list.value[idx], status: 'draft', ...result }
      if (current.value?.id === id) current.value = { ...current.value, status: 'draft', ...result }
      return result
    } finally {
      publishing.value = false
    }
  }

  return {
    list,
    total,
    current,
    loadingList,
    loadingDetail,
    saving,
    publishing,
    removing,
    fetchList,
    fetchDetail,
    create,
    update,
    remove,
    publish
    , unpublish
  }
})

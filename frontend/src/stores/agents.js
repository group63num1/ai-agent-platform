import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAgents, getAgent, createAgent, updateAgent, deleteAgent, publishAgent } from '@/api/agent'

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
        list.value = data
        total.value = data.length
      } else if (data && typeof data === 'object') {
        list.value = data.items || []
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
      const idx = list.value.findIndex(a => a.id === id)
      if (idx !== -1) list.value[idx] = { ...list.value[idx], status: 'published', ...result }
      if (current.value?.id === id) current.value = { ...current.value, status: 'published', ...result }
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
  }
})

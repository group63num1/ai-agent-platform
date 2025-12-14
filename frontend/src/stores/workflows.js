import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getWorkflows,
  getWorkflow,
  createWorkflow,
  deleteWorkflow,
  saveWorkflowOrder,
  executeWorkflow
} from '@/api/workflow'

export const useWorkflowsStore = defineStore('workflows', () => {
  const list = ref([])
  const total = ref(0)
  const loadingList = ref(false)
  const current = ref(null)
  const loadingDetail = ref(false)
  const saving = ref(false)
  const removing = ref(false)
  const running = ref(false)

  async function fetchList(params = {}) {
    loadingList.value = true
    try {
      const data = await getWorkflows(params)
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
      current.value = await getWorkflow(id)
    } finally {
      loadingDetail.value = false
    }
  }

  async function create(payload) {
    saving.value = true
    try {
      const created = await createWorkflow(payload)
      list.value.unshift(created)
      return created
    } finally {
      saving.value = false
    }
  }

  async function update(id, payload) {
    saving.value = true
    try {
      const updated = await saveWorkflowOrder(id, payload)
      current.value = updated
      const idx = list.value.findIndex(w => w.id === id)
      if (idx !== -1) list.value[idx] = { ...list.value[idx], ...updated }
      return updated
    } finally {
      saving.value = false
    }
  }

  async function remove(id) {
    removing.value = true
    try {
      await deleteWorkflow(id)
      list.value = list.value.filter(w => w.id !== id)
      if (current.value?.id === id) current.value = null
    } finally {
      removing.value = false
    }
  }

  async function run(id, payload = {}) {
    running.value = true
    try {
      return await executeWorkflow(id, payload)
    } finally {
      running.value = false
    }
  }

  return {
    list,
    total,
    current,
    loadingList,
    loadingDetail,
    saving,
    removing,
    running,
    fetchList,
    fetchDetail,
    create,
    update,
    remove,
    run
  }
})

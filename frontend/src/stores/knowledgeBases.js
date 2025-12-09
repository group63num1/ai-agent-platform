import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getKnowledgeBases,
  getKnowledgeBase,
  createKnowledgeBase,
  updateKnowledgeBase,
  deleteKnowledgeBase,
  getDocuments,
  uploadDocument,
  deleteDocument,
  vectorSearch
} from '@/api/knowledgeBase'

export const useKnowledgeBasesStore = defineStore('knowledgeBases', () => {
  const list = ref([])
  const total = ref(0)
  const loadingList = ref(false)
  const current = ref(null)
  const loadingDetail = ref(false)
  const saving = ref(false)
  const removing = ref(false)
  const documents = ref([])
  const loadingDocuments = ref(false)
  const uploading = ref(false)
  const searching = ref(false)

  async function fetchList(params = {}) {
    loadingList.value = true
    try {
      const data = await getKnowledgeBases(params)
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
      current.value = await getKnowledgeBase(id)
    } finally {
      loadingDetail.value = false
    }
  }

  async function create(payload) {
    saving.value = true
    try {
      const created = await createKnowledgeBase(payload)
      list.value.unshift(created)
      return created
    } finally {
      saving.value = false
    }
  }

  async function update(id, payload) {
    saving.value = true
    try {
      const updated = await updateKnowledgeBase(id, payload)
      current.value = updated
      const idx = list.value.findIndex(kb => kb.id === id)
      if (idx !== -1) list.value[idx] = { ...list.value[idx], ...updated }
      return updated
    } finally {
      saving.value = false
    }
  }

  async function remove(id) {
    removing.value = true
    try {
      await deleteKnowledgeBase(id)
      list.value = list.value.filter(kb => kb.id !== id)
      if (current.value?.id === id) current.value = null
    } finally {
      removing.value = false
    }
  }

  async function fetchDocuments(id, params = {}) {
    loadingDocuments.value = true
    try {
      const data = await getDocuments(id, params)
      if (Array.isArray(data)) {
        documents.value = data
      } else if (data && typeof data === 'object') {
        documents.value = data.items || []
      }
    } finally {
      loadingDocuments.value = false
    }
  }

  async function upload(id, formData) {
    uploading.value = true
    try {
      const result = await uploadDocument(id, formData)
      // 刷新文档列表
      await fetchDocuments(id)
      return result
    } finally {
      uploading.value = false
    }
  }

  async function removeDocument(id, docId) {
    try {
      await deleteDocument(id, docId)
      documents.value = documents.value.filter(doc => doc.id !== docId)
    } catch (error) {
      throw error
    }
  }

  async function search(id, queryData) {
    searching.value = true
    try {
      const result = await vectorSearch(id, queryData)
      return result
    } finally {
      searching.value = false
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
    documents,
    loadingDocuments,
    uploading,
    searching,
    fetchList,
    fetchDetail,
    create,
    update,
    remove,
    fetchDocuments,
    upload,
    removeDocument,
    search
  }
})

<template>
  <div class="bg-white rounded-lg p-6 shadow">
    <h3 class="text-lg font-semibold mb-4">知识库信息</h3>
    <el-form :model="kb" label-width="120px" readonly>
      <el-form-item label="名称">
        <span>{{ kb?.name || '-' }}</span>
      </el-form-item>
      <el-form-item label="应用域类型">
        <el-tag>{{ kb?.category || 'personal' }}</el-tag>
      </el-form-item>
      <el-form-item label="文档数">
        <span>{{ kb?.documentCount || 0 }}</span>
      </el-form-item>
      <el-form-item label="文本块数">
        <span>{{ kb?.chunkCount || 0 }}</span>
      </el-form-item>
      <el-form-item label="总大小">
        <span>{{ formatBytes(kb?.totalSize || 0) }}</span>
      </el-form-item>
      <el-form-item label="创建者">
        <span>{{ kb?.creator || '-' }}</span>
      </el-form-item>
      <el-form-item label="创建时间">
        <span>{{ formatDate(kb?.createdAt) }}</span>
      </el-form-item>
      <el-form-item v-if="kb?.description" label="描述">
        <p class="text-gray-600">{{ kb.description }}</p>
      </el-form-item>
    </el-form>

    <div class="flex gap-2 mt-6">
      <el-button type="primary" @click="handleEdit">编辑信息</el-button>
      <el-button type="danger" @click="handleDelete">删除知识库</el-button>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑知识库信息" width="600px">
      <KnowledgeBaseForm :knowledge-base="kb" @saved="onEditSaved" @cancel="editDialogVisible = false" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'
import KnowledgeBaseForm from '@/components/KnowledgeBaseForm.vue'

const props = defineProps({
  knowledgeBase: { type: Object, required: true }
})

const router = useRouter()
const store = useKnowledgeBasesStore()
const editDialogVisible = ref(false)

const kb = props.knowledgeBase

function formatDate(timestamp) {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

function formatBytes(bytes) {
  if (bytes === 0) return '0B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + sizes[i]
}

function handleEdit() {
  editDialogVisible.value = true
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除知识库"${kb?.name}"吗？此操作不可撤销。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await store.remove(kb.id)
    ElMessage.success('删除成功')
    router.push({ name: 'knowledgeBases' })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

function onEditSaved() {
  editDialogVisible.value = false
  ElMessage.success('更新成功')
}
</script>

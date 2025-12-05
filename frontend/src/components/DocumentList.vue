<template>
  <div class="bg-white rounded-lg p-6 shadow">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold">文档列表</h3>
      <el-button type="primary" @click="showUploadDialog = true">+ 上传文档</el-button>
    </div>

    <!-- 文档列表加载状态 -->
    <div v-if="store.loadingDocuments" class="p-4">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 文档列表 -->
    <div v-else>
      <div
        v-if="store.documents.length === 0"
        class="flex items-center justify-center h-32 bg-gray-50 rounded text-gray-500"
      >
        暂无文档，点击"上传文档"添加
      </div>

      <el-table v-else :data="store.documents" stripe style="width: 100%">
        <el-table-column prop="name" label="文档名称" min-width="200" />
        <el-table-column prop="chunkCount" label="文本块数" width="100">
          <template #default="{ row }">
            <span>{{ row.chunkCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="100">
          <template #default="{ row }">
            <span>{{ formatBytes(row.size) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'processed' ? 'success' : 'info'">
              {{ row.status === 'processed' ? '已处理' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadedAt" label="上传时间" width="180">
          <template #default="{ row }">
            <span>{{ formatDate(row.uploadedAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              size="small"
              type="danger"
              text
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 上传文档对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传文档"
      width="600px"
      @close="handleCloseUpload"
    >
      <DocumentUpload
        :knowledge-base-id="props.knowledgeBaseId"
        @uploaded="onUploaded"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'
import DocumentUpload from '@/components/DocumentUpload.vue'

const props = defineProps({
  knowledgeBaseId: { type: String, required: true }
})

const store = useKnowledgeBasesStore()
const showUploadDialog = ref(false)

onMounted(() => {
  loadDocuments()
})

function loadDocuments() {
  store.fetchDocuments(props.knowledgeBaseId)
}

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

async function handleDelete(document) {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档"${document.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await store.removeDocument(props.knowledgeBaseId, document.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

function onUploaded() {
  showUploadDialog.value = false
  loadDocuments()
  ElMessage.success('上传成功')
}

function handleCloseUpload() {
  showUploadDialog.value = false
}
</script>

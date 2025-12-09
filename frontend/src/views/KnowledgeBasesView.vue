<template>
  <div class="p-4 space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-semibold">知识库管理</h2>
      <el-button type="primary" @click="showCreate = true">+ 新建知识库</el-button>
    </div>

    <!-- 列表加载状态 -->
    <div v-if="store.loadingList" class="p-4">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 知识库列表 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-if="store.list.length === 0"
        class="col-span-full flex items-center justify-center h-40 bg-gray-50 rounded-lg"
      >
        <p class="text-gray-500">暂无知识库，点击"新建知识库"创建</p>
      </div>
      <div
        v-for="kb in store.list"
        :key="kb.id"
        class="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer border border-gray-200"
        @click="handleSelectKB(kb)"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1 min-w-0">
            <h3 class="font-semibold text-gray-800 truncate">{{ kb.name }}</h3>
            <p class="text-sm text-gray-500 mt-1 line-clamp-2">
              {{ kb.description || '暂无描述' }}
            </p>
            <div class="flex items-center gap-2 mt-3">
              <el-tag size="small">{{ kb.category || 'personal' }}</el-tag>
              <span class="text-xs text-gray-400">文档: {{ kb.documentCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="flex gap-2 mt-4">
          <el-button
            size="small"
            type="primary"
            text
            @click.stop="handleEnter(kb)"
          >
            进入
          </el-button>
          <el-button
            size="small"
            type="danger"
            text
            @click.stop="handleDelete(kb)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新建知识库对话框 -->
    <el-dialog
      v-model="showCreate"
      :title="editingKB ? '编辑知识库' : '新建知识库'"
      width="600px"
    >
      <KnowledgeBaseForm
        :knowledge-base="editingKB"
        @saved="onCreated"
        @cancel="handleCloseCreate"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'
import { ElMessage, ElMessageBox } from 'element-plus'
import KnowledgeBaseForm from '@/components/KnowledgeBaseForm.vue'

const router = useRouter()
const store = useKnowledgeBasesStore()
const showCreate = ref(false)
const editingKB = ref(null)

onMounted(() => {
  store.fetchList()
})

function handleSelectKB(kb) {
  router.push({ name: 'knowledgeBaseDetail', params: { id: kb.id } })
}

function handleEnter(kb) {
  router.push({ name: 'knowledgeBaseDetail', params: { id: kb.id } })
}

async function handleDelete(kb) {
  try {
    await ElMessageBox.confirm(`确定要删除知识库"${kb.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await store.remove(kb.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

function onCreated(created) {
  showCreate.value = false
  editingKB.value = null
  ElMessage.success('操作成功')
  store.fetchList()
}

function handleCloseCreate() {
  showCreate.value = false
  editingKB.value = null
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

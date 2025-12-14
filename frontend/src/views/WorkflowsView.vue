<template>
  <div class="p-4 space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold">工作流编排</h2>
        <p class="text-sm text-gray-500">设计、拖拽并连接节点，快速生成自动化链路</p>
      </div>
      <div class="flex items-center gap-2">
        <el-button @click="refresh">刷新</el-button>
        <el-button type="primary" @click="openCreate">+ 新建工作流（拖拽）</el-button>
      </div>
    </div>

    <div class="flex flex-wrap items-center gap-3">
      <el-input
        v-model="keyword"
        class="w-72"
        placeholder="按名称或标签搜索"
        clearable
        @clear="handleFilter"
        @keyup.enter="handleFilter"
      />
      <el-select
        v-model="statusFilter"
        placeholder="状态筛选"
        clearable
        class="w-48"
        @change="handleFilter"
      >
        <el-option label="草稿" value="draft" />
        <el-option label="已上线" value="published" />
      </el-select>
    </div>

    <div v-if="store.loadingList" class="p-4">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-if="displayed.length === 0"
        class="col-span-full flex items-center justify-center h-40 bg-gray-50 rounded-lg border border-dashed border-gray-200"
      >
        <p class="text-gray-500">暂无工作流，点击“新建工作流”开始编排</p>
      </div>

      <div
        v-for="flow in displayed"
        :key="flow.id"
        class="bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition"
      >
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold text-gray-800 truncate">{{ flow.name }}</h3>
              <el-tag size="small" :type="statusType(flow.status)">{{ statusText(flow.status) }}</el-tag>
            </div>
            <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ flow.intro || flow.description || '暂无描述' }}</p>
          </div>
        </div>

        <div class="flex flex-wrap gap-2 mt-3">
          <el-tag v-for="tag in flow.tags || []" :key="tag" type="info" size="small">{{ tag }}</el-tag>
          <el-tag v-if="!flow.tags || flow.tags.length === 0" size="small" type="info" effect="plain">未设置标签</el-tag>
        </div>

        <div class="grid grid-cols-2 gap-3 text-sm text-gray-600 mt-3">
          <div class="flex items-center gap-2">
            <el-tag size="small" effect="plain">{{ flow.triggerType || 'manual' }}</el-tag>
            <span class="text-gray-500">触发</span>
          </div>
          <div class="flex items-center gap-2 justify-end text-right">
            <span class="text-gray-500">步骤</span>
            <el-tag size="small" effect="plain">{{ (flow.agentIds || []).length }}</el-tag>
          </div>
        </div>

        <div class="flex items-center justify-between mt-4">
          <div class="text-xs text-gray-400">更新于 {{ formatTime(flow.updatedAt || flow.createdAt) }}</div>
          <div class="flex gap-2">
            <el-button size="small" text type="primary" @click="handleEdit(flow)">进入编排</el-button>
            <el-button size="small" text type="success" :loading="store.running" @click="handleRun(flow)">运行</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(flow)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useWorkflowsStore } from '@/stores/workflows'

const store = useWorkflowsStore()
const keyword = ref('')
const statusFilter = ref('')
const router = useRouter()

onMounted(() => {
  store.fetchList()
})

const displayed = computed(() => {
  return (store.list || []).filter(item => {
    const matchKeyword = keyword.value
      ? (item.name || '').includes(keyword.value) || (item.tags || []).some(t => t.includes(keyword.value))
      : true
    const matchStatus = statusFilter.value ? item.status === statusFilter.value : true
    return matchKeyword && matchStatus
  })
})

function statusText(status) {
  if (status === 'published') return '已上线'
  return '草稿'
}

function statusType(status) {
  if (status === 'published') return 'success'
  return 'info'
}

function formatTime(ts) {
  if (!ts) return '刚刚'
  const date = new Date(ts)
  return date.toLocaleString()
}

function openCreate() {
  router.push({ name: 'workflowBuilderNew' })
}

function handleEdit(flow) {
  router.push({ name: 'workflowBuilder', params: { id: flow.id } })
}

async function handleDelete(flow) {
  try {
    await ElMessageBox.confirm(`确认删除工作流"${flow.name}"吗？`, '提示', {
      type: 'warning'
    })
    await store.remove(flow.id)
    ElMessage.success('已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function handleRun(flow) {
  try {
    await store.run(flow.id, { input: 'preview-run' })
    ElMessage.success('已提交运行')
  } catch (error) {
    ElMessage.error(error.message || '运行失败')
  }
}

function handleFilter() {
  // 前端过滤，触发视图刷新
  keyword.value = keyword.value.trim()
}

function refresh() {
  store.fetchList()
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

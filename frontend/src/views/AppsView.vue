<template>
  <div class="apps-container">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">应用列表</h1>
      <p class="text-sm text-gray-500 mt-1">查看和管理所有应用</p>
    </div>

    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <el-button
          :type="sortBy === 'rating' ? 'primary' : 'default'"
          size="small"
          @click="handleSort('rating')"
        >
          评分 {{ sortBy === 'rating' ? (sortOrder === 'desc' ? '↓' : '↑') : '' }}
        </el-button>
        <el-button
          :type="sortBy === 'downloads' ? 'primary' : 'default'"
          size="small"
          @click="handleSort('downloads')"
        >
          下载 {{ sortBy === 'downloads' ? (sortOrder === 'desc' ? '↓' : '↑') : '' }}
        </el-button>
        <el-button
          :type="sortBy === '' ? 'primary' : 'default'"
          size="small"
          @click="handleClearSort"
        >
          默认
        </el-button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="5" animated />

    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      :closable="true"
      @close="error = ''"
      show-icon
      class="mb-6"
    />

    <div v-else-if="list.length === 0" class="text-center py-12">
      <p class="text-gray-500">暂无应用</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <AppCard v-for="item in list" :key="item.id" :item="item" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AppCard from '@/components/AppCard.vue'
import { getApps } from '@/data/getApps'

const loading = ref(false)
const error = ref('')
const list = ref([])
const sortBy = ref('')
const sortOrder = ref('desc')

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const apps = await getApps(sortBy.value, sortOrder.value)
    list.value = apps
  } catch (e) {
    error.value = e.message || '加载失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

const handleSort = (type) => {
  if (sortBy.value === type) {
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  } else {
    sortBy.value = type
    sortOrder.value = 'desc'
  }
  loadData()
}

const handleClearSort = () => {
  sortBy.value = ''
  sortOrder.value = 'desc'
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.apps-container {
  max-width: 1400px;
  margin: 0 auto;
}
</style>


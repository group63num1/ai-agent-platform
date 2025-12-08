<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-lg font-semibold">已发布产品</h2>
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4" v-loading="loading">
      <el-card
        v-for="item in products"
        :key="item.id"
        class="relative cursor-pointer hover:shadow-md transition-shadow"
        @click="openProduct(item)"
      >
        <div class="flex items-start justify-between mb-2">
          <h3 class="text-base font-semibold truncate pr-6">{{ item.name }}</h3>
          <el-tag size="small" type="success">{{ item.status }}</el-tag>
        </div>
        <p class="text-sm text-gray-600 line-clamp-3 min-h-[3.6em]">{{ item.description || '无简介' }}</p>
        <div class="absolute bottom-2 right-2" @click.stop>
          <el-dropdown @command="cmd => onCommand(item, cmd)">
            <span class="el-dropdown-link">
              <el-button text :icon="MoreFilled" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="unpublish">下架</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import { getPublishedAgents } from '@/api/agent'
import { useAgentsStore } from '@/stores/agents'

const router = useRouter()
const store = useAgentsStore()
const loading = ref(false)
const products = ref([])

async function refresh() {
  loading.value = true
  try {
    const data = await getPublishedAgents()
    products.value = Array.isArray(data) ? data : (data?.items || [])
  } finally {
    loading.value = false
  }
}

onMounted(refresh)

function openProduct(row) {
  router.push({ name: 'productChat', params: { id: row.id } })
}

async function onCommand(row, cmd) {
  if (cmd === 'unpublish') {
    try {
      await store.unpublish(row.id)
      ElMessage.success('已下架，已移回智能体模块')
      await refresh()
    } catch (e) {
      ElMessage.error(e.message || '下架失败')
    }
  }
}
</script>

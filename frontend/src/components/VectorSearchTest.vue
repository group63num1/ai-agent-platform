<template>
  <div class="bg-white rounded-lg p-6 shadow">
    <h3 class="text-lg font-semibold mb-4">向量检索测试</h3>

    <div class="space-y-4">
      <!-- 搜索输入 -->
      <div>
        <label class="block text-sm font-medium mb-2">查询内容</label>
        <el-input
          v-model="query"
          type="textarea"
          :rows="3"
          placeholder="输入查询内容，测试向量检索效果..."
        />
      </div>

      <!-- 参数设置 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-2">返回结果数</label>
          <el-input-number v-model="topK" :min="1" :max="20" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-2">相似度阈值</label>
          <el-slider v-model="similarityThreshold" :min="0" :max="1" :step="0.1" />
        </div>
      </div>

      <!-- 开始检索按钮 -->
      <div class="flex gap-2">
        <el-button
          type="primary"
          :loading="store.searching"
          @click="handleSearch"
          :disabled="!query.trim() || store.list.length === 0"
        >
          开始检索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 搜索结果提示 -->
      <div v-if="store.list.length === 0" class="p-4 bg-orange-50 border border-orange-200 rounded text-orange-700 text-sm">
        💡 提示：检索切分方式会影响检索效果，建议在文档列表中查看文档状态
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="mt-6">
        <h4 class="font-medium mb-3">检索结果 ({{ searchResults.length }} 条)</h4>
        <div class="space-y-3 max-h-96 overflow-y-auto">
          <div
            v-for="(result, index) in searchResults"
            :key="index"
            class="p-3 border border-gray-200 rounded hover:bg-blue-50 transition"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-800">
                  {{ result.content.substring(0, 100) }}...
                </p>
                <p class="text-xs text-gray-500 mt-1">来源: {{ result.source || '未知' }}</p>
              </div>
              <span class="text-sm font-semibold text-blue-600 whitespace-nowrap">
                {{ (result.score * 100).toFixed(0) }}%
              </span>
            </div>
            <div class="mt-2 w-full bg-gray-200 rounded-full h-1.5">
              <div
                class="bg-blue-600 h-1.5 rounded-full"
                :style="{ width: (result.score * 100) + '%' }"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 无结果提示 -->
      <div
        v-if="hasSearched && searchResults.length === 0"
        class="p-4 bg-gray-50 border border-gray-200 rounded text-gray-600 text-sm text-center"
      >
        没有找到相关内容，请尝试调整查询条件
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'

const props = defineProps({
  knowledgeBaseId: { type: String, required: true }
})

const store = useKnowledgeBasesStore()
const query = ref('')
const topK = ref(5)
const similarityThreshold = ref(0.5)
const searchResults = ref([])
const hasSearched = ref(false)

async function handleSearch() {
  if (!query.value.trim()) {
    ElMessage.warning('请输入查询内容')
    return
  }

  try {
    const result = await store.search(props.knowledgeBaseId, {
      query: query.value,
      topK: topK.value,
      similarityThreshold: similarityThreshold.value
    })

    searchResults.value = result?.items || result || []
    hasSearched.value = true

    if (searchResults.value.length === 0) {
      ElMessage.info('未找到符合条件的结果')
    } else {
      ElMessage.success(`找到 ${searchResults.value.length} 条结果`)
    }
  } catch (error) {
    ElMessage.error('检索失败: ' + (error.message || '未知错误'))
  }
}

function handleReset() {
  query.value = ''
  topK.value = 5
  similarityThreshold.value = 0.5
  searchResults.value = []
  hasSearched.value = false
}
</script>

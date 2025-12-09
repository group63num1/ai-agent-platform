<template>
  <div class="space-y-4">
    <!-- 返回按钮和标题 -->
    <div class="flex items-center gap-4">
      <el-button text @click="handleBack" :icon="ArrowLeft">返回</el-button>
      <h1 class="text-2xl font-semibold">{{ store.current?.name || '知识库详情' }}</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="store.loadingDetail" class="p-4">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 详情内容 -->
    <div v-else-if="store.current" class="space-y-4">
      <!-- 知识库信息 -->
      <KBInfo :knowledge-base="store.current" />

      <!-- 向量检索测试 -->
      <VectorSearchTest :knowledge-base-id="store.current.id" />

      <!-- 文档列表 -->
      <DocumentList :knowledge-base-id="store.current.id" />
    </div>

    <!-- 空状态 -->
    <div v-else class="flex items-center justify-center h-40 text-gray-500">
      未找到知识库
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'
import { ArrowLeft } from '@element-plus/icons-vue'
import KBInfo from '@/components/KBInfo.vue'
import VectorSearchTest from '@/components/VectorSearchTest.vue'
import DocumentList from '@/components/DocumentList.vue'

const router = useRouter()
const route = useRoute()
const store = useKnowledgeBasesStore()

onMounted(() => {
  const kbId = route.params.id
  if (kbId) {
    store.fetchDetail(kbId)
  }
})

function handleBack() {
  router.back()
}
</script>

<template>
  <div class="p-4 space-y-4" v-loading="loadingDetail">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-semibold">智能体详情</h2>
      <div class="space-x-2">
        <el-button type="success" :disabled="agent?.status==='published'" :loading="publishing" @click="onPublish">发布</el-button>
        <el-popconfirm title="确定删除该智能体?" @confirm="onRemove">
          <template #reference>
            <el-button type="danger" :loading="removing">删除</el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <div v-if="agent" class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <el-card>
          <template #header>
            <div class="flex items-center justify-between">
              <span>基本信息</span>
            </div>
          </template>
          <p><span class="font-medium">名称：</span>{{ agent.name }}</p>
          <p><span class="font-medium">模型：</span>{{ agent.model }}</p>
          <p><span class="font-medium">状态：</span>
            <el-tag :type="agent.status==='published' ? 'success' : 'info'">{{ agent.status || 'draft' }}</el-tag>
          </p>
          <p class="mt-2"><span class="font-medium">描述：</span>{{ agent.description || '—' }}</p>
          <p class="mt-2"><span class="font-medium">Prompt：</span>{{ agent.prompt || '—' }}</p>
        </el-card>
      </div>
      <div>
        <el-card>
          <template #header>
            <span>编辑</span>
          </template>
          <AgentForm :agent="agent" @saved="onUpdated" />
        </el-card>
      </div>
    </div>

    <el-empty v-else description="未找到该智能体" />
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAgentsStore } from '@/stores/agents'
import AgentForm from '@/components/AgentForm.vue'

const route = useRoute()
const router = useRouter()
const store = useAgentsStore()

const agent = computed(() => store.current)
const loadingDetail = computed(() => store.loadingDetail)
const publishing = computed(() => store.publishing)
const removing = computed(() => store.removing)

onMounted(() => {
  const id = route.params.id
  store.fetchDetail(id)
})

async function onPublish() {
  await store.publish(route.params.id)
}

async function onRemove() {
  await store.remove(route.params.id)
  router.push({ name: 'agents' })
}

function onUpdated() {
  // 可加入提示
}
</script>

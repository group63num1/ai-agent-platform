<template>
  <div class="p-4 space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-semibold">智能体管理</h2>
      <el-button type="primary" @click="showCreate = true">新建智能体</el-button>
    </div>

    <AgentList />

    <el-dialog v-model="showCreate" title="新建智能体" width="600px">
      <AgentForm @saved="onCreated" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAgentsStore } from '@/stores/agents'
import { useRouter } from 'vue-router'
import AgentList from '@/components/AgentList.vue'
import AgentForm from '@/components/AgentForm.vue'

const store = useAgentsStore()
const showCreate = ref(false)
const router = useRouter()

onMounted(() => {
  store.fetchList()
})

function onCreated(created) {
  showCreate.value = false
  // 创建成功跳转工作台页面
  if (created?.id) {
    router.push({ name: 'agentStudio', params: { id: created.id } })
  }
}
</script>

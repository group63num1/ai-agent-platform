<template>
  <div>
    <el-form :model="form" label-width="90px" @submit.prevent>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入智能体名称" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="功能/用途简介（可选）" />
        </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSubmit">{{ submitText }}</el-button>
        <el-button @click="onReset" :disabled="saving">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, watch, computed } from 'vue'
import { useAgentsStore } from '@/stores/agents'
const FIXED_MODEL = 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B'
import { getAgents, getPublishedAgents, createAgentSession, getAgentSessions, deleteAgentSession } from '@/api/agent'
import { ElMessage } from 'element-plus'

const props = defineProps({
  agent: { type: Object, default: null }
})
const emits = defineEmits(['saved'])

const store = useAgentsStore()
const form = reactive({
  name: '',
  description: ''
})

watch(() => props.agent, (val) => {
  if (val) {
    form.name = val.name || ''
    form.description = val.description || ''
  }
}, { immediate: true })

const saving = computed(() => store.saving)
const submitText = computed(() => props.agent ? '保存修改' : '创建智能体')

async function onSubmit() {
  const payload = { ...form, model: FIXED_MODEL }
  let result
  if (props.agent) {
    result = await store.update(props.agent.id, payload)
  } else {
    // 创建前校验重名（草稿或已发布均不允许重名）
    try {
      const draftList = await getAgents({ page: 1, pageSize: 1000 })
      const drafts = Array.isArray(draftList) ? draftList : (draftList?.items || [])
      const publishedList = await getPublishedAgents()
      const published = Array.isArray(publishedList) ? publishedList : (publishedList?.items || [])
      const allNames = new Set([...
        drafts.map(a => (a.name || '').trim()),
        ...published.map(a => (a.name || '').trim())
      ])
      const newName = (payload.name || '').trim()
      if (!newName) {
        ElMessage.error('名称不能为空')
        return
      }
      if (allNames.has(newName)) {
        ElMessage.error('不可创建同名智能体（包含草稿与已发布）')
        return
      }
    } catch (e) {
      // 若校验失败，仍继续走后端，由后端兜底
      console.warn('重名校验失败，交由后端判断：', e)
    }
    result = await store.create(payload)
    // 为新建智能体创建一个调试会话（若不存在）
    try {
      const sessions = await getAgentSessions(result.id)
      const list = Array.isArray(sessions) ? sessions : (sessions?.items || [])
      if (!list || list.length === 0) {
        await createAgentSession(result.id, { name: '调试会话' })
      }
    } catch (e) {
      console.warn('创建调试会话失败：', e)
    }
    // 新建后清空表单（可选）
    onReset()
  }
  emits('saved', result)
}

function onReset() {
  form.name = ''
  form.description = ''
  
}
</script>

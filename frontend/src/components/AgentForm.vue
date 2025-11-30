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
    result = await store.create(payload)
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

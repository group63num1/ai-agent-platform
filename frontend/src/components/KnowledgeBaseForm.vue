<template>
  <div>
    <el-form :model="form" label-width="90px" @submit.prevent>
      <el-form-item label="名称" required>
        <el-input v-model="form.name" placeholder="请输入知识库名称" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="知识库描述（可选）"
        />
      </el-form-item>
      <el-form-item label="应用域类型">
        <el-select v-model="form.category" placeholder="选择应用域类型">
          <el-option label="个人" value="personal" />
          <el-option label="企业" value="enterprise" />
          <el-option label="教育" value="education" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSubmit">{{ submitText }}</el-button>
        <el-button @click="onReset" :disabled="saving">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, watch, computed } from 'vue'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'

const props = defineProps({
  knowledgeBase: { type: Object, default: null }
})
const emits = defineEmits(['saved', 'cancel'])

const store = useKnowledgeBasesStore()
const form = reactive({
  name: '',
  description: '',
  category: 'personal'
})

watch(
  () => props.knowledgeBase,
  (val) => {
    if (val) {
      form.name = val.name || ''
      form.description = val.description || ''
      form.category = val.category || 'personal'
    }
  },
  { immediate: true }
)

const saving = computed(() => store.saving)
const submitText = computed(() => (props.knowledgeBase ? '保存修改' : '创建知识库'))

async function onSubmit() {
  if (!form.name.trim()) {
    alert('知识库名称不能为空')
    return
  }
  const payload = { ...form }
  let result
  if (props.knowledgeBase) {
    result = await store.update(props.knowledgeBase.id, payload)
  } else {
    result = await store.create(payload)
    onReset()
  }
  emits('saved', result)
}

function onReset() {
  if (!props.knowledgeBase) {
    form.name = ''
    form.description = ''
    form.category = 'personal'
  }
  emits('cancel')
}
</script>

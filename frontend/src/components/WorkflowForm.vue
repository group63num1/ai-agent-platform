<template>
  <el-form :model="form" label-width="100px" class="space-y-2" @submit.prevent>
    <el-form-item label="名称" required>
      <el-input v-model="form.name" placeholder="请输入工作流名称" />
    </el-form-item>

    <el-form-item label="描述">
      <el-input
        v-model="form.description"
        type="textarea"
        :rows="3"
        placeholder="简要说明这个工作流解决什么问题"
      />
    </el-form-item>

    <el-form-item label="状态">
      <el-select v-model="form.status" class="w-full">
        <el-option label="草稿" value="draft" />
        <el-option label="已上线" value="published" />
      </el-select>
    </el-form-item>

    <el-form-item label="触发方式">
      <el-select v-model="form.triggerType" class="w-full">
        <el-option label="手动" value="manual" />
        <el-option label="定时" value="schedule" />
        <el-option label="Webhook" value="webhook" />
      </el-select>
    </el-form-item>

    <el-form-item label="标签">
      <el-select
        v-model="form.tags"
        class="w-full"
        multiple
        filterable
        allow-create
        default-first-option
        placeholder="输入后回车可创建自定义标签"
      />
    </el-form-item>

    <el-form-item label="步骤" required>
      <div class="w-full space-y-2">
        <div
          v-for="(step, index) in form.steps"
          :key="step.id"
          class="border border-gray-200 rounded-md p-3 bg-gray-50"
        >
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-gray-600">步骤 {{ index + 1 }}</span>
            <el-button
              v-if="form.steps.length > 1"
              type="danger"
              link
              size="small"
              @click="removeStep(index)"
            >删除</el-button>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <el-input v-model="step.name" placeholder="步骤名称，例如：读取工单" />
            <el-select v-model="step.type" placeholder="步骤类型">
              <el-option label="LLM" value="llm" />
              <el-option label="HTTP 调用" value="http" />
              <el-option label="函数" value="function" />
              <el-option label="数据处理" value="data" />
            </el-select>
          </div>
          <el-input
            v-model="step.description"
            type="textarea"
            :rows="2"
            placeholder="该步骤的输入/输出、调用的插件或模型"
            class="mt-2"
          />
        </div>
        <el-button type="primary" text @click="addStep">+ 添加步骤</el-button>
      </div>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" :loading="saving" @click="onSubmit">{{ submitText }}</el-button>
      <el-button @click="emitCancel" :disabled="saving">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useWorkflowsStore } from '@/stores/workflows'

const props = defineProps({
  workflow: { type: Object, default: null }
})
const emit = defineEmits(['saved', 'cancel'])

const store = useWorkflowsStore()

const form = reactive({
  name: '',
  description: '',
  status: 'draft',
  triggerType: 'manual',
  tags: [],
  steps: []
})

const saving = computed(() => store.saving)
const submitText = computed(() => (props.workflow ? '保存修改' : '创建工作流'))

function hydrateFromProps(val) {
  if (!val) {
    form.name = ''
    form.description = ''
    form.status = 'draft'
    form.triggerType = 'manual'
    form.tags = []
    form.steps = [{ id: Date.now().toString(), name: '初始化', type: 'llm', description: '' }]
    return
  }
  form.name = val.name || ''
  form.description = val.description || ''
  form.status = val.status || 'draft'
  form.triggerType = val.triggerType || 'manual'
  form.tags = Array.isArray(val.tags) ? [...val.tags] : []
  form.steps = Array.isArray(val.steps) && val.steps.length > 0
    ? val.steps.map(s => ({
        id: s.id || `${Date.now()}-${Math.random()}`,
        name: s.name || '',
        type: s.type || 'llm',
        description: s.description || ''
      }))
    : [{ id: Date.now().toString(), name: '初始化', type: 'llm', description: '' }]
}

watch(() => props.workflow, (val) => {
  hydrateFromProps(val)
}, { immediate: true })

function addStep() {
  form.steps.push({
    id: `${Date.now()}-${Math.random()}`,
    name: '',
    type: 'llm',
    description: ''
  })
}

function removeStep(index) {
  form.steps.splice(index, 1)
}

function emitCancel() {
  emit('cancel')
}

function buildPayload() {
  return {
    name: form.name?.trim(),
    description: form.description?.trim(),
    status: form.status,
    triggerType: form.triggerType,
    tags: form.tags,
    steps: form.steps.map(({ name, type, description }, idx) => ({
      id: `${idx + 1}`,
      name,
      type,
      description
    }))
  }
}

async function onSubmit() {
  const payload = buildPayload()
  if (!payload.name) {
    ElMessage.error('名称不能为空')
    return
  }
  let result
  if (props.workflow?.id) {
    result = await store.update(props.workflow.id, payload)
  } else {
    result = await store.create(payload)
  }
  ElMessage.success('保存成功')
  emit('saved', result)
}
</script>

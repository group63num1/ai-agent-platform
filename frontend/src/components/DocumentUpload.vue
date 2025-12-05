<template>
  <div>
    <el-form label-width="90px" @submit.prevent>
      <!-- 文件选择 -->
      <el-form-item label="选择文件" required>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".txt,.md,.pdf"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到这里或<em>点击选择</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只支持 TXT 和 Markdown 格式，文件小于1MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <!-- 标题输入 -->
      <el-form-item label="标题">
        <el-input
          v-model="title"
          placeholder="留空则使用文件名"
        />
      </el-form-item>

      <!-- 切分方式 -->
      <el-form-item label="切分方式">
        <el-select v-model="splitMethod" placeholder="选择切分方式">
          <el-option label="固定大小（推荐）" value="fixed" />
          <el-option label="按段落" value="paragraph" />
          <el-option label="按句子" value="sentence" />
        </el-select>
      </el-form-item>

      <!-- 固定大小参数 -->
      <el-form-item v-if="splitMethod === 'fixed'" label="固定大小分块">
        <el-input-number
          v-model="chunkSize"
          :min="100"
          :max="2000"
          :step="100"
          placeholder="每块的字符数"
        />
        <span class="text-sm text-gray-500 ml-2">字符/块；重叠50字符</span>
      </el-form-item>

      <!-- 自动向量化 -->
      <el-form-item>
        <el-checkbox v-model="autoVectorize">
          自动向量化
          <el-icon class="text-gray-400">
            <info-filled />
          </el-icon>
        </el-checkbox>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" :loading="uploading" @click="handleSubmit">
          {{ uploading ? '上传中...' : '直接上传' }}
        </el-button>
        <el-button @click="handleCancel" :disabled="uploading">取消</el-button>
      </el-form-item>

      <!-- 进度显示 -->
      <div v-if="uploadProgress > 0" class="mt-4">
        <el-progress :percentage="uploadProgress" />
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, InfoFilled } from '@element-plus/icons-vue'
import { useKnowledgeBasesStore } from '@/stores/knowledgeBases'

const props = defineProps({
  knowledgeBaseId: { type: String, required: true }
})

const emits = defineEmits(['uploaded'])

const store = useKnowledgeBasesStore()
const uploadRef = ref(null)
const title = ref('')
const splitMethod = ref('fixed')
const chunkSize = ref(500)
const autoVectorize = ref(true)
const uploading = ref(false)
const uploadProgress = ref(0)
const selectedFile = ref(null)

function handleFileChange(file) {
  selectedFile.value = file
  if (!title.value) {
    title.value = file.name?.replace(/\.[^/.]+$/, '') || ''
  }
}

async function handleSubmit() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  const formData = new FormData()
  formData.append('file', selectedFile.value.raw || selectedFile.value)
  formData.append('title', title.value || selectedFile.value.name)
  formData.append('splitMethod', splitMethod.value)
  formData.append('chunkSize', chunkSize.value)
  formData.append('autoVectorize', autoVectorize.value)

  uploading.value = true
  uploadProgress.value = 0

  try {
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 30
      }
    }, 200)

    await store.upload(props.knowledgeBaseId, formData)

    clearInterval(progressInterval)
    uploadProgress.value = 100

    ElMessage.success('文档上传成功')
    emits('uploaded')

    // 重置表单
    setTimeout(() => {
      title.value = ''
      selectedFile.value = null
      uploadProgress.value = 0
      splitMethod.value = 'fixed'
      chunkSize.value = 500
      autoVectorize.value = true
    }, 500)
  } catch (error) {
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

function handleCancel() {
  title.value = ''
  selectedFile.value = null
  uploadProgress.value = 0
}
</script>

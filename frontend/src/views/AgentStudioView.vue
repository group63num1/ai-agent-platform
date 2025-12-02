<template>
  <div class="p-4 h-full flex flex-col gap-4">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2 class="text-lg font-semibold">智能体工作台</h2>
      </div>
      <div class="space-x-2">
        <el-button :loading="savingAll" type="primary" @click="saveAll">保存</el-button>
        <el-button :loading="publishing" type="success" @click="onPublish">发布</el-button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 flex-1">
      <!-- 左：人设与回复逻辑（Markdown） -->
      <el-card class="col-span-1 h-full">
        <template #header>
          <span>人设与回复逻辑</span>
        </template>
        <el-input v-model="profileMd" type="textarea" :rows="20" placeholder="在此用 Markdown 编写智能体的人设、语气、工作流与回复逻辑..." />
      </el-card>

      <!-- 中：编排模块（模型与参数、插件选择） -->
      <el-card class="col-span-1 h-full">
        <template #header>
          <span>编排模块</span>
        </template>
        <div class="space-y-4">
          <el-form label-width="120px">
            <el-form-item label="选择模型">
              <el-select v-model="settings.model" placeholder="选择模型">
                <!-- OpenAI 系列 -->
                <el-option label="OpenAI · gpt-4o" value="gpt-4o" />
                <el-option label="OpenAI · gpt-4o-mini" value="gpt-4o-mini" />
                <!-- 腾讯混元 -->
                <el-option label="腾讯混元 · hunyuan-lite" value="hunyuan-lite" />
                <el-option label="腾讯混元 · hunyuan-pro" value="hunyuan-pro" />
                <!-- 字节豆包 -->
                <el-option label="豆包 · doubao-lite" value="doubao-lite" />
                <el-option label="豆包 · doubao-pro" value="doubao-pro" />
                <!-- DeepSeek -->
                <el-option label="DeepSeek · deepseek-chat" value="deepseek-chat" />
                <el-option label="DeepSeek · deepseek-r1" value="deepseek-r1" />
              </el-select>
            </el-form-item>
            <el-form-item label="上下文轮数">
              <el-input-number v-model="settings.contextRounds" :min="0" :max="20" />
            </el-form-item>
            <el-form-item label="最大回复长度">
              <el-input-number v-model="settings.maxTokens" :min="64" :max="4096" />
            </el-form-item>
            <el-form-item label="插件选择">
              <el-select v-model="settings.plugins" multiple placeholder="选择插件">
                <el-option label="检索知识库" value="kb-retrieval" />
                <el-option label="网页抓取" value="web-scraper" />
                <el-option label="结构化填表" value="form-filler" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 右：预览与调试（聊天界面） -->
      <el-card class="col-span-1 h-full flex flex-col">
        <template #header>
          <span>预览与调试</span>
        </template>
        <div class="flex flex-col h-[70vh]">
          <div class="flex-1 overflow-auto space-y-3 pr-1">
            <div v-for="(m,i) in messages" :key="i" class="flex" :class="m.role==='user' ? 'justify-end' : 'justify-start'">
              <div class="max-w-[80%] px-3 py-2 rounded-lg" :class="m.role==='user' ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-800'">
                <div class="whitespace-pre-wrap">{{ m.content }}</div>
              </div>
            </div>
          </div>
          <div class="pt-2 flex items-end gap-2">
            <el-input v-model="userMessage" type="textarea" :rows="2" placeholder="输入你的问题..." />
            <el-button type="primary" :loading="chatting" @click="sendMessage">发送</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAgentsStore } from '@/stores/agents'
import { updateAgent, getAgent } from '@/api/agent'
import { chatWithAgent, getAgentChatMessages } from '@/api/agent'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const store = useAgentsStore()
const agentId = route.params.id

const profileMd = ref('')
const savingAll = ref(false)

const settings = reactive({
  model: 'hunyuan',
  contextRounds: 3,
  maxTokens: 512,
  plugins: []
})
const publishing = computed(() => store.publishing)

const userMessage = ref('')
const chatting = ref(false)
const messages = ref([])
const localHistoryKey = `agent_chat_history_${agentId}`
const FIXED_MODEL = 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B'

onMounted(async () => {
  await store.fetchDetail(agentId)
  const a = await getAgent(agentId)
  profileMd.value = a.profileMd || ''
  settings.model = a.model || settings.model
  settings.contextRounds = a.contextRounds ?? settings.contextRounds
  settings.maxTokens = a.maxTokens ?? settings.maxTokens
  settings.plugins = a.plugins || []

  // 直接加载后端基于智能体的历史消息
  await loadHistory()
})

async function loadHistory() {
  try {
    const list = await getAgentChatMessages(agentId, { limit: 200 })
    if (Array.isArray(list)) {
      messages.value = list.map(m => ({ role: m.role || 'assistant', content: m.content || '' }))
      return
    }
  } catch (e) {
    console.warn('后端历史加载失败，使用本地缓存回退：', e)
  }
  loadLocalHistory()
}

function loadLocalHistory() {
  try {
    const raw = localStorage.getItem(localHistoryKey)
    if (raw) {
      const arr = JSON.parse(raw)
      if (Array.isArray(arr)) messages.value = arr
    }
  } catch {}
}
async function saveAll() {
  savingAll.value = true
  try {
    await updateAgent(agentId, {
      profileMd: profileMd.value,
      model: settings.model,
      contextRounds: settings.contextRounds,
      maxTokens: settings.maxTokens,
      plugins: settings.plugins
    })
    ElMessage.success('已保存所有设置')
  } finally {
    savingAll.value = false
  }
}

async function onPublish() {
  try {
    await store.publish(agentId)
    ElMessage.success('已发布')
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  }
}

function clearChat() {
  messages.value = []
}

async function sendMessage() {
  if (!userMessage.value.trim()) return
  chatting.value = true
  try {
    const content = userMessage.value
    messages.value.push({ role: 'user', content })
    // 强制使用后端指定的固定模型标识
    const payload = {
      message: content,
      model: FIXED_MODEL,
      contextRounds: settings.contextRounds,
      maxTokens: settings.maxTokens
    }
    const resp = await chatWithAgent(agentId, payload)
    const reply = resp?.content || resp?.message || (typeof resp === 'string' ? resp : JSON.stringify(resp))
    messages.value.push({ role: 'assistant', content: reply || '' })
    userMessage.value = ''
    // 本地持久化（作为后备，避免刷新丢失）
    try { localStorage.setItem(localHistoryKey, JSON.stringify(messages.value)) } catch {}
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    chatting.value = false
  }
}

function goBack() {
  router.push({ name: 'agents' })
}
</script>

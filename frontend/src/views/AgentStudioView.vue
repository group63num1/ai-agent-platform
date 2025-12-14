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
              <el-select v-model="settings.model" placeholder="选择模型" :loading="loadingModels">
                <el-option v-for="name in modelNames" :key="name" :label="name" :value="name" />
              </el-select>
            </el-form-item>
            <el-form-item label="上下文轮数">
              <el-input-number v-model="settings.contextRounds" :min="0" :max="20" />
            </el-form-item>
            <el-form-item label="最大回复长度">
              <el-input-number v-model="settings.maxTokens" :min="64" :max="4096" />
            </el-form-item>
            <!-- 插件管理：用独立容器避免 el-form-item 收缩内容区 -->
            <div class="w-full">
              <div class="w-full border border-dashed border-gray-300 rounded p-3 text-sm">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <el-button text  @click="togglePlugins">
                      <span v-if="!pluginsExpanded">&gt;</span>
                      <span v-else>v</span>
                    </el-button>
                    <span class="text-gray-800">插件管理</span>
                  </div>
                  <el-button plain type="primary" size="small" @click="openAddPluginDialog">添加</el-button>
                </div>
                <div v-if="pluginsExpanded" class="mt-2 space-y-2">
                  <div v-if="settings.plugins.length===0" class="text-gray-500">暂无插件</div>
                  <div v-else class="space-y-1">
                    <div v-for="(p, idx) in settings.plugins" :key="p" class="flex items-center justify-between">
                      <span>{{ displayPluginLabel(p) }}</span>
                      <el-button plain type="danger" size="small" @click="removePlugin(idx)">移除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 知识库管理：用独立容器避免 el-form-item 收缩内容区 -->
            <div class="w-full">
              <div class="w-full border border-dashed border-gray-300 rounded p-3 text-sm">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <el-button text  @click="toggleKBs">
                      <span v-if="!kbsExpanded">&gt;</span>
                      <span v-else>v</span>
                    </el-button>
                    <span class="text-gray-800">知识库管理</span>
                  </div>
                  <el-button plain type="primary" size="small" @click="openAddKBDialog">添加</el-button>
                </div>
                <div v-if="kbsExpanded" class="mt-2 space-y-2">
                  <div v-if="settings.knowledgeBases.length===0" class="text-gray-500">暂无知识库</div>
                  <div v-else class="space-y-1">
                    <div v-for="(kb, idx) in settings.knowledgeBases" :key="kb" class="flex items-center justify-between">
                      <span>{{ displayKBLabel(kb) }}</span>
                      <el-button plain type="danger" size="small" @click="removeKB(idx)">移除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
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
    <!-- 对话框区域 -->
    <el-dialog v-model="addPluginDialogVisible" title="添加插件" width="50%">
      <div class="space-y-2">
        <div v-for="opt in pluginOptions" :key="opt.value" class="flex items-center justify-between py-1 px-2 border-b border-gray-100">
          <span>{{ opt.label }}</span>
          <el-button type="primary" size="small" @click="addPlugin(opt.value); addPluginDialogVisible=false">添加</el-button>
        </div>
      </div>
      <template #footer>
        <el-button plain @click="addPluginDialogVisible=false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addKBDialogVisible" title="添加知识库" width="50%">
      <div class="space-y-2">
        <div v-for="opt in kbOptions" :key="opt.value" class="flex items-center justify-between py-1 px-2 border-b border-gray-100">
          <span>{{ opt.label }}</span>
          <el-button type="primary" size="small" @click="addKB(opt.value); addKBDialogVisible=false">添加</el-button>
        </div>
      </div>
      <template #footer>
        <el-button plain @click="addKBDialogVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAgentsStore } from '@/stores/agents'
import { updateAgent, getAgent, getAgentSessions, createAgentSession, getAgentSessionMessages, chatAgentSessionSSE, getModels } from '@/api/agent'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPlugins } from '@/api/plugins'
import { getKnowledgeBasesList } from '@/api/agent'
import { ElDialog } from 'element-plus'

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
  plugins: [],
  knowledgeBases: []
})
const publishing = computed(() => store.publishing)

const userMessage = ref('')
const chatting = ref(false)
const messages = ref([])
const currentSessionId = ref('')
const localHistoryKey = `agent_chat_history_${agentId}`
const FIXED_MODEL = 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B'
const pluginOptions = ref([])
const loadingPlugins = ref(false)
const addPluginDialogVisible = ref(false)
const kbOptions = ref([])
const addKBDialogVisible = ref(false)
const pluginsExpanded = ref(true)
const kbsExpanded = ref(true)
const modelNames = ref([])
const loadingModels = ref(false)

async function fetchPlugins() {
  loadingPlugins.value = true
  try {
    const options = await getPlugins()
    pluginOptions.value = options
  } catch (e) {
    console.warn('加载插件列表失败:', e)
  } finally {
    loadingPlugins.value = false
  }
}

onMounted(async () => {
  await store.fetchDetail(agentId)
  const a = await getAgent(agentId)
  profileMd.value = a.profileMd || ''
  settings.model = a.model || settings.model
  settings.contextRounds = a.contextRounds ?? settings.contextRounds
  settings.maxTokens = a.maxTokens ?? settings.maxTokens
  settings.plugins = a.plugins || []
  settings.knowledgeBases = a.knowledgeBases || a.knowledge_base || a.knowledgeBase || []

  // 草稿进入工作台时，如无会话则自动创建调试会话
  try {
    const sessions = await getAgentSessions(agentId)
    const list = Array.isArray(sessions) ? sessions : (sessions?.items || [])
    if (!list || list.length === 0) {
      await createAgentSession(agentId, { name: '调试会话' })
      const sessions2 = await getAgentSessions(agentId)
      const list2 = Array.isArray(sessions2) ? sessions2 : (sessions2?.items || [])
      if (list2.length > 0) currentSessionId.value = list2[0].sessionId
    } else {
      currentSessionId.value = list[0].sessionId
    }
  } catch (e) {
    console.warn('进入工作台自动创建调试会话失败：', e)
  }
  // 加载当前会话历史
  await loadHistory()

  // 加载模型显示名列表
  await fetchModelNames()
})

async function loadHistory() {
  if (!currentSessionId.value) return
  try {
    const list = await getAgentSessionMessages(agentId, currentSessionId.value, { limit: 200 })
    if (Array.isArray(list)) {
      messages.value = list.map(m => ({ role: m.role || 'assistant', content: m.content || '' }))
      return
    }
  } catch (e) {
    console.warn('会话历史加载失败，使用本地缓存回退：', e)
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
      plugins: settings.plugins,
      knowledgeBases: settings.knowledgeBases
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
    // 发布后跳转到智能体管理页面
    router.push({ name: 'agents' })
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
    let assistantBuffer = ''
    await chatAgentSessionSSE(agentId, currentSessionId.value, content, (delta) => {
      assistantBuffer += delta
    })
    if (assistantBuffer) {
      messages.value.push({ role: 'assistant', content: assistantBuffer })
    }
    userMessage.value = ''
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

// 插件管理逻辑
function togglePlugins() { pluginsExpanded.value = !pluginsExpanded.value }
function openAddPluginDialog() {
  if (pluginOptions.value.length === 0) fetchPlugins()
  addPluginDialogVisible.value = true
}
function displayPluginLabel(val) {
  const found = pluginOptions.value.find(x => x.value === val)
  return found ? found.label : val
}
function addPlugin(val) {
  if (!settings.plugins.includes(val)) settings.plugins.push(val)
}
function removePlugin(idx) { settings.plugins.splice(idx, 1) }

// 知识库管理逻辑（示例数据）
function toggleKBs() { kbsExpanded.value = !kbsExpanded.value }
function openAddKBDialog() { addKBDialogVisible.value = true }
onMounted(async () => {
  // 预加载知识库选项（启用的）
  try {
    const list = await getKnowledgeBasesList()
    // 映射为 {label,value}
    kbOptions.value = Array.isArray(list) ? list.map(name => ({ label: name, value: name })) : []
  } catch (e) {
    console.warn('加载知识库列表失败：', e)
  }
})
function displayKBLabel(val) {
  const found = kbOptions.value.find(x => x.value === val)
  return found ? found.label : val
}
function addKB(val) {
  if (!settings.knowledgeBases.includes(val)) settings.knowledgeBases.push(val)
}
function removeKB(idx) { settings.knowledgeBases.splice(idx, 1) }

async function fetchModelNames() {
  loadingModels.value = true
  try {
    const res = await getModels()
    const names = Array.isArray(res?.models) ? res.models : (Array.isArray(res) ? res : [])
    modelNames.value = names
    if (settings.model && !modelNames.value.includes(settings.model)) {
      settings.model = ''
    }
  } catch (e) {
    console.warn('加载模型列表失败：', e)
  } finally {
    loadingModels.value = false
  }
}
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
    <el-card class="col-span-1">
      <template #header>
        <span>会话列表</span>
        <div class="float-right space-x-2">
          <el-button size="small" type="primary" @click="createSession" :loading="creating">新建会话</el-button>
        </div>
      </template>
      <div>
        <el-empty v-if="sessions.length===0" description="暂无会话" />
        <el-menu v-else :default-active="activeSessionId" class="w-full">
          <el-menu-item v-for="s in sessions" :key="s.sessionId" :index="s.sessionId" @click="setActive(s.sessionId)">
            <div class="flex items-center justify-between w-full h-8">
              <div class="flex-1 min-w-0 flex items-center">
                <template v-if="editingSessionId === s.sessionId">
                  <el-input
                    v-model="editingName"
                    size="small"
                    @click.stop
                    @blur="finishInlineRename(s)"
                    @keyup.enter.native="finishInlineRename(s)"
                    placeholder="输入新的会话名称"
                    class="w-full align-middle"
                  />
                </template>
                <template v-else>
                  <span class="truncate">{{ s.name }}</span>
                </template>
              </div>
              <div class="ml-2 flex items-center h-8">
                <template v-if="editingSessionId !== s.sessionId">
                  <el-dropdown @command="cmd => onSessionCommand(s, cmd)">
                    <span class="el-dropdown-link">
                      <el-button text size="small" class="p-0 leading-none align-middle h-6">···</el-button>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="rename">重命名</el-dropdown-item>
                        <el-dropdown-item command="delete">
                          <span class="text-red-500">删除</span>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </template>
              </div>
            </div>
          </el-menu-item>
        </el-menu>
      </div>
    </el-card>

    <el-card class="col-span-3 h-full flex flex-col">
      <template #header>
        <div class="flex items-center gap-2">
          <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
          <span>{{ agentName || '产品聊天' }}</span>
        </div>
      </template>
      <div class="flex flex-col h-[70vh]">
        <div class="flex-1 overflow-auto space-y-3 pr-1" v-loading="loadingMessages">
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAgentSessions, createAgentSession, deleteAgentSession, getAgentSessionMessages, updateAgentSessionName } from '@/api/agent'
import { getAgent } from '@/api/agent'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const agentId = route.params.id
const agentName = ref('')
const agentStatus = ref('')

const sessions = ref([])
const activeSessionId = ref('')
const creating = ref(false)
const loadingMessages = ref(false)
const messages = ref([])
const userMessage = ref('')
const chatting = ref(false)
const editingSessionId = ref('')
const editingName = ref('')

async function loadSessions() {
  const list = await getAgentSessions(agentId)
  sessions.value = Array.isArray(list) ? list : (list?.items || [])
  // 产品进入无会话则自动创建一个
  if (!sessions.value || sessions.value.length === 0) {
    try {
      await createAgentSession(agentId, { name: agentStatus.value === 'published' ? '新对话' : '默认会话' })
      const res2 = await getAgentSessions(agentId)
      sessions.value = Array.isArray(res2) ? res2 : (res2?.items || [])
    } catch (e) {
      console.warn('自动创建产品会话失败：', e)
    }
  }
  if (!activeSessionId.value && sessions.value.length > 0) {
    setActive(sessions.value[0].sessionId)
  }
}

async function setActive(sessionId) {
  activeSessionId.value = sessionId
  await loadMessages()
}

async function loadMessages() {
  if (!activeSessionId.value) return
  loadingMessages.value = true
  try {
    const list = await getAgentSessionMessages(agentId, activeSessionId.value, { limit: 200 })
    messages.value = Array.isArray(list) ? list.map(m => ({ role: m.role, content: m.content })) : []
  } finally {
    loadingMessages.value = false
  }
}

async function createSession() {
  creating.value = true
  try {
    const s = await createAgentSession(agentId, { name: agentStatus.value === 'published' ? '新对话' : '' })
    await loadSessions()
    ElMessage.success('会话已创建')
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    creating.value = false
  }
}

async function removeSession(sessionId) {
  try {
    await deleteAgentSession(agentId, sessionId)
    await loadSessions()
    ElMessage.success('已删除会话')
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

function onSessionCommand(s, cmd) {
  if (cmd === 'rename') {
    editingSessionId.value = s.sessionId
    editingName.value = s.name || ''
  } else if (cmd === 'delete') {
    removeSession(s.sessionId)
  }
}

async function finishInlineRename(s) {
  const newName = (editingName.value || '').trim()
  try {
    if (newName && newName !== s.name) {
      await updateAgentSessionName(agentId, s.sessionId, { name: newName })
      await loadSessions()
      ElMessage.success('会话名称已更新')
    }
  } catch (e) {
    ElMessage.error(e.message || '重命名失败')
  } finally {
    editingSessionId.value = ''
    editingName.value = ''
  }
}

async function sendMessage() {
  if (!userMessage.value.trim() || !activeSessionId.value) return
  chatting.value = true
  try {
    messages.value.push({ role: 'user', content: userMessage.value })
    // SSE POST
    const resp = await fetch(`/api/agents/${agentId}/sessions/${activeSessionId.value}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' },
      body: JSON.stringify({ message: userMessage.value })
    })
    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let assistantBuffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      // 解析 SSE: 按行处理，提取以 'data: ' 开头的内容
      const lines = chunk.split(/\r?\n/)
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const dataStr = line.slice(5).trim()
          if (dataStr === '[DONE]') {
            if (assistantBuffer) {
              messages.value.push({ role: 'assistant', content: assistantBuffer })
              assistantBuffer = ''
            }
          } else {
            try {
              const obj = JSON.parse(dataStr)
              if (obj?.content) {
                assistantBuffer += obj.content
              }
            } catch {}
          }
        }
      }
    }
    // 流结束但没有 [DONE] 的情况也推送一次
    if (assistantBuffer) {
      messages.value.push({ role: 'assistant', content: assistantBuffer })
    }
    userMessage.value = ''
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    chatting.value = false
  }
}

onMounted(loadSessions)

async function loadAgentName() {
  try {
    const a = await getAgent(agentId)
    agentName.value = a?.name || ''
    agentStatus.value = a?.status || ''
  } catch {}
}
async function renameSessionPrompt(s) {
  try {
    const name = window.prompt('请输入新的会话名称', s.name || '')
    if (name && name !== s.name) {
      await updateAgentSessionName(agentId, s.sessionId, { name })
      await loadSessions()
      ElMessage.success('会话名称已更新')
    }
  } catch (e) {
    ElMessage.error(e.message || '重命名失败')
  }
}

onMounted(loadAgentName)

function goBack() {
  router.push({ name: 'products' })
}
</script>

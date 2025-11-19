<template>
  <div class="chat-container h-full flex flex-col">
    <!-- 会话列表侧边栏 -->
    <div class="flex flex-1 overflow-hidden">
      <aside v-if="showSidebar" class="sidebar w-64 bg-white border-r border-gray-200 flex flex-col">
        <div class="p-4 border-b border-gray-200">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-800">会话列表</h2>
            <el-button
              type="primary"
              size="small"
              :icon="Plus"
              @click="handleCreateSession"
            >
              新建会话
            </el-button>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto">
          <el-skeleton v-if="sessionsLoading" :rows="5" animated class="p-4" />
          <div v-else-if="sessions.length === 0" class="p-4 text-center text-gray-500 text-sm">
            暂无会话，点击"新建会话"开始对话
          </div>
          <div v-else class="p-2">
            <div
              v-for="session in sessions"
              :key="session.id"
              class="session-item p-3 mb-2 rounded-lg cursor-pointer transition-colors"
              :class="currentSessionId === session.id ? 'bg-blue-50 border border-blue-200' : 'hover:bg-gray-50'"
              @click="handleSelectSession(session.id)"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1 min-w-0">
                  <div class="font-medium text-gray-800 truncate">{{ session.title || '新对话' }}</div>
                  <div class="text-xs text-gray-500 mt-1">
                    {{ session.messageCount || 0 }} 条消息
                  </div>
                </div>
                <el-button
                  type="danger"
                  :icon="Delete"
                  size="small"
                  text
                  @click.stop="handleDeleteSession(session.id)"
                  class="ml-2"
                />
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 主聊天区域 -->
      <main class="flex-1 flex flex-col bg-gray-50">
        <!-- 聊天头部 -->
        <div class="chat-header bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <el-button
              v-if="!showSidebar"
              :icon="Menu"
              text
              @click="showSidebar = true"
            />
            <h3 class="text-lg font-semibold text-gray-800">
              {{ currentSession?.title || '智能客服助手' }}
            </h3>
            <el-button
              v-if="currentSession"
              :icon="Edit"
              text
              size="small"
              @click="handleEditTitle"
            />
          </div>
          <div class="flex items-center gap-2">
            <el-button
              type="primary"
              :icon="Plus"
              size="small"
              @click="handleCreateSession"
            >
              新建会话
            </el-button>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="chat-messages flex-1 overflow-y-auto p-4" ref="messagesContainer">
          <div v-if="!currentSessionId" class="flex items-center justify-center h-full">
            <div class="text-center text-gray-500">
              <el-icon class="text-6xl mb-4"><ChatDotSquare /></el-icon>
              <p class="text-lg mb-2">欢迎使用智能客服助手</p>
              <p class="text-sm">请选择一个会话或创建新会话开始对话</p>
            </div>
          </div>

          <div v-else-if="messagesLoading" class="flex items-center justify-center h-full">
            <el-skeleton :rows="3" animated />
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item flex"
              :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="message-bubble max-w-[70%] rounded-lg px-4 py-2"
                :class="message.role === 'user' 
                  ? 'bg-blue-500 text-white' 
                  : message.role === 'assistant'
                    ? 'bg-white border border-gray-200 text-gray-800'
                    : 'bg-gray-100 text-gray-600 text-sm'"
              >
                <div class="whitespace-pre-wrap break-words">{{ message.content }}</div>
                <div class="text-xs mt-1 opacity-70">
                  {{ formatTime(message.createdAt) }}
                </div>
              </div>
            </div>

            <!-- AI回复加载中 -->
            <div v-if="sending" class="flex justify-start">
              <div class="message-bubble bg-white border border-gray-200 rounded-lg px-4 py-2">
                <div class="flex items-center gap-2">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span class="text-gray-500">AI正在思考...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input bg-white border-t border-gray-200 p-4">
          <div v-if="!currentSessionId" class="text-center text-gray-500 text-sm">
            请先创建或选择一个会话
          </div>
          <div v-else class="flex items-end gap-2">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入您的问题..."
              :disabled="sending"
              @keydown.ctrl.enter="handleSendMessage"
              @keydown.meta.enter="handleSendMessage"
              class="flex-1"
            />
            <el-button
              type="primary"
              :icon="Promotion"
              :loading="sending"
              :disabled="!inputMessage.trim() || sending"
              @click="handleSendMessage"
            >
              发送
            </el-button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Edit,
  Menu,
  ChatDotSquare,
  Promotion,
  Loading
} from '@element-plus/icons-vue'
import {
  createSession,
  getUserSessions,
  getSession,
  deleteSession,
  updateSessionTitle,
  sendMessage,
  getSessionMessages
} from '@/api/chat'

const route = useRoute()
const router = useRouter()

// 状态管理
const showSidebar = ref(true)
const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)
const currentSession = ref(null)
const messages = ref([])
const messagesLoading = ref(false)
const sending = ref(false)
const inputMessage = ref('')
const messagesContainer = ref(null)

// 加载会话列表
const loadSessions = async () => {
  sessionsLoading.value = true
  try {
    const data = await getUserSessions()
    sessions.value = data || []
    
    // 如果没有当前会话且有会话列表，自动选择第一个
    if (!currentSessionId.value && sessions.value.length > 0) {
      handleSelectSession(sessions.value[0].id)
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败: ' + (error.message || '未知错误'))
  } finally {
    sessionsLoading.value = false
  }
}

// 创建新会话
const handleCreateSession = async () => {
  try {
    const newSession = await createSession({ title: '新对话' })
    sessions.value.unshift(newSession)
    handleSelectSession(newSession.id)
    ElMessage.success('会话创建成功')
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败: ' + (error.message || '未知错误'))
  }
}

// 选择会话
const handleSelectSession = async (sessionId) => {
  currentSessionId.value = sessionId
  currentSession.value = sessions.value.find(s => s.id === sessionId)
  await loadMessages(sessionId)
}

// 加载消息
const loadMessages = async (sessionId, lastMessageId = null) => {
  messagesLoading.value = true
  try {
    const data = await getSessionMessages(sessionId, lastMessageId, 100)
    if (lastMessageId) {
      // 追加消息（分页加载）
      messages.value = [...messages.value, ...(data || [])]
    } else {
      // 首次加载或重新加载
      messages.value = data || []
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败: ' + (error.message || '未知错误'))
  } finally {
    messagesLoading.value = false
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) {
    return
  }

  const userMessageContent = inputMessage.value.trim()
  inputMessage.value = ''

  // 如果没有会话，先创建会话
  if (!currentSessionId.value) {
    try {
      const newSession = await createSession({ title: userMessageContent.substring(0, 30) || '新对话' })
      sessions.value.unshift(newSession)
      currentSessionId.value = newSession.id
      currentSession.value = newSession
    } catch (error) {
      ElMessage.error('创建会话失败: ' + (error.message || '未知错误'))
      inputMessage.value = userMessageContent
      return
    }
  }

  sending.value = true

  try {
    const response = await sendMessage({
      sessionId: currentSessionId.value,
      content: userMessageContent
    })

    // 添加用户消息和AI回复到消息列表
    if (response.userMessage) {
      messages.value.push(response.userMessage)
    }
    if (response.assistantMessage) {
      messages.value.push(response.assistantMessage)
    }

    // 更新会话列表中的消息数量
    const session = sessions.value.find(s => s.id === currentSessionId.value)
    if (session) {
      session.messageCount = (session.messageCount || 0) + 2
    }

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败: ' + (error.message || '未知错误'))
    // 恢复输入框内容
    inputMessage.value = userMessageContent
  } finally {
    sending.value = false
  }
}

// 删除会话
const handleDeleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个会话吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteSession(sessionId)
    
    // 从列表中移除
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    
    // 如果删除的是当前会话，清空当前会话
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      currentSession.value = null
      messages.value = []
      
      // 如果还有其他会话，选择第一个
      if (sessions.value.length > 0) {
        handleSelectSession(sessions.value[0].id)
      }
    }

    ElMessage.success('会话删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error)
      ElMessage.error('删除会话失败: ' + (error.message || '未知错误'))
    }
  }
}

// 编辑会话标题
const handleEditTitle = async () => {
  try {
    const { value: newTitle } = await ElMessageBox.prompt(
      '请输入新的会话标题',
      '编辑标题',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: currentSession.value?.title || '新对话',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '标题不能为空'
          }
          if (value.trim().length > 100) {
            return '标题长度不能超过100个字符'
          }
          return true
        }
      }
    )

    await updateSessionTitle(currentSessionId.value, newTitle.trim())
    
    // 更新本地会话信息
    if (currentSession.value) {
      currentSession.value.title = newTitle.trim()
    }
    const session = sessions.value.find(s => s.id === currentSessionId.value)
    if (session) {
      session.title = newTitle.trim()
    }

    ElMessage.success('标题更新成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新标题失败:', error)
      ElMessage.error('更新标题失败: ' + (error.message || '未知错误'))
    }
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    nextTick(() => {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    })
  }
}

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 监听消息变化，自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 64px);
}

.sidebar {
  height: 100%;
}

.chat-messages {
  min-height: 0;
}

.message-bubble {
  word-wrap: break-word;
  word-break: break-word;
}

.session-item {
  transition: all 0.2s;
}

:deep(.el-textarea__inner) {
  resize: none;
}

:deep(.el-loading-spinner) {
  margin-top: 0;
}
</style>


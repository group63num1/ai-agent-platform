#!/usr/bin/env node
import express from 'express'
import cors from 'cors'
import { nanoid } from 'nanoid'

// 配置
const PORT = process.env.MOCK_PORT || 5175 // 避免与 Vite (默认5173) 冲突
const app = express()
app.use(cors())
app.use(express.json())

// 统一响应包装
function ok(data) {
  return { code: 0, message: 'success', data, timestamp: Date.now() }
}
function fail(code, message) {
  return { code, message, data: null, timestamp: Date.now() }
}

// 简单内存数据
const USERS = [
  { userId: 1, username: 'admin', nickname: '管理员', phone: '13800000000', bio: '系统管理员', avatarUrl: '' }
]
let AGENTS = [
  {
    id: nanoid(8),
    name: '知识问答助手',
    description: '处理常见问题的智能体',
    model: 'gpt-4o-mini',
    prompt: '请用简洁语言回答用户问题',
    status: 'draft',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    sessionId: nanoid(10),
    chatHistory: []
  }
]
let MENUS = [
  { title: '首页', path: '/home', icon: 'House', permission: 'home:view', sortOrder: 1 },
  { title: '应用管理', path: '/home/apps', icon: 'Grid', permission: 'apps:view', sortOrder: 2 },
  { title: '智能体管理', path: '/home/agents', icon: 'Grid', permission: 'agents:view', sortOrder: 3 },
  { title: '知识库管理', path: '/home/knowledge-bases', icon: 'Grid', permission: 'kb:view', sortOrder: 4 },
  { title: '工作流编排', path: '/home/workflows', icon: 'Grid', permission: 'workflow:view', sortOrder: 5 },
  { title: '插件管理', path: '/home/plugins', icon: 'Grid', permission: 'plugins:view', sortOrder: 6},
  { title: '个人信息', path: '/home/profile', icon: 'User', permission: 'profile:view', sortOrder: 9 }
]

// 知识库数据
let KNOWLEDGE_BASES = [
  {
    id: nanoid(8),
    name: '样例知识库',
    description: '这是一个示例知识库',
    category: 'personal',
    documentCount: 2,
    chunkCount: 10,
    totalSize: 5120,
    creator: 'admin',
    createdAt: Date.now() - 86400000,
    updatedAt: Date.now() - 3600000
  }
]

let DOCUMENTS = {}

// 工作流数据
let WORKFLOWS = [
  {
    id: nanoid(8),
    name: '客服分流',
    intro: '根据工单内容自动分配到合适的客服队列，并通知值班同学。',
    status: 'draft',
    triggerType: 'manual',
    tags: ['客服', '分配'],
    agentIds: [],
    createdAt: Date.now() - 7200000,
    updatedAt: Date.now() - 3600000
  }
]
let WORKFLOW_RUNS = {}

// 伪鉴权：约定一个固定 token
const VALID_TOKEN = 'mock-token'
function auth(req, res, next) {
  const authHeader = req.headers.authorization || ''
  const token = authHeader.replace(/Bearer\s+/i, '')
  if (!token) return res.status(401).json(fail(401, '未登录'))
  if (token !== VALID_TOKEN) return res.status(401).json(fail(401, 'Token 无效'))
  next()
}

// 登录
app.post('/api/v1/auth/login', (req, res) => {
  const { username } = req.body
  const user = USERS.find(u => u.username === username) || USERS[0]
  return res.json(ok({ userId: user.userId, username: user.username, token: VALID_TOKEN }))
})

// 用户信息
app.get('/api/v1/user/profile', auth, (req, res) => {
  const user = USERS[0]
  res.json(ok({ ...user }))
})
app.put('/api/v1/user/profile', auth, (req, res) => {
  Object.assign(USERS[0], req.body || {})
  res.json(ok({ ...USERS[0] }))
})
app.delete('/api/v1/user/profile', auth, (req, res) => {
  res.json(ok({ deleted: true }))
})

// 菜单
app.get('/api/v1/menus', auth, (req, res) => {
  res.json(ok(MENUS))
})

// Agents 列表（不强制鉴权，避免前端清除真实后端的 token）
app.get('/api/agents', (req, res) => {
  // 支持简单查询 & 分页 & 状态筛选
  let { page = 1, pageSize = 20, keyword = '', status } = req.query
  page = parseInt(page); pageSize = parseInt(pageSize)
  let items = AGENTS.filter(a => {
    const matchKw = !keyword || a.name?.includes(keyword) || a.description?.includes(keyword)
    const matchStatus = status ? a.status === status : true
    return matchKw && matchStatus
  })
  const total = items.length
  const start = (page - 1) * pageSize
  const end = start + pageSize
  items = items.slice(start, end)
  res.json(ok({ items, total, page, pageSize }))
})

// 创建（不强制鉴权）
app.post('/api/agents', (req, res) => {
  const { name, description, model, prompt } = req.body || {}
  if (!name) return res.status(400).json(fail(400, '名称必填'))
  const agent = {
    id: nanoid(8),
    name,
    description: description || '',
    model: model || 'gpt-4o-mini',
    prompt: prompt || '',
    status: 'draft',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    sessionId: nanoid(10),
    chatHistory: []
  }
  AGENTS.unshift(agent)
  res.json(ok(agent))
})

// 详情（不强制鉴权）
app.get('/api/agents/:id', (req, res) => {
  const agent = AGENTS.find(a => a.id === req.params.id)
  if (!agent) return res.status(404).json(fail(404, '未找到智能体'))
  res.json(ok(agent))
})

// 更新（不强制鉴权）
app.put('/api/agents/:id', (req, res) => {
  const idx = AGENTS.findIndex(a => a.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到智能体'))
  const patch = req.body || {}
  AGENTS[idx] = { ...AGENTS[idx], ...patch, updatedAt: Date.now() }
  res.json(ok(AGENTS[idx]))
})

// 删除（不强制鉴权）
app.delete('/api/agents/:id', (req, res) => {
  const idx = AGENTS.findIndex(a => a.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到智能体'))
  const removed = AGENTS[idx]
  AGENTS.splice(idx, 1)
  res.json(ok({ id: removed.id }))
})

// 发布（不强制鉴权）
app.post('/api/agents/:id/publish', (req, res) => {
  const idx = AGENTS.findIndex(a => a.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到智能体'))
  AGENTS[idx].status = 'published'
  AGENTS[idx].updatedAt = Date.now()
  res.json(ok({ id: AGENTS[idx].id, status: AGENTS[idx].status }))
})

// 获取所有已发布智能体
app.get('/api/agents/published', (req, res) => {
  const items = AGENTS.filter(a => a.status === 'published')
  res.json(ok({ items, total: items.length }))
})

// 会话管理（简单模拟）
const AGENT_SESSIONS = {} // { [agentId]: [{ sessionId, name, createdAt }] }
app.get('/api/agents/:id/sessions', (req, res) => {
  const agentId = req.params.id
  if (!AGENTS.find(a => a.id === agentId)) return res.status(404).json(fail(404, '未找到智能体'))
  const items = AGENT_SESSIONS[agentId] || []
  res.json(ok({ items, total: items.length }))
})
app.post('/api/agents/:id/sessions', (req, res) => {
  const agentId = req.params.id
  if (!AGENTS.find(a => a.id === agentId)) return res.status(404).json(fail(404, '未找到智能体'))
  const { name = '会话' } = req.body || {}
  const session = { sessionId: nanoid(10), name, createdAt: Date.now() }
  AGENT_SESSIONS[agentId] = AGENT_SESSIONS[agentId] || []
  AGENT_SESSIONS[agentId].push(session)
  res.json(ok(session))
})
app.delete('/api/agents/:id/sessions/:sessionId', (req, res) => {
  const agentId = req.params.id
  const sid = req.params.sessionId
  const list = AGENT_SESSIONS[agentId] || []
  const idx = list.findIndex(s => s.sessionId === sid)
  if (idx === -1) return res.status(404).json(fail(404, '未找到会话'))
  list.splice(idx, 1)
  AGENT_SESSIONS[agentId] = list
  res.json(ok({ deleted: true }))
})

// 与智能体对话（简单模拟，根据设置返回拼接内容）
app.post('/api/agents/:id/chat', (req, res) => {
  const idx = AGENTS.findIndex(a => a.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到智能体'))
  const { message = '', model = 'gpt-4o-mini', contextRounds = 3, maxTokens = 512 } = req.body || {}
  const agent = AGENTS[idx]
  const prefix = agent.profileMd ? `[人格设定生效] ` : ''
  const content = `${prefix}模型(${model}) 回复：${message.slice(0, maxTokens)}`
  // 记录历史：用户消息 + 助手回复
  agent.chatHistory.push({ role: 'user', content: message })
  agent.chatHistory.push({ role: 'assistant', content })
  // 按 contextRounds 进行简单截断（仅保留末尾若干轮）
  const maxPairs = Math.max(1, Number(contextRounds))
  const targetLen = maxPairs * 2
  if (agent.chatHistory.length > targetLen) {
    agent.chatHistory = agent.chatHistory.slice(agent.chatHistory.length - targetLen)
  }
  res.json(ok({ role: 'assistant', content }))
})

// 获取智能体聊天历史（用于工作台加载）
app.get('/api/agents/:id/chat/messages', (req, res) => {
  const idx = AGENTS.findIndex(a => a.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到智能体'))
  const agent = AGENTS[idx]
  const { limit = 100 } = req.query
  const lmt = parseInt(limit)
  const items = agent.chatHistory.slice(Math.max(0, agent.chatHistory.length - lmt))
  res.json(ok(items))
})

// ==================== 知识库管理 API ====================

// 获取知识库列表
app.get('/api/knowledge-bases', (req, res) => {
  let { page = 1, pageSize = 20, keyword = '' } = req.query
  page = parseInt(page)
  pageSize = parseInt(pageSize)
  let items = KNOWLEDGE_BASES.filter(
    kb => !keyword || kb.name.includes(keyword) || kb.description.includes(keyword)
  )
  const total = items.length
  const start = (page - 1) * pageSize
  const end = start + pageSize
  items = items.slice(start, end)
  res.json(ok({ items, total, page, pageSize }))
})

// 创建知识库
app.post('/api/knowledge-bases', (req, res) => {
  const { name, description, category } = req.body || {}
  if (!name) return res.status(400).json(fail(400, '名称必填'))
  const kb = {
    id: nanoid(8),
    name,
    description: description || '',
    category: category || 'personal',
    documentCount: 0,
    chunkCount: 0,
    totalSize: 0,
    creator: 'admin',
    createdAt: Date.now(),
    updatedAt: Date.now()
  }
  KNOWLEDGE_BASES.unshift(kb)
  DOCUMENTS[kb.id] = []
  res.json(ok(kb))
})

// 获取知识库详情
app.get('/api/knowledge-bases/:id', (req, res) => {
  const kb = KNOWLEDGE_BASES.find(k => k.id === req.params.id)
  if (!kb) return res.status(404).json(fail(404, '未找到知识库'))
  res.json(ok(kb))
})

// 更新知识库
app.put('/api/knowledge-bases/:id', (req, res) => {
  const idx = KNOWLEDGE_BASES.findIndex(k => k.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到知识库'))
  const patch = req.body || {}
  KNOWLEDGE_BASES[idx] = {
    ...KNOWLEDGE_BASES[idx],
    ...patch,
    updatedAt: Date.now()
  }
  res.json(ok(KNOWLEDGE_BASES[idx]))
})

// 删除知识库
app.delete('/api/knowledge-bases/:id', (req, res) => {
  const idx = KNOWLEDGE_BASES.findIndex(k => k.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到知识库'))
  const removed = KNOWLEDGE_BASES[idx]
  KNOWLEDGE_BASES.splice(idx, 1)
  delete DOCUMENTS[removed.id]
  res.json(ok({ id: removed.id }))
})

// 获取知识库的文档列表
app.get('/api/knowledge-bases/:id/documents', (req, res) => {
  if (!DOCUMENTS[req.params.id]) {
    return res.status(404).json(fail(404, '未找到知识库'))
  }
  const docs = DOCUMENTS[req.params.id] || []
  res.json(ok(docs))
})

// 上传文档
app.post('/api/knowledge-bases/:id/documents', (req, res) => {
  if (!DOCUMENTS[req.params.id]) {
    return res.status(404).json(fail(404, '未找到知识库'))
  }
  
  const { title = '新文档', splitMethod = 'fixed', chunkSize = 500, autoVectorize = true } = req.body || {}
  
  const doc = {
    id: nanoid(8),
    name: title,
    size: Math.floor(Math.random() * 100000) + 10000,
    chunkCount: Math.floor(Math.random() * 50) + 5,
    status: autoVectorize ? 'processing' : 'pending',
    uploadedAt: Date.now(),
    splitMethod,
    chunkSize
  }
  
  DOCUMENTS[req.params.id].push(doc)
  
  // 更新知识库统计
  const kb = KNOWLEDGE_BASES.find(k => k.id === req.params.id)
  if (kb) {
    kb.documentCount = DOCUMENTS[req.params.id].length
    kb.chunkCount = DOCUMENTS[req.params.id].reduce((sum, d) => sum + d.chunkCount, 0)
    kb.totalSize = DOCUMENTS[req.params.id].reduce((sum, d) => sum + d.size, 0)
    kb.updatedAt = Date.now()
    
    // 模拟处理完成
    setTimeout(() => {
      doc.status = 'processed'
    }, 2000)
  }
  
  res.json(ok(doc))
})

// 删除文档
app.delete('/api/knowledge-bases/:id/documents/:docId', (req, res) => {
  if (!DOCUMENTS[req.params.id]) {
    return res.status(404).json(fail(404, '未找到知识库'))
  }
  
  const idx = DOCUMENTS[req.params.id].findIndex(d => d.id === req.params.docId)
  if (idx === -1) {
    return res.status(404).json(fail(404, '未找到文档'))
  }
  
  const removed = DOCUMENTS[req.params.id][idx]
  DOCUMENTS[req.params.id].splice(idx, 1)
  
  // 更新知识库统计
  const kb = KNOWLEDGE_BASES.find(k => k.id === req.params.id)
  if (kb) {
    kb.documentCount = DOCUMENTS[req.params.id].length
    kb.chunkCount = DOCUMENTS[req.params.id].reduce((sum, d) => sum + d.chunkCount, 0)
    kb.totalSize = DOCUMENTS[req.params.id].reduce((sum, d) => sum + d.size, 0)
    kb.updatedAt = Date.now()
  }
  
  res.json(ok({ id: removed.id }))
})

// 向量检索
app.post('/api/knowledge-bases/:id/search', (req, res) => {
  if (!KNOWLEDGE_BASES.find(k => k.id === req.params.id)) {
    return res.status(404).json(fail(404, '未找到知识库'))
  }
  
  const { query = '', topK = 5, similarityThreshold = 0.5 } = req.body || {}
  
  if (!query.trim()) {
    return res.status(400).json(fail(400, '查询内容不能为空'))
  }
  
  // 模拟向量检索结果
  const mockResults = []
  const docCount = Math.min(topK, 3)
  
  for (let i = 0; i < docCount; i++) {
    const score = 0.95 - i * 0.15
    if (score >= similarityThreshold) {
      mockResults.push({
        id: nanoid(8),
        content: `这是查询"${query}"的第 ${i + 1} 个相关结果的摘要文本。包含了与你的查询相关的信息内容...`,
        source: `样例文档 ${i + 1}`,
        score: parseFloat(score.toFixed(2)),
        chunkIndex: i
      })
    }
  }
  
  res.json(ok({
    items: mockResults,
    total: mockResults.length,
    query,
    topK,
    similarityThreshold
  }))
})

// ==================== 工作流编排 API ====================
app.get('/api/workflows', (req, res) => {
  let { page = 1, pageSize = 20, keyword = '', status } = req.query
  page = parseInt(page)
  pageSize = parseInt(pageSize)
  let items = WORKFLOWS.filter(flow => {
    const kw = keyword.trim()
    const matchKeyword = kw ? (flow.name || '').includes(kw) || (flow.tags || []).some(t => t.includes(kw)) : true
    const matchStatus = status ? flow.status === status : true
    return matchKeyword && matchStatus
  })
  const total = items.length
  const start = (page - 1) * pageSize
  const end = start + pageSize
  items = items.slice(start, end)
  res.json(ok({ items, total, page, pageSize }))
})

app.post('/api/workflows', (req, res) => {
  const { name, intro = '', status = 'draft', triggerType = 'manual', tags = [] } = req.body || {}
  if (!name) return res.status(400).json(fail(400, '名称必填'))
  const now = Date.now()
  const flow = {
    id: nanoid(8),
    name,
    intro,
    status,
    triggerType,
    tags,
    agentIds: [],
    createdAt: now,
    updatedAt: now
  }
  WORKFLOWS.unshift(flow)
  res.json(ok(flow))
})

app.get('/api/workflows/:id', (req, res) => {
  const flow = WORKFLOWS.find(w => w.id === req.params.id)
  if (!flow) return res.status(404).json(fail(404, '未找到工作流'))
  res.json(ok(flow))
})

app.post('/api/workflows/:id/save', (req, res) => {
  const idx = WORKFLOWS.findIndex(w => w.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到工作流'))
  const { agentIds = [] } = req.body || {}
  if (!Array.isArray(agentIds)) return res.status(400).json(fail(400, 'agentIds 应为数组'))

  const invalid = agentIds.find(id => !AGENTS.find(a => a.id === id && a.status === 'published'))
  if (invalid) return res.status(400).json(fail(400, `Agent ${invalid} 不存在或未发布`))

  WORKFLOWS[idx] = {
    ...WORKFLOWS[idx],
    agentIds: [...agentIds],
    updatedAt: Date.now()
  }
  res.json(ok(WORKFLOWS[idx]))
})

app.delete('/api/workflows/:id', (req, res) => {
  const idx = WORKFLOWS.findIndex(w => w.id === req.params.id)
  if (idx === -1) return res.status(404).json(fail(404, '未找到工作流'))
  const removed = WORKFLOWS[idx]
  WORKFLOWS.splice(idx, 1)
  delete WORKFLOW_RUNS[removed.id]
  res.json(ok({ id: removed.id }))
})

app.post('/api/workflows/:id/execute', (req, res) => {
  const flow = WORKFLOWS.find(w => w.id === req.params.id)
  if (!flow) return res.status(404).json(fail(404, '未找到工作流'))
  const { input = '', nodeInputs = [], sessionId = nanoid(10) } = req.body || {}

  const results = (flow.agentIds || []).map(agentId => ({
    agentId,
    output: `模拟执行 Agent(${agentId})，输入：${input || '空'}`
  }))

  const output = results.length ? results[results.length - 1].output : ''
  res.json(ok({
    workflowId: flow.id,
    sessionId,
    nodeResults: results,
    output,
    nodeInputs
  }))
})

app.listen(PORT, () => {
  console.log(`Mock API server running at http://localhost:${PORT}`)
  console.log('Use token: mock-token')
})

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
  { title: '个人信息', path: '/home/profile', icon: 'User', permission: 'profile:view', sortOrder: 9 }
]

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
app.post('/api/auth/login', (req, res) => {
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
app.get('/api/menus', auth, (req, res) => {
  res.json(ok(MENUS))
})

// Agents 列表（不强制鉴权，避免前端清除真实后端的 token）
app.get('/api/agents', (req, res) => {
  // 支持简单查询 & 分页
  let { page = 1, pageSize = 20, keyword = '' } = req.query
  page = parseInt(page); pageSize = parseInt(pageSize)
  let items = AGENTS.filter(a => !keyword || a.name.includes(keyword) || a.description.includes(keyword))
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

app.listen(PORT, () => {
  console.log(`Mock API server running at http://localhost:${PORT}`)
  console.log('Use token: mock-token')
})

# 知识库管理功能说明

## 功能概述

为 AI Agent Platform 项目新增了完整的知识库管理功能，包括：

- ✅ 知识库的创建、编辑、删除
- ✅ 文档的上传和管理
- ✅ 向量检索测试
- ✅ 知识库详情页面

## 项目结构

### API 接口 (`src/api/knowledgeBase.js`)

提供以下接口：

```javascript
// 知识库管理
createKnowledgeBase(data)      // 创建知识库
getKnowledgeBases(params)      // 获取知识库列表
getKnowledgeBase(id)           // 获取知识库详情
updateKnowledgeBase(id, data)  // 更新知识库
deleteKnowledgeBase(id)        // 删除知识库

// 文档管理
getDocuments(id, params)       // 获取文档列表
uploadDocument(id, formData)   // 上传文档
deleteDocument(id, docId)      // 删除文档

// 向量检索
vectorSearch(id, data)         // 向量检索
```

### 数据存储 (`src/stores/knowledgeBases.js`)

使用 Pinia 管理状态，包括：

- `list`: 知识库列表
- `current`: 当前选中的知识库
- `documents`: 文档列表
- 各种加载状态和操作方法

### 视图页面

#### 1. 知识库列表页面 (`src/views/KnowledgeBasesView.vue`)

- 显示所有知识库卡片
- 支持新建知识库（点击按钮弹出对话框）
- 支持进入知识库详情
- 支持删除知识库

#### 2. 知识库详情页面 (`src/views/KnowledgeBaseDetailView.vue`)

包含三个主要部分：

**a) 知识库信息组件 (`KBInfo.vue`)**
- 显示知识库基本信息（名称、类型、文档数、创建时间等）
- 支持编辑和删除知识库

**b) 向量检索测试组件 (`VectorSearchTest.vue`)**
- 输入查询内容进行向量检索
- 可配置返回结果数 (topK) 和相似度阈值
- 展示检索结果（包括相关性评分和进度条）

**c) 文档列表组件 (`DocumentList.vue`)**
- 列表显示所有文档
- 显示文档信息：名称、块数、大小、状态、上传时间
- 支持删除文档
- 点击"上传文档"按钮打开上传对话框

### 组件

#### `KnowledgeBaseForm.vue`
- 创建/编辑知识库的表单
- 字段：名称、描述、应用域类型

#### `DocumentUpload.vue`
- 文档上传对话框
- 支持拖拽和点击选择文件
- 支持选择切分方式（固定大小/段落/句子）
- 支持自动向量化选项

## Mock API 端点

在 `mock-server.js` 中添加了以下端点：

```javascript
// 知识库
GET    /api/knowledge-bases              // 列表
POST   /api/knowledge-bases              // 创建
GET    /api/knowledge-bases/:id          // 详情
PUT    /api/knowledge-bases/:id          // 更新
DELETE /api/knowledge-bases/:id          // 删除

// 文档
GET    /api/knowledge-bases/:id/documents        // 文档列表
POST   /api/knowledge-bases/:id/documents        // 上传文档
DELETE /api/knowledge-bases/:id/documents/:docId // 删除文档

// 向量检索
POST   /api/knowledge-bases/:id/search           // 向量检索
```

## 路由配置

在 `src/router/index.js` 中添加了以下路由：

```javascript
{
  path: 'knowledge-bases',
  name: 'knowledgeBases',
  component: KnowledgeBases,
  meta: { title: '知识库管理', requiresAuth: true }
},
{
  path: 'knowledge-bases/:id',
  name: 'knowledgeBaseDetail',
  component: KnowledgeBaseDetail,
  meta: { title: '知识库详情', requiresAuth: true }
}
```

## 菜单集成

在 `HomeView.vue` 中添加了菜单项"知识库管理"，位置在应用管理和智能体管理之间。

## 使用流程

### 1. 创建知识库

1. 在左侧菜单点击"知识库管理"
2. 点击"+ 新建知识库"按钮
3. 填写知识库信息（名称、描述、应用域类型）
4. 点击"创建知识库"

### 2. 上传文档

1. 点击知识库卡片进入详情页
2. 在"文档列表"部分点击"+ 上传文档"
3. 选择文件（TXT、Markdown 格式）
4. 可选：修改标题、切分方式、块大小
5. 可选：取消勾选"自动向量化"
6. 点击"直接上传"

### 3. 进行向量检索

1. 在"向量检索测试"部分输入查询内容
2. 可选：调整返回结果数和相似度阈值
3. 点击"开始检索"
4. 查看搜索结果（包括相关度评分）

### 4. 管理文档

1. 在"文档列表"中查看所有文档
2. 可以删除不需要的文档
3. 文档信息包括：名称、块数、大小、处理状态

## 后续开发建议

### 1. 真实后端集成

- 将 mock 接口替换为真实后端 API
- 实现文件的实际上传存储
- 实现真实的向量化和检索算法
- 添加数据库持久化

### 2. 功能增强

- [ ] 支持批量上传文档
- [ ] 支持更多文件格式（PDF、DOCX 等）
- [ ] 知识库搜索和筛选
- [ ] 文档预览功能
- [ ] 向量模型选择
- [ ] 检索结果的文本高亮
- [ ] 知识库的权限管理
- [ ] 版本控制功能

### 3. 性能优化

- [ ] 文档列表分页加载
- [ ] 大文件分片上传
- [ ] 检索结果缓存
- [ ] 前端组件优化

### 4. 用户体验改进

- [ ] 上传进度条完善
- [ ] 错误提示详细化
- [ ] 操作确认对话框
- [ ] 快捷键支持
- [ ] 国际化支持

## 技术栈

- Vue 3 (Composition API)
- Element Plus UI 组件库
- Pinia 状态管理
- Axios HTTP 请求
- Express Mock 服务器
- Tailwind CSS 样式

## 文件清单

### 新增文件

```
src/
├── api/
│   └── knowledgeBase.js                    # API 接口定义
├── stores/
│   └── knowledgeBases.js                   # Pinia Store
├── views/
│   ├── KnowledgeBasesView.vue             # 知识库列表页面
│   └── KnowledgeBaseDetailView.vue        # 知识库详情页面
└── components/
    ├── KnowledgeBaseForm.vue               # 创建/编辑表单
    ├── KBInfo.vue                          # 知识库信息组件
    ├── VectorSearchTest.vue                # 向量检索测试组件
    ├── DocumentList.vue                    # 文档列表组件
    └── DocumentUpload.vue                  # 文档上传组件
```

### 修改文件

```
src/
├── router/index.js                         # 添加路由配置
└── views/
    └── HomeView.vue                        # 添加菜单项

mock-server.js                              # 添加 Mock API
```

## 测试方式

1. 启动前端开发服务器：
   ```bash
   npm run dev
   ```

2. 启动 Mock API 服务器（新终端）：
   ```bash
   node mock-server.js
   ```

3. 访问 `http://localhost:5173`（或显示的地址）

4. 登录后在菜单中点击"知识库管理"进行测试

## 常见问题

**Q: 如何修改 Mock 数据？**
A: 编辑 `mock-server.js` 文件中的 `KNOWLEDGE_BASES` 和 `DOCUMENTS` 变量。

**Q: 数据是否会持久化？**
A: 不会。Mock 服务器使用内存存储，刷新页面后数据会重置。

**Q: 如何集成真实后端？**
A: 更新 `src/api/knowledgeBase.js` 中的 API 端点，将 `/api/` 前缀改为实际后端地址。

---

**最后更新**: 2025年12月3日

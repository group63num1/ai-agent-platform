# 知识库管理功能 - 快速开始

## 🚀 功能介绍

已成功为项目添加完整的知识库管理系统，包括以下功能：

### 📋 页面列表

| 页面 | 功能 | 路由 |
|------|------|------|
| 知识库列表 | 创建、查看、删除知识库 | `/home/knowledge-bases` |
| 知识库详情 | 查看知识库信息、管理文档、向量检索 | `/home/knowledge-bases/:id` |

### 🎯 核心功能

1. **知识库管理**
   - ✅ 新建知识库（指定名称、描述、应用域类型）
   - ✅ 编辑知识库信息
   - ✅ 删除知识库
   - ✅ 知识库列表展示（卡片视图）

2. **文档管理**
   - ✅ 上传文档（支持 TXT、Markdown 格式）
   - ✅ 配置文档切分方式（固定大小/段落/句子）
   - ✅ 自动向量化选项
   - ✅ 文档列表展示（表格视图）
   - ✅ 删除文档

3. **向量检索**
   - ✅ 输入查询内容进行检索
   - ✅ 可调配 topK 和相似度阈值
   - ✅ 显示检索结果及相关度评分
   - ✅ 可视化相关度进度条

4. **UI 交互**
   - ✅ 新建知识库对话框（图1）
   - ✅ 文档上传对话框（图4）
   - ✅ 知识库信息/检索/文档三栏布局（图3）
   - ✅ 网格式知识库卡片列表（图2）

## 📁 项目结构

```
frontend/
├── src/
│   ├── api/
│   │   └── knowledgeBase.js          # API 接口（9 个方法）
│   ├── stores/
│   │   └── knowledgeBases.js         # Pinia 状态管理
│   ├── components/
│   │   ├── KnowledgeBaseForm.vue     # 创建/编辑表单
│   │   ├── KBInfo.vue                # 知识库信息组件
│   │   ├── VectorSearchTest.vue      # 向量检索组件
│   │   ├── DocumentList.vue          # 文档列表组件
│   │   └── DocumentUpload.vue        # 上传组件
│   ├── views/
│   │   ├── KnowledgeBasesView.vue    # 列表页面
│   │   └── KnowledgeBaseDetailView.vue # 详情页面
│   └── router/
│       └── index.js                  # 路由配置（已更新）
├── mock-server.js                     # Mock API（已扩展）
└── KNOWLEDGE_BASE_README.md          # 详细文档
```

## 🔧 技术实现

### API 层 (`src/api/knowledgeBase.js`)

```javascript
// 知识库操作
createKnowledgeBase(data)       // POST /api/knowledge-bases
getKnowledgeBases(params)       // GET /api/knowledge-bases
getKnowledgeBase(id)            // GET /api/knowledge-bases/:id
updateKnowledgeBase(id, data)   // PUT /api/knowledge-bases/:id
deleteKnowledgeBase(id)         // DELETE /api/knowledge-bases/:id

// 文档操作
getDocuments(id, params)        // GET /api/knowledge-bases/:id/documents
uploadDocument(id, formData)    // POST /api/knowledge-bases/:id/documents
deleteDocument(id, docId)       // DELETE /api/knowledge-bases/:id/documents/:docId

// 向量检索
vectorSearch(id, data)          // POST /api/knowledge-bases/:id/search
```

### Mock 数据结构

**知识库对象**
```javascript
{
  id: string,                    // 唯一标识
  name: string,                  // 知识库名称
  description: string,           // 描述
  category: string,              // 分类（personal/enterprise/education/other）
  documentCount: number,         // 文档数
  chunkCount: number,            // 文本块数
  totalSize: number,             // 总大小（字节）
  creator: string,               // 创建者
  createdAt: timestamp,          // 创建时间
  updatedAt: timestamp           // 更新时间
}
```

**文档对象**
```javascript
{
  id: string,                    // 唯一标识
  name: string,                  // 文档名称
  size: number,                  // 大小（字节）
  chunkCount: number,            // 块数
  status: string,                // 状态（pending/processing/processed）
  uploadedAt: timestamp,         // 上传时间
  splitMethod: string,           // 切分方式
  chunkSize: number              // 块大小
}
```

## 🎨 用户界面

### 1. 知识库列表（图2风格）
- 网格卡片展示
- 每张卡片显示：名称、描述、分类、文档数
- 操作按钮：进入、删除
- 新建按钮在顶部右侧

### 2. 创建知识库对话框（图1风格）
- 表单字段：名称、描述、应用域类型
- 验证：名称必填
- 操作：创建/取消

### 3. 知识库详情（图3风格）
分三个部分：

**左上：知识库信息**
- 展示基本信息
- 编辑/删除按钮

**右上：向量检索测试**
- 查询框
- 参数设置（topK、相似度阈值）
- 检索按钮
- 结果展示

**下方：文档列表**
- 表格展示文档
- 上传按钮打开对话框（图4）

### 4. 上传文档对话框（图4风格）
- 拖拽区域选择文件
- 标题输入
- 切分方式选择
- 块大小参数
- 自动向量化开关
- 上传进度条
- 上传/取消按钮

## 🚀 快速测试

### 1. 启动开发服务器

```bash
cd frontend
npm install
npm run dev
```

### 2. 启动 Mock API 服务器

```bash
# 在新终端
cd frontend
node mock-server.js
```

输出应该显示：
```
Mock API server running at http://localhost:5175
Use token: mock-token
```

### 3. 登录并访问

1. 打开 http://localhost:5173
2. 用户名：admin（或任意用户名）
3. 登录后点击左侧菜单"知识库管理"

### 4. 功能演示流程

**创建知识库：**
- 点击"+ 新建知识库"
- 填写表单（名称、描述、类型）
- 点击"创建知识库"

**上传文档：**
- 点击知识库卡片进入详情
- 在"文档列表"点击"+ 上传文档"
- 拖拽或选择文件
- 点击"直接上传"

**向量检索：**
- 在"向量检索测试"输入查询内容
- 调整参数（可选）
- 点击"开始检索"
- 查看结果

**删除操作：**
- 文档列表中的"删除"按钮
- 知识库卡片中的"删除"按钮
- 知识库信息中的"删除知识库"按钮

## 📊 API 响应格式

所有接口返回统一格式：

```javascript
{
  code: 0,           // 0 表示成功，非 0 表示错误
  message: "success", // 消息
  data: {},          // 数据
  timestamp: 1701619077000
}
```

## 🔐 认证

- Mock 服务器使用固定 token: `mock-token`
- 前端自动在 Authorization header 中发送 token
- HTTP 拦截器已配置

## 💾 数据存储

- Mock 数据存储在内存中
- 刷新页面后数据会重置
- 若要保留数据，需集成真实数据库

## 🔗 菜单集成

已在 `HomeView.vue` 中添加菜单项"知识库管理"：
- 路由：`/home/knowledge-bases`
- 位置：应用管理和智能体管理之间
- 图标：Grid（网格图标）
- 排序权重自动调整

## 📱 响应式设计

- 知识库卡片网格：1 列（手机）→ 2 列（平板）→ 3 列（桌面）
- 文档表格：水平滚动（手机）
- 对话框：自适应宽度

## 🎓 后续开发指南

详见 `KNOWLEDGE_BASE_README.md` 中的"后续开发建议"部分

### 重点集成项：

1. **真实后端集成**
   - 修改 API 端点前缀
   - 实现文件上传存储
   - 实现向量化和检索

2. **功能增强**
   - 批量上传
   - 更多文件格式支持
   - 文档预览
   - 权限管理

3. **性能优化**
   - 分页加载
   - 分片上传
   - 缓存机制

## ✅ 完成检查表

- [x] 知识库 CRUD 操作
- [x] 文档上传和管理
- [x] 向量检索功能
- [x] Mock API 实现
- [x] 路由配置
- [x] 菜单集成
- [x] 组件开发
- [x] UI 对话框
- [x] 错误处理
- [x] 加载状态
- [x] 用户反馈（提示信息）

## 📞 问题排查

**问题：菜单中看不到"知识库管理"**
- 检查 HomeView.vue 是否已更新
- 清理浏览器缓存，重新加载

**问题：API 请求 404**
- 确保 Mock 服务器在运行
- 检查端口 5175 是否被占用
- 查看浏览器控制台的具体错误

**问题：对话框无法打开**
- 检查 Element Plus 是否正确导入
- 查看 console 中的 Vue 警告

**问题：文件上传失败**
- 检查文件大小是否超过 1MB
- 检查文件格式是否为 TXT/Markdown
- 查看网络请求状态

---

**创建时间**: 2025年12月3日  
**最后更新**: 2025年12月3日  
**状态**: ✅ 完成

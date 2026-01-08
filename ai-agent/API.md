# API 说明

## 健康检查
- `GET /health`
  - 响应：`{ "status": "ok", "timestamp": "..." }`

## 模型
- `GET /api/models`
  - 响应：
    - `success` (bool)
    - `count` (number)
    - `models` (string[])：模型显示名列表（display_name 优先，其次 model_id）
    - `items` (Model[])：完整模型对象列表，字段包含：
      - `model_id` (string)：唯一 ID
      - `display_name` (string|null)：展示名
      - `base_url` (string)
      - `api_key` (string，敏感)
      - `enabled` (bool)
      - 生成参数：`max_tokens` `temperature` `top_p` `top_k` `frequency_penalty` `presence_penalty` `stop_sequences` `timeout`
      - `created_at` `updated_at`

## 对话（流式 SSE）
- `POST /api/chat`
- Body (ChatRequest)：
  - `message` (string, 必填)：用户问题
  - `model_id` (string, 必填)：模型 ID
  - `session_id` (string, 选填)：会话 ID，复用上下文
  - `tools` (string[]，选填)：工具 ID 列表，空/省略表示不用工具
  - `knowledge_bases` (string[]，选填)：知识库 ID 列表，空/省略表示不用 RAG
  - `system_prompt` (string，选填)
  - `history` (array<{role, content}>，选填)：传空列表会清空当轮历史，不传/设为 null 则使用服务端缓存的历史
- 响应：SSE 数据流 `data: {...}\n\n`，字段 `content`/`done`/`summary`，结束行 `data: [DONE]`

## 工具管理
- `POST /api/tools` 创建
  - Body：`user_id`(必填, string), `openapi`(必填, OpenAPI JSON对象)
  - 说明：自动解析OpenAPI JSON，为每个endpoint创建工具，tool_id格式为 `{user_id}_{operationId}`
- `PUT /api/tools/{tool_id}` 更新
  - Body：`name`(选), `purpose`(选), `version`(选), `call_method`(选), `parameters`(选), `user_settings`(选, Dict)
- `DELETE /api/tools/{tool_id}` 删除

## 知识库管理（当前 4 个接口）
- `POST /api/knowledge-bases` 创建
  - Body (KnowledgeBaseCreateRequest)：`user_id`(必填), `name`(必填), `files`(必填 List<{filename, content}>，可为空列表创建空库), `description`(选), `chunking_method`(选，默认 recursive), `chunk_size`(选，默认1000), `chunk_overlap`(选，默认200), `enabled`(选，默认 true)
- `POST /api/knowledge-bases/{kb_id}/query` 查询
  - Body (KnowledgeBaseQueryRequest)：`query_text`(选), `similarity_threshold`(选), `limit`(选，默认20)
  - 有 query_text 时按阈值返回 top5；无 query 时返回前 N 个 chunk
- `PUT /api/knowledge-bases/{kb_id}` 更新
  - Body (KnowledgeBaseUpdateRequest)：`name`/`description`/`enabled`/`files`/`chunking_method`/`chunk_size`/`chunk_overlap`（提供 files 或 chunk 配置会触发重建向量库）
- `DELETE /api/knowledge-bases/{kb_id}` 删除（含向量与 DB 记录）

## 示例

### Chat（SSE）
```
curl -N -H "Content-Type: application/json" \
  -X POST http://localhost:8000/api/chat \
  -d '{
    "message": "北京和广州天气对比",
    "model_id": "qwen3-max",
    "session_id": "sess1",
    "tools": ["test_user_getWeatherInfo"],
    "knowledge_bases": []
  }'
```

### 创建工具
```
curl -H "Content-Type: application/json" -X POST http://localhost:8000/api/tools -d '{
  "user_id": "test_user",
  "openapi": {
    "openapi": "3.0.0",
    "info": {"title": "天气查询", "version": "1.0.0"},
    "paths": {
      "/weather": {
        "get": {
          "operationId": "getWeatherInfo",
          "summary": "查询实时天气",
          "parameters": [{"name":"city","in":"query","required":true}]
        }
      }
    }
  }
}'
```

### 创建知识库
```
curl -H "Content-Type: application/json" -X POST http://localhost:8000/api/knowledge-bases -d '{
  "user_id": "u1",
  "name": "kb_demo",
  "files": [{"filename":"a.txt","content":"hello world"}],
  "description": "demo kb"
}'
```

# AI Agent API 使用文档

## 📋 模型管理接口

### 1. 获取模型列表

**请求：**
```http
GET http://localhost:8000/api/models?enabled_only=false
```

**响应：**
```json
{
  "success": true,
  "models": [
    {
      "model_id": "qwen-plus",
      "display_name": "通义千问Plus",
      "model": "qwen-plus",
      "api_key": "sk-xxx",
      "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1",
      "enabled": true,
      "description": "阿里云大模型",
      "max_tokens": 2000,
      "temperature": 0.7
    },
    {
      "model_id": "deepseek-chat",
      "display_name": "DeepSeek",
      "model": "deepseek-chat",
      "api_key": "sk-xxx",
      "base_url": "https://api.deepseek.com",
      "enabled": true,
      "description": "DeepSeek模型",
      "max_tokens": 4000,
      "temperature": 0.7
    }
  ]
}
```

**参数：**
- `enabled_only` (可选): 是否只返回启用的模型，默认 `false`

---

### 2. 获取单个模型详情

**请求：**
```http
GET http://localhost:8000/api/models/qwen-plus
```

**响应：**
```json
{
  "success": true,
  "model": {
    "model_id": "qwen-plus",
    "display_name": "通义千问Plus",
    "model": "qwen-plus",
    "api_key": "sk-xxx",
    "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1",
    "enabled": true,
    "description": "阿里云大模型",
    "max_tokens": 2000,
    "temperature": 0.7
  }
}
```

---

### 3. 添加新模型

**请求：**
```http
POST http://localhost:8000/api/models
Content-Type: application/json

{
  "model_id": "gpt-4",
  "display_name": "GPT-4",
  "api_key": "sk-xxxxxxxxxxxxxxxx",
  "base_url": "https://api.openai.com/v1",
  "model_type": "openai",
  "max_tokens": 8000,
  "temperature": 0.7,
  "enabled": true
}
```

**响应：**
```json
{
  "success": true,
  "message": "模型添加成功"
}
```

**字段说明：**
- `model_id` (必需): 模型唯一标识
- `display_name` (必需): 显示名称
- `api_key` (必需): API密钥
- `base_url` (可选): API基础URL
- `model_type` (可选): 模型类型，默认 "openai"
- `max_tokens` (可选): 最大token数
- `temperature` (可选): 温度参数 0-1
- `enabled` (可选): 是否启用，默认 `true`

---

### 4. 更新模型配置

**请求：**
```http
PUT http://localhost:8000/api/models/gpt-4
Content-Type: application/json

{
  "api_key": "sk-new-key",
  "enabled": false,
  "max_tokens": 16000
}
```

**响应：**
```json
{
  "success": true,
  "message": "模型更新成功"
}
```

**说明：** 只需提供要更新的字段

---

### 5. 删除模型

**请求：**
```http
DELETE http://localhost:8000/api/models/gpt-4
```

**响应：**
```json
{
  "success": true,
  "message": "模型删除成功"
}
```

---

## 🔌 插件管理接口

### 1. 获取插件列表

**请求：**
```http
GET http://localhost:8000/api/plugins?enabled_only=true
```

**响应：**
```json
{
  "success": true,
  "plugins": [
    {
      "plugin_id": "uuid-xxx",
      "plugin_name": "weather_query",
      "description": "查询天气信息",
      "enabled": true,
      "auth_type": "none",
      "openapi_spec": { ... }
    }
  ]
}
```

---

### 2. 添加插件

**请求：**
```http
POST http://localhost:8000/api/plugins
Content-Type: application/json

{
  "plugin_name": "weather_query",
  "description": "查询城市天气",
  "auth_type": "none",
  "openapi_spec": {
    "openapi": "3.0.0",
    "info": {
      "title": "天气API",
      "version": "1.0.0"
    },
    "servers": [
      {"url": "http://wttr.in"}
    ],
    "paths": {
      "/{city}": {
        "get": {
          "operationId": "weather_query",
          "summary": "查询天气",
          "parameters": [
            {
              "name": "city",
              "in": "path",
              "required": true,
              "schema": {"type": "string"}
            }
          ]
        }
      }
    }
  }
}
```

**响应：**
```json
{
  "success": true,
  "plugin_id": "uuid-xxx",
  "plugin_name": "weather_query",
  "tools_count": 1,
  "tools": [
    {
      "name": "weather_query",
      "description": "查询天气",
      "method": "GET",
      "path": "/{city}"
    }
  ],
  "required_config": {
    "auth_type": "none",
    "required_params": [],
    "optional_params": [],
    "needs_config": false
  },
  "message": "插件添加成功！此插件无需配置参数，可直接调用。"
}
```

---

### 3. 更新/删除插件

**更新：**
```http
PUT http://localhost:8000/api/plugins/{plugin_id}
Content-Type: application/json

{
  "enabled": false
}
```

**删除：**
```http
DELETE http://localhost:8000/api/plugins/{plugin_id}
```

---

## 💬 对话接口

### 基础对话

**请求：**
```http
POST http://localhost:8000/api/chat
Content-Type: application/json

{
  "message": "你好",
  "modelId": "qwen-plus"
}
```

**响应：**
```json
{
  "question": "你好",
  "answer": "你好！有什么可以帮助你的吗？",
  "success": true,
  "model": "qwen-plus",
  "metadata": {},
  "timestamp": "2025-12-02T00:00:00"
}
```

---

### 带插件的对话（无需配置）

**请求：**
```http
POST http://localhost:8000/api/chat
Content-Type: application/json

{
  "message": "北京今天天气怎么样？",
  "modelId": "qwen-plus",
  "pluginNames": ["weather_query"]
}
```

---

### 带插件的对话（需要配置）

**请求：**
```http
POST http://localhost:8000/api/chat
Content-Type: application/json

{
  "message": "查询我的订单",
  "modelId": "qwen-plus",
  "pluginNames": ["user_api"],
  "pluginParams": {
    "user_api": {
      "token": "user_temp_token_12345",
      "user_id": "user123"
    }
  }
}
```

**说明：** `pluginParams` 根据插件的 `required_config` 提供

---

## 🧪 测试示例

### Python 测试脚本

```python
import requests

BASE_URL = "http://localhost:8000"

# 1. 获取模型列表
response = requests.get(f"{BASE_URL}/api/models")
models = response.json()["models"]
print("可用模型：", [m["model_id"] for m in models])

# 2. 添加新模型
new_model = {
    "model_id": "custom-model",
    "display_name": "自定义模型",
    "api_key": "sk-xxx",
    "base_url": "https://api.example.com/v1",
    "model_type": "custom",
    "enabled": True
}
response = requests.post(f"{BASE_URL}/api/models", json=new_model)
print(response.json())

# 3. 使用模型对话
chat_request = {
    "message": "你好",
    "modelId": "custom-model"
}
response = requests.post(f"{BASE_URL}/api/chat", json=chat_request)
print(response.json()["answer"])
```

---

## ✅ 完整的 API 列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/models` | 获取模型列表 |
| GET | `/api/models/{id}` | 获取单个模型 |
| POST | `/api/models` | 添加模型 |
| PUT | `/api/models/{id}` | 更新模型 |
| DELETE | `/api/models/{id}` | 删除模型 |
| GET | `/api/plugins` | 获取插件列表 |
| POST | `/api/plugins` | 添加插件 |
| PUT | `/api/plugins/{id}` | 更新插件 |
| DELETE | `/api/plugins/{id}` | 删除插件 |
| POST | `/api/chat` | 对话接口 |
| POST | `/api/rag` | RAG检索 |
| POST | `/api/batch` | 批量处理 |
| GET | `/health` | 健康检查 |

---

## 🚀 快速开始

1. **启动服务器**
```bash
python main.py
```

2. **查看 API 文档**
```
http://localhost:8000/docs
```

3. **测试接口**
```bash
# 获取模型列表
curl http://localhost:8000/api/models

# 添加模型
curl -X POST http://localhost:8000/api/models \
  -H "Content-Type: application/json" \
  -d '{
    "model_id": "test-model",
    "display_name": "测试模型",
    "api_key": "sk-xxx"
  }'
```

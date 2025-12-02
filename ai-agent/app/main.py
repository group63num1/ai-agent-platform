"""
FastAPI应用 - Python端API服务器
接收来自Java后端的HTTP请求，处理后返回响应
"""

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from typing import Dict, Any, Optional, List
from datetime import datetime
import logging

from app.models import (
    ChatRequest,
    ChatResponse,
    RAGRequest,
    RAGResponse,
    ModelCreateRequest,
    ModelUpdateRequest,
    PluginCreateRequest,
    PluginUpdateRequest,
    PluginSyncRequest,
)
from core.model_registry import get_model_registry, ModelInfo
from core import agent_service as agent_svc
import config

# 配置日志
logging.basicConfig(
    level=getattr(logging, config.LOG_LEVEL),
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)

# 获取模型注册表实例
model_registry = get_model_registry()


# ==================== 生命周期管理 ====================

from contextlib import asynccontextmanager


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时
    try:
        logger.info("🚀 正在启动应用...")

        # 1. 初始化数据库
        from core.database import init_database, seed_default_models

        init_database()
        logger.info("✅ 数据库连接成功")

        # 2. 初始化默认模型（如果需要）
        seed_default_models()

        # 3. 从数据库同步模型到内存
        model_registry.sync_from_database()
        logger.info(f"✅ 已加载 {len(model_registry.list())} 个模型")

        # 4. 从数据库同步插件到内存
        from core.plugin_registry import get_plugin_registry

        plugin_registry = get_plugin_registry()
        plugin_registry.sync_from_database()
        logger.info(f"✅ 已加载 {len(plugin_registry.list())} 个插件")

        logger.info("✅ 应用启动完成")

        yield

    except Exception as e:
        logger.error(f"❌ 应用启动失败: {e}")
        raise

    # 关闭时
    logger.info("👋 应用正在关闭...")


# 创建FastAPI应用
app = FastAPI(
    title="AI Agent Python API",
    description="Python端AI Agent API服务，与Java后端通信",
    version="1.0.0",
    lifespan=lifespan,
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=config.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== 生命周期事件 ====================


# ==================== API端点 ====================
@app.get("/")
async def root():
    """根路径 - API信息"""
    return {
        "service": "AI Agent Python API",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "chat": "/api/chat",
            "rag": "/api/rag",
            "models": "/api/models",
            "plugins_sync": "/api/plugins/sync",
            "plugins": "/api/plugins",
            "batch": "/api/batch",
            "health": "/health",
        },
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    try:
        from core.database import get_session

        # 测试数据库连接
        with get_session() as session:
            session.execute("SELECT 1")
        db_status = "connected"
    except Exception as e:
        logger.error(f"数据库连接检查失败: {e}")
        db_status = "disconnected"

    return {
        "status": "healthy",
        "database": db_status,
        "timestamp": datetime.now().isoformat(),
    }


# ==================== 对话接口 ====================


@app.post("/api/chat")
async def chat(request: ChatRequest):
    """
    对话接口 - 纯粹的 AI 推理服务

    请求体:
    {
        "message": "今天天气怎么样？",
        "modelId": "qwen-plus",
        "systemPrompt": "你是一个专业的助手",
        "pluginNames": ["weather_query"],
        "pluginParams": {
            "weather_query": {
                "user_id": "user123",
                "token": "temp_token_xxx"
            }
        },
        "enableRag": true,
        "history": [{"role": "user", "content": "..."}]
    }

    响应:
    {
        "question": "今天天气怎么样？",
        "answer": "AI回答...",
        "success": true,
        "model": "qwen-plus",
        "metadata": {...}
    }
    """
    try:
        logger.info(
            f"收到Chat请求: model={request.modelId}, message={request.message[:50]}"
        )

        # 如果有动态插件JSON，先注册
        if request.pluginsJson:
            agent_svc.register_plugins_json(request.pluginsJson)

        # 调用Agent服务
        result = agent_svc.chat(
            request.message,
            session_id="default",  # 无状态服务
            model=request.modelId,
            system_prompt=request.systemPrompt,
            history=request.history,
            enable_rag=request.enableRag,
            enable_plugins=bool(request.pluginNames),
            allowed_plugins=request.pluginNames,
            plugin_params=request.pluginParams,  # 传递运行时参数
        )

        # 构造响应
        response = ChatResponse(
            question=request.message,
            answer=result["reply"],
            success=True,
            model=request.modelId,
            metadata={
                "history_length": result.get("history_length", 0),
                "has_summary": result.get("has_summary", False),
            },
        )

        return response.dict()

    except Exception as e:
        logger.error(f"Chat接口错误: {str(e)}")
        return ChatResponse(
            question=request.message,
            answer="",
            success=False,
            error=str(e),
            model=request.modelId,
        ).dict()


# ==================== RAG接口 ====================


@app.post("/api/rag")
async def rag_search(request: RAGRequest):
    """
    RAG检索接口 - 在知识库中检索相关信息

    请求体:
    {
        "query": "什么是深度学习？",
        "topK": 5
    }

    响应:
    {
        "query": "什么是深度学习？",
        "results": [...],
        "success": true
    }
    """
    try:
        logger.info(f"收到RAG请求: query={request.query[:50]}")

        # 调用RAG服务
        result = agent_svc.rag_search(request.query, "default", k=request.topK)

        # 构造响应
        response = RAGResponse(
            query=request.query,
            results=result.get("results", []),
            success=True,
        )

        return response.dict()

    except Exception as e:
        logger.error(f"RAG接口错误: {str(e)}")
        return RAGResponse(
            query=request.query,
            results=[],
            success=False,
            error=str(e),
        ).dict()


# ==================== 模型管理 ====================


@app.get("/api/models")
async def list_models(enabled_only: bool = False):
    """
    获取所有模型列表

    参数:
    - enabled_only: 是否只返回启用的模型

    响应:
    {
        "models": [
            {
                "model_id": "qwen-plus",
                "display_name": "通义千问Plus",
                "model": "qwen-plus",
                "api_key": "sk-xxx",
                "base_url": "https://...",
                "enabled": true,
                ...
            }
        ]
    }
    """
    try:
        models = model_registry.list(enabled_only=enabled_only)
        return {"success": True, "models": models}
    except Exception as e:
        logger.error(f"获取模型列表失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/models/{model_id}")
async def get_model(model_id: str):
    """
    获取指定模型详情

    响应:
    {
        "success": true,
        "model": {...}
    }
    """
    try:
        model_info = model_registry.get(model_id)
        if not model_info:
            raise HTTPException(status_code=404, detail=f"模型不存在: {model_id}")

        from dataclasses import asdict

        return {"success": True, "model": asdict(model_info)}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取模型失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/models")
async def add_model(req: ModelCreateRequest):
    """
    添加新模型

    请求体:
    {
        "model_id": "gpt-4",
        "display_name": "GPT-4",
        "api_key": "sk-xxx",
        "base_url": "https://api.openai.com/v1",
        "model_type": "openai",
        "enabled": true
    }

    响应:
    {
        "success": true,
        "message": "模型添加成功"
    }
    """
    try:
        from core.database import create_model

        # 写入数据库
        create_model(
            model_id=req.model_id,
            display_name=req.display_name,
            model=req.model_id,
            api_key=req.api_key,
            base_url=req.base_url,
            enabled=req.enabled,
            description=req.model_type,
            max_tokens=req.max_tokens,
            temperature=str(req.temperature) if req.temperature else None,
        )

        # 同步到内存
        model_registry.sync_from_database()

        logger.info(f"✅ 添加模型成功: {req.model_id}")
        return {"success": True, "message": "模型添加成功"}

    except Exception as e:
        logger.error(f"添加模型失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.put("/api/models/{model_id}")
async def update_model(model_id: str, req: ModelUpdateRequest):
    """
    更新模型配置

    请求体: 需要更新的字段（可选）
    {
        "api_key": "new-key",
        "enabled": false
    }

    响应:
    {
        "success": true,
        "message": "模型更新成功"
    }
    """
    try:
        from core.database import update_model as db_update_model

        # 构造更新参数（只更新非None的字段）
        update_data = {k: v for k, v in req.dict().items() if v is not None}

        if not update_data:
            raise HTTPException(status_code=400, detail="没有提供要更新的字段")

        # 更新数据库
        result = db_update_model(model_id, **update_data)
        if not result:
            raise HTTPException(status_code=404, detail=f"模型不存在: {model_id}")

        # 同步到内存
        model_registry.sync_from_database()

        logger.info(f"✅ 更新模型成功: {model_id}")
        return {"success": True, "message": "模型更新成功"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新模型失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/models/{model_id}")
async def delete_model(model_id: str):
    """
    删除模型

    响应:
    {
        "success": true,
        "message": "模型删除成功"
    }
    """
    try:
        from core.database import delete_model as db_delete_model

        # 从数据库删除
        ok = db_delete_model(model_id)
        if not ok:
            raise HTTPException(status_code=404, detail=f"模型不存在: {model_id}")

        # 从内存移除
        model_registry.remove(model_id)

        logger.info(f"✅ 删除模型成功: {model_id}")
        return {"success": True, "message": "模型删除成功"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除模型失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ==================== 插件管理 ====================


@app.post("/api/plugins")
async def add_plugin(req: PluginCreateRequest):
    """
    添加插件 (OpenAPI 3.0 规范)

    请求体:
    {
        "plugin_name": "weather_query",
        "description": "查询天气插件",
        "openapi_spec": { OpenAPI 3.0 完整规范 },
        "auth_type": "none",       # "none", "bearer", "apikey"
        "auth_config": {"token": "xxx"}  # 认证配置
    }

    响应:
    {
        "success": true,
        "plugin_id": "uuid-xxx"
    }
    """
    try:
        from core.database import create_plugin
        import uuid

        # 验证 OpenAPI 规范
        if "openapi" not in req.openapi_spec:
            raise HTTPException(
                status_code=400, detail="无效的 OpenAPI 规范: 缺少 'openapi' 字段"
            )

        # 生成插件ID
        plugin_id = str(uuid.uuid4())

        # 写入数据库
        create_plugin(
            plugin_id=plugin_id,
            plugin_name=req.plugin_name,
            description=req.description,
            openapi_spec=req.openapi_spec,
            enabled=True,
            auth_type=req.auth_type,
            auth_config=req.auth_config,
        )

        # 提取插件需要的配置参数
        from core.plugins import extract_required_config

        required_config = extract_required_config(
            openapi_spec=req.openapi_spec,
            auth_type=req.auth_type,
        )

        # 预解析工具信息（不实际创建工具，只是获取元数据）
        tools_info = []
        paths = req.openapi_spec.get("paths", {})
        for path, path_item in paths.items():
            for method, operation in path_item.items():
                if method.lower() not in ["get", "post", "put", "delete", "patch"]:
                    continue
                operation_id = operation.get("operationId", f"{method}_{path}")
                summary = operation.get("summary", "")
                tools_info.append(
                    {
                        "name": operation_id,
                        "description": summary,
                        "method": method.upper(),
                        "path": path,
                    }
                )

        logger.info(
            f"✅ 添加插件成功: {req.plugin_name} ({plugin_id}), 包含 {len(tools_info)} 个工具"
            + (
                f", 需要配置参数"
                if required_config.get("needs_config")
                else ", 无需配置"
            )
        )

        return {
            "success": True,
            "plugin_id": plugin_id,
            "plugin_name": req.plugin_name,
            "tools_count": len(tools_info),
            "tools": tools_info,
            "required_config": required_config,  # 返回需要的配置参数
            "message": (
                f"插件添加成功！调用时需要传递参数: {[p['name'] for p in required_config['required_params']]}"
                if required_config.get("needs_config")
                else "插件添加成功！此插件无需配置参数，可直接调用。"
            ),
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"添加插件失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/plugins")
async def list_plugins_api(
    enabled_only: bool = False,
):
    """
    获取插件列表

    参数:
    - enabled_only: 只返回启用的插件

    响应:
    {
        "success": true,
        "plugins": [
            {
                "plugin_id": "uuid-xxx",
                "plugin_name": "weather_query",
                "description": "查询天气",
                "enabled": true,
                ...
            }
        ]
    }
    """
    try:
        from core.database import list_plugins as db_list_plugins

        plugins = db_list_plugins(enabled_only=enabled_only)
        # list_plugins 现在直接返回字典列表
        return {"success": True, "plugins": plugins}

    except Exception as e:
        logger.error(f"获取插件列表失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/plugins/{plugin_id}")
async def get_plugin_api(plugin_id: str):
    """
    获取插件详情

    响应:
    {
        "success": true,
        "plugin": {
            "plugin_id": "uuid-xxx",
            "plugin_name": "weather_query",
            "openapi_spec": { ... },
            ...
        }
    }
    """
    try:
        from core.database import get_plugin

        plugin = get_plugin(plugin_id)
        if not plugin:
            raise HTTPException(status_code=404, detail=f"插件不存在: {plugin_id}")

        return {"success": True, "plugin": plugin}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取插件失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.put("/api/plugins/{plugin_id}")
async def update_plugin_api(plugin_id: str, req: PluginUpdateRequest):
    """
    更新插件

    请求体: 需要更新的字段（可选）
    {
        "plugin_name": "new_name",
        "description": "新描述",
        "enabled": false
    }

    响应:
    {
        "success": true,
        "message": "插件更新成功"
    }
    """
    try:
        from core.database import update_plugin as db_update_plugin

        # 构造更新参数（只更新非None的字段）
        update_data = {k: v for k, v in req.dict().items() if v is not None}

        if not update_data:
            raise HTTPException(status_code=400, detail="没有提供要更新的字段")

        # 更新数据库
        result = db_update_plugin(plugin_id, **update_data)
        if not result:
            raise HTTPException(status_code=404, detail=f"插件不存在: {plugin_id}")

        logger.info(f"✅ 更新插件成功: {plugin_id}")
        return {"success": True, "message": "插件更新成功"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新插件失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/plugins/{plugin_id}")
async def delete_plugin_api(plugin_id: str):
    """
    删除插件

    响应:
    {
        "success": true,
        "message": "插件删除成功"
    }
    """
    try:
        from core.database import delete_plugin as db_delete_plugin

        ok = db_delete_plugin(plugin_id)
        if not ok:
            raise HTTPException(status_code=404, detail=f"插件不存在: {plugin_id}")

        logger.info(f"✅ 删除插件成功: {plugin_id}")
        return {"success": True, "message": "插件删除成功"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除插件失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ==================== 批量处理 ====================


@app.post("/api/batch")
async def batch_process(requests: List[ChatRequest]):
    """
    批量处理接口 - 批量处理多个对话请求

    请求体:
    {
        "requests": [request1, request2, ...]
    }

    响应:
    {
        "results": [response1, response2, ...],
        "total": 2,
        "success_count": 2,
        "failure_count": 0
    }
    """
    try:
        logger.info(f"收到批量请求: 数量={len(requests)}")

        responses = []
        success_count = 0
        failure_count = 0

        for req in requests:
            try:
                # 如果有动态插件JSON，先注册
                if req.pluginsJson:
                    agent_svc.register_plugins_json(req.pluginsJson)

                # 调用Agent服务
                result = agent_svc.chat(
                    req.message,
                    session_id="default",
                    model=req.modelId,
                    system_prompt=req.systemPrompt,
                    history=req.history,
                    enable_rag=req.enableRag,
                    enable_plugins=bool(req.pluginNames),
                    allowed_plugins=req.pluginNames,
                )

                response = ChatResponse(
                    question=req.message,
                    answer=result["reply"],
                    success=True,
                    model=req.modelId,
                    metadata={
                        "history_length": result.get("history_length", 0),
                    },
                )
                success_count += 1

            except Exception as e:
                logger.error(f"批量处理单个请求失败: {str(e)}")
                response = ChatResponse(
                    question=req.message,
                    answer="",
                    success=False,
                    error=str(e),
                    model=req.modelId,
                )
                failure_count += 1

            responses.append(response.dict())

        return {
            "results": responses,
            "total": len(requests),
            "success_count": success_count,
            "failure_count": failure_count,
        }

    except Exception as e:
        logger.error(f"批量处理错误: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


# ==================== 异常处理 ====================


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """全局异常处理"""
    logger.error(f"未处理的异常: {str(exc)}")
    return JSONResponse(
        status_code=500,
        content={"success": False, "error": str(exc), "message": "服务器内部错误"},
    )

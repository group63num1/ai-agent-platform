"""
FastAPI应用 - 简化版，只保留9个核心接口
1个chat接口 + 4个tool接口 + 4个知识库接口
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from typing import List, Optional
import logging
import uuid
import json
from datetime import datetime

from app.models import (
    ChatRequest,
    ChatResponse,
    ToolCreateRequest,
    ToolUpdateRequest,
    KnowledgeBaseCreateRequest,
    KnowledgeBaseUpdateRequest,
    KnowledgeBaseQueryRequest,
)
from core import agent_service as agent_svc
from core.model_registry import get_model_registry
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
    try:
        logger.info("🚀 正在启动应用...")

        # 初始化数据库
        from core.database import init_database

        init_database()
        logger.info("✅ 数据库连接成功")

        # 从数据库同步模型到内存
        model_registry.sync_from_database()
        logger.info(f"✅ 已加载 {len(model_registry.list())} 个模型")

        logger.info("✅ 应用启动完成")

        yield

    except Exception as e:
        logger.error(f"❌ 应用启动失败: {e}")
        raise

    logger.info("👋 应用正在关闭...")


# 创建FastAPI应用
app = FastAPI(
    title="AI Agent API - 简化版",
    description="9个核心接口：1个chat + 4个tool + 4个knowledge_base",
    version="2.0.0",
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


# ==================== 1. Chat 接口 ====================


@app.get("/api/models")
async def list_models_api():
    """返回模型库中的全部模型名称及基本信息"""
    try:
        from core.database import list_models

        items = list_models(enabled_only=False)
        names = [m.get("display_name") or m.get("model_id") for m in items]
        return {
            "success": True,
            "count": len(items),
            "models": names,
            "items": items,
        }
    except Exception as e:
        logger.error(f"列出模型失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/chat")
async def chat(request: ChatRequest):
    """
    对话接口 - 核心接口（流式输出）
    支持指定 tools 和 knowledgeBases
    """

    async def generate():
        try:
            logger.info(f"开始流式对话: message={request.message[:50]}...")
            chunk_count = 0

            # 调用 agent_service（流式）
            for chunk in agent_svc.chat_stream(
                message=request.message,
                session_id=request.session_id,
                model=request.model_id,
                tools=request.tools or [],
                knowledge_bases=request.knowledge_bases or [],
                system_prompt=request.system_prompt,
                history=request.history,
            ):
                chunk_count += 1
                chunk_content = chunk.get("content", "")
                logger.debug(
                    f"生成 chunk #{chunk_count}: content_len={len(chunk_content)}, done={chunk.get('done')}"
                )
                # SSE 格式
                yield f"data: {json.dumps(chunk, ensure_ascii=False)}\n\n"

            # 结束标记
            logger.info(f"流式对话完成，共生成 {chunk_count} 个 chunk")
            yield "data: [DONE]\n\n"

        except Exception as e:
            logger.error(f"Chat流式输出失败: {e}", exc_info=True)
            error_data = {"error": str(e), "success": False}
            yield f"data: {json.dumps(error_data, ensure_ascii=False)}\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")


# ==================== 2-5. Tool 接口 ====================


@app.post("/api/tools")
async def create_tool(body: dict):
    """
    创建工具 - 接收 user_id 和 OpenAPI JSON

    请求体格式:
    {
        "user_id": "user123",
        "openapi": { ... OpenAPI JSON文件 ... }
    }
    """
    try:
        if not isinstance(body, dict):
            raise HTTPException(status_code=400, detail="请求体必须是JSON对象")

        user_id = body.get("user_id")
        openapi_json = body.get("openapi")

        if not user_id:
            raise HTTPException(status_code=400, detail="缺少 user_id 字段")
        if not openapi_json:
            raise HTTPException(status_code=400, detail="缺少 openapi 字段")

        from core.database import (
            create_tool,
            parse_openapi_to_tool_fields,
            delete_tool,
            list_tools,
        )

        # 解析 OpenAPI JSON
        tool_fields_list = parse_openapi_to_tool_fields(openapi_json)

        if not tool_fields_list:
            raise HTTPException(
                status_code=400, detail="OpenAPI 中未找到有效的路径定义"
            )

        # 只删除即将创建的同名工具（避免重复 key）
        from core.database import list_tools, delete_tool

        existing_tools = list_tools()
        new_tool_ids = [f"{user_id}_{tf['operation_id']}" for tf in tool_fields_list]

        for tool in existing_tools:
            if tool["tool_id"] in new_tool_ids:
                delete_tool(tool["tool_id"])
                logger.info(f"删除旧工具: {tool['tool_id']}")

        # 为每个端点创建工具
        created_tools = []
        for tool_fields in tool_fields_list:
            # tool_id = user_id + 工具英文名（operationId 本身就是唯一的）
            tool_id = f"{user_id}_{tool_fields['operation_id']}"

            tool = create_tool(
                tool_id=tool_id,
                name=tool_fields["name"],
                purpose=tool_fields["purpose"],
                version=tool_fields["version"],
                call_method=tool_fields["call_method"],
                parameters=tool_fields["parameters"],
                user_settings={},  # 初始为空，用户可以通过更新接口设置参数值
            )
            created_tools.append({"tool_id": tool_id, "name": tool_fields["name"]})

        logger.info(f"✅ 为用户 {user_id} 创建了 {len(created_tools)} 个工具")
        return {
            "success": True,
            "tools": created_tools,
            "message": f"成功创建 {len(created_tools)} 个工具",
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"创建工具失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.put("/api/tools/{tool_id}")
async def update_tool(tool_id: str, body: dict):
    """
    更新工具 - 接收 tool_id 和要修改的字段

    请求体格式:
    {
        "name": "新名称",  // 可选
        "purpose": "新用途",  // 可选
        "version": "1.0.1",  // 可选
        ... 其他字段
    }
    """
    try:
        if not isinstance(body, dict):
            raise HTTPException(status_code=400, detail="请求体必须是JSON对象")

        from core.database import update_tool as db_update_tool, get_tool

        # 检查工具是否存在
        tool = get_tool(tool_id)
        if not tool:
            raise HTTPException(status_code=404, detail=f"工具不存在: {tool_id}")

        # 只允许更新特定字段
        allowed_fields = {
            "name",
            "purpose",
            "version",
            "call_method",
            "parameters",
            "user_settings",
        }

        update_data = {
            k: v for k, v in body.items() if k in allowed_fields and v is not None
        }

        if not update_data:
            raise HTTPException(status_code=400, detail="没有提供要更新的字段")

        result = db_update_tool(tool_id, **update_data)
        if not result:
            raise HTTPException(status_code=404, detail=f"更新失败: {tool_id}")

        logger.info(f"✅ 更新工具: {tool_id}")
        return {"success": True, "message": "工具更新成功"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新工具失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/tools/{tool_id}")
async def delete_tool(tool_id: str):
    """
    删除工具 - 根据 tool_id 删除
    """
    try:
        from core.database import delete_tool as db_delete_tool

        ok = db_delete_tool(tool_id)
        if not ok:
            raise HTTPException(status_code=404, detail=f"工具不存在: {tool_id}")

        logger.info(f"✅ 删除工具: {tool_id}")
        return {"success": True, "message": "工具删除成功"}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除工具失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ==================== 6-9. KnowledgeBase 接口 ====================


@app.post("/api/knowledge-bases")
async def create_knowledge_base_api(req: KnowledgeBaseCreateRequest):
    """
    创建知识库

    - 接收文件列表、chunking配置等参数
    - 自动向量化并存储到 Milvus
    - 返回知识库信息
    """
    try:
        from core.knowledge_service import get_kb_service

        kb_service = get_kb_service()

        if not req.user_id or not str(req.user_id).strip():
            raise HTTPException(status_code=400, detail="user_id 不能为空")

        result = kb_service.create_knowledge_base(
            user_id=req.user_id,
            name=req.name,
            files=req.files,
            description=req.description or "",
            chunking_method=req.chunking_method or "recursive",
            chunk_size=req.chunk_size or 1000,
            chunk_overlap=req.chunk_overlap or 200,
            enabled=req.enabled if req.enabled is not None else True,
        )

        if result["success"]:
            logger.info(f"✅ 创建知识库成功: {req.name} ({result['kb_id']})")
            return result
        else:
            logger.error(f"❌ 创建知识库失败: {result.get('error')}")
            raise HTTPException(status_code=500, detail=result.get("error"))

    except Exception as e:
        logger.error(f"创建知识库失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/knowledge-bases/{kb_id}/query")
async def query_knowledge_bases(kb_id: str, req: KnowledgeBaseQueryRequest):
    """
    查询知识库内容

    - kb_id: 知识库ID（路径参数）
    - 若提供 query_text + similarity_threshold:
      * similarity_threshold 存在且有匹配: 返回相似度 >= threshold 的前5条（包含 similarity 字段）
      * 无匹配或未提供 threshold: 返回前20条
    - 否则: 返回该知识库的前 20 个 chunk
    """
    try:
        from core.knowledge_service import get_kb_service
        from core.database import get_knowledge_base

        # 验证知识库是否存在
        kb_info = get_knowledge_base(kb_id)
        if not kb_info:
            raise HTTPException(status_code=404, detail=f"知识库不存在: {kb_id}")

        kb_service = get_kb_service()

        # 1. 如果有 query（必带阈值），先取 top5，再按阈值过滤，返回满足的（最多5条）
        if req.query_text:
            threshold = (
                req.similarity_threshold
                if req.similarity_threshold is not None
                else 0.0
            )

            results = kb_service.search_similar_content(
                query_text=req.query_text,
                kb_id=kb_id,
                limit=5,
                similarity_threshold=threshold,
            )

            for result in results:
                if "similarity" not in result and "similarity_score" in result:
                    result["similarity"] = result.get("similarity_score")

            return {
                "success": True,
                "count": len(results),
                "results": results,
                "query_text": req.query_text,
                "similarity_threshold": threshold,
                "kb_id": kb_id,
            }

        # 2. 无 query：返回该知识库的前 20 个 chunk（按内部顺序）
        top_chunks = kb_service.get_top_chunks(kb_id=kb_id, limit=req.limit or 20)

        return {
            "success": True,
            "count": len(top_chunks),
            "results": top_chunks,
            "query_text": None,
            "kb_id": kb_id,
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"查询知识库失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.put("/api/knowledge-bases/{kb_id}")
async def update_knowledge_base(kb_id: str, req: KnowledgeBaseUpdateRequest):
    """
    更新知识库

    - 支持更新名称、描述、启用状态、chunking配置等
    - 若更新了 files、chunking_method、chunk_size、chunk_overlap：
      * 重新向量化整个知识库
      * 删除旧的 Milvus 集合
      * 创建新的向量存储
    - 否则只更新数据库字段
    """
    try:
        from core.knowledge_service import get_kb_service
        from core.database import get_knowledge_base

        kb_service = get_kb_service()

        # 检查知识库是否存在
        kb_info = get_knowledge_base(kb_id)
        if not kb_info:
            raise HTTPException(status_code=404, detail=f"知识库不存在: {kb_id}")

        # 准备更新数据
        update_data = {k: v for k, v in req.dict().items() if v is not None}
        if not update_data:
            raise HTTPException(status_code=400, detail="没有提供要更新的字段")

        # 判断是否需要重新向量化
        need_rebuild = any(
            key in update_data
            for key in ["files", "chunking_method", "chunk_size", "chunk_overlap"]
        )

        if need_rebuild:
            # 重新构建向量库
            logger.info(f"🔄 检测到需要重新向量化的字段，开始重建知识库: {kb_id}")
            result = kb_service.rebuild_knowledge_base(
                kb_id=kb_id,
                files=update_data.get("files"),
                chunking_method=update_data.get("chunking_method"),
                chunk_size=update_data.get("chunk_size"),
                chunk_overlap=update_data.get("chunk_overlap"),
                name=update_data.get("name"),
                description=update_data.get("description"),
                enabled=update_data.get("enabled"),
            )

            if not result["success"]:
                raise HTTPException(status_code=500, detail=result.get("error"))

            logger.info(f"✅ 知识库重建成功: {kb_id}")
            return result
        else:
            # 只更新元数据
            from core.database import update_knowledge_base as db_update_kb

            result = db_update_kb(kb_id, **update_data)
            if not result:
                raise HTTPException(status_code=404, detail=f"知识库不存在: {kb_id}")

            logger.info(f"✅ 更新知识库元数据: {kb_id}")
            return {"success": True, "message": "知识库更新成功", "kb_id": kb_id}

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新知识库失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/knowledge-bases/{kb_id}")
async def delete_knowledge_base(kb_id: str):
    """
    删除知识库

    - 同时删除向量数据库（Milvus集合）和关系数据库记录
    - 返回 success 状态
    """
    try:
        from core.knowledge_service import get_kb_service

        kb_service = get_kb_service()

        # 删除知识库（包括 Milvus 集合和数据库记录）
        result = kb_service.delete_knowledge_base(kb_id)

        if not result["success"]:
            if "不存在" in result.get("error", ""):
                raise HTTPException(status_code=404, detail=result.get("error"))
            else:
                raise HTTPException(status_code=500, detail=result.get("error"))

        logger.info(f"✅ 删除知识库成功: {kb_id}")
        return result

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除知识库失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# ==================== 健康检查 ====================


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "ok", "timestamp": datetime.now().isoformat()}

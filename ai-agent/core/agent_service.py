"""Session-based Agent service wrapper.
Provides per-session conversation handling with context memory.
"""

from typing import Dict, Optional, List, Any
import config
from core.agent import LangChainAgent
from core.model_registry import get_model_registry
import logging

logger = logging.getLogger(__name__)

# Session -> Agent mapping (each session keeps its own memory)
_session_agents: Dict[str, LangChainAgent] = {}
# Track per-session model selection (model_id -> session)
_session_models: Dict[str, str] = {}

DEFAULT_SESSION = "default"


def _get_agent(
    session_id: Optional[str], model_id: Optional[str] = None
) -> LangChainAgent:
    """获取或创建 session 的 agent,使用数据库中的模型配置"""
    sid = session_id or DEFAULT_SESSION
    registry = get_model_registry()

    # Update selected model for session if provided
    if model_id:
        _session_models[sid] = model_id

    # 获取当前 session 使用的模型 ID（如果没有指定，使用第一个可用模型）
    selected_model_id = _session_models.get(sid)
    if not selected_model_id:
        models = registry.list(enabled_only=True)
        if not models:
            raise RuntimeError("没有可用的模型，请先通过 /api/models 添加模型")
        selected_model_id = models[0]["model_id"]
        _session_models[sid] = selected_model_id

    # 从注册中心获取模型信息
    model_info = registry.get(selected_model_id)
    if not model_info:
        raise ValueError(f"模型不存在: {selected_model_id}")

    # 自动启用模型（如果未启用）
    if not model_info.enabled:
        logger.info(f"自动启用模型: {selected_model_id}")
        from core.database import get_db, ModelDB

        with get_db() as db:
            model_record = (
                db.query(ModelDB).filter(ModelDB.model_id == selected_model_id).first()
            )
            if model_record:
                model_record.enabled = True
                db.flush()
        # 重新获取模型信息以确保 enabled 状态已更新
        model_info = registry.get(selected_model_id)

    # 创建或更新 agent
    if sid not in _session_agents:
        logger.info(
            f"[DEBUG] 创建 Agent - api_key: {model_info.api_key[:10]}..., base_url: {model_info.base_url}, model: {model_info.model}"
        )
        logger.info(
            f"[DEBUG] ModelInfo 完整内容: max_tokens={model_info.max_tokens}, temperature={model_info.temperature}, timeout={model_info.timeout}"
        )

        _session_agents[sid] = LangChainAgent(
            api_key=model_info.api_key,
            base_url=model_info.base_url,
            model=model_info.model,
        )
        # 将数据库中的生成配置注入到 LLM 实例
        llm = _session_agents[sid].llm
        logger.info(
            f"[DEBUG] 注入前 LLM 属性: max_tokens={getattr(llm, 'max_tokens', 'NOT_SET')}, timeout={getattr(llm, 'timeout', 'NOT_SET')}"
        )

        for attr in [
            "max_tokens",
            "temperature",
            "top_p",
            "top_k",
            "frequency_penalty",
            "presence_penalty",
            "timeout",
        ]:
            val = getattr(model_info, attr, None)
            if val is not None:
                setattr(llm, attr, val)
                logger.info(f"[DEBUG] 注入参数: {attr}={val}")

        logger.info(
            f"[DEBUG] 注入后 LLM 属性: max_tokens={getattr(llm, 'max_tokens', 'NOT_SET')}, timeout={getattr(llm, 'timeout', 'NOT_SET')}"
        )
    else:
        # If model has changed, re-create agent to apply new model
        current_model = getattr(_session_agents[sid].llm, "model", None)
        if model_info.model != current_model:
            logger.info(
                f"Session {sid} 切换模型: {current_model} -> {model_info.model}"
            )
            _session_agents[sid] = LangChainAgent(
                api_key=model_info.api_key,
                base_url=model_info.base_url,
                model=model_info.model,
            )
            # 切换后同样注入生成配置
            llm = _session_agents[sid].llm
            for attr in [
                "max_tokens",
                "temperature",
                "top_p",
                "top_k",
                "frequency_penalty",
                "presence_penalty",
                "timeout",
            ]:
                val = getattr(model_info, attr, None)
                if val is not None:
                    setattr(llm, attr, val)
    return _session_agents[sid]


def chat_stream(
    message: str,
    session_id: Optional[str] = None,
    model: Optional[str] = None,
    system_prompt: Optional[str] = None,
    history: Optional[List[Dict[str, str]]] = None,
    tools: Optional[List[str]] = None,
    knowledge_bases: Optional[List[str]] = None,
):
    """
    流式对话接口 - 逐 token 输出

    Args:
        message: 用户消息
        session_id: 会话ID
        model: 模型ID
        system_prompt: 系统提示词
        history: 对话历史
        tools: 工具ID列表
        knowledge_bases: 知识库ID列表

    Yields:
        Dict: 每次生成的 token 块，格式:
            {"content": "token文本", "done": False}
            最后一个块: {"content": "", "done": True, "metadata": {...}}
    """
    agent = _get_agent(session_id, model_id=model)

    # 处理 tools（从数据库加载）
    enable_tools = tools is not None and len(tools) > 0
    tools_info = None
    if enable_tools:
        logger.info(f"启用 {len(tools)} 个工具: {tools}")
        # 从数据库加载工具信息
        from core.database import get_db, ToolDB

        tools_info = []
        try:
            with get_db() as db:
                for tool_id in tools:
                    tool_record = (
                        db.query(ToolDB).filter(ToolDB.tool_id == tool_id).first()
                    )
                    if tool_record:
                        tools_info.append(
                            {
                                "tool_id": tool_record.tool_id,
                                "name": tool_record.name,
                                "purpose": tool_record.purpose,
                                "version": tool_record.version,
                                "call_method": tool_record.call_method,
                                "parameters": tool_record.parameters,
                                "user_settings": tool_record.user_settings,
                            }
                        )
                    else:
                        logger.warning(f"工具不存在: {tool_id}")
        except Exception as e:
            logger.error(f"加载工具失败: {e}")
            tools_info = None

    # 处理 knowledge_bases（如果提供）
    enable_rag = knowledge_bases is not None and len(knowledge_bases) > 0
    if enable_rag:
        logger.info(f"启用 {len(knowledge_bases)} 个知识库: {knowledge_bases}")
        # 验证知识库是否存在（可选）
        from core.database import get_knowledge_base

        for kb_id in knowledge_bases:
            kb_info = get_knowledge_base(kb_id)
            if not kb_info:
                logger.warning(f"知识库不存在: {kb_id}")

    # 调用 agent 的流式方法，传入 tools_info 和 knowledge_bases 参数
    for chunk in agent.chat_stream(
        message,
        history=history,
        enable_rag=enable_rag,
        knowledge_bases=knowledge_bases,
        enable_plugins=enable_tools,
        allowed_plugins=tools,
        system_prompt=system_prompt,
        tools_info=tools_info,
    ):
        yield chunk


def rag_search(
    query: str,
    kb_ids: Optional[List[str]] = None,
    session_id: Optional[str] = None,
    model: Optional[str] = None,
) -> Dict:
    """
    RAG 搜索接口

    Args:
        query: 查询文本
        kb_ids: 知识库ID列表，若为空则返回错误
        session_id: 会话ID
        model: 模型ID

    Returns:
        检索结果字典
    """
    agent = _get_agent(session_id, model=model)
    if not kb_ids:
        return {
            "session_id": session_id or DEFAULT_SESSION,
            "results": [],
            "error": "请指定知识库ID",
        }
    results = agent.milvus_retriever.retrieve(
        query_text=query, kb_ids=kb_ids, k=10, score_threshold=0.6
    )
    return {
        "session_id": session_id or DEFAULT_SESSION,
        "results": results,
        "model": getattr(agent.llm, "model", None),
    }


def set_model(session_id: Optional[str], model: str) -> Dict:
    """Explicitly set model for a session."""
    sid = session_id or DEFAULT_SESSION
    _session_models[sid] = model

    # 从注册中心获取模型信息
    registry = get_model_registry()
    model_info = registry.get(model)

    if not model_info:
        raise ValueError(f"模型不存在: {model}")

    if not model_info.enabled:
        raise ValueError(f"模型已禁用: {model}")

    # Re-create agent to apply new model immediately
    _session_agents[sid] = LangChainAgent(
        api_key=model_info.api_key,
        base_url=model_info.base_url,
        model=model_info.model,
    )
    return {"session_id": sid, "model": model}


# --------- 插件管理给上层使用 ---------


def register_plugins_json(plugins: List[Dict]) -> Dict[str, Any]:
    """旧的 JSON 插件注册接口（已废弃，建议使用数据库插件）"""
    logger.warning("register_plugins_json 已废弃，请使用数据库插件系统")
    return {"success": False, "message": "此接口已废弃，请使用 /api/plugins 添加插件"}


def list_plugins() -> List[Dict[str, Any]]:
    """列出所有插件（Plugin 表已移除，返回空列表）"""
    logger.warning("Plugin 表已被移除，返回空列表")
    return []


def reload_plugins_from_database():
    """从数据库重新加载插件（Plugin 表已移除）"""
    logger.warning("Plugin 表已被移除，无需重新加载")

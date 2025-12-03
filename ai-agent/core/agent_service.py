"""Session-based Agent service wrapper.
Provides per-session conversation handling with context memory.
"""

from typing import Dict, Optional, List, Any
import config
from core.agent import LangChainAgent, RAGRetriever
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

    if not model_info.enabled:
        raise ValueError(f"模型已禁用: {selected_model_id}")

    # 创建或更新 agent
    if sid not in _session_agents:
        _session_agents[sid] = LangChainAgent(
            api_key=model_info.api_key,
            txt_corpus_path=config.CORPUS_PATH,
            base_url=model_info.base_url,
            model=model_info.model,
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
                txt_corpus_path=config.CORPUS_PATH,
                base_url=model_info.base_url,
                model=model_info.model,
            )
    return _session_agents[sid]


def chat(
    message: str,
    session_id: Optional[str] = None,
    model: Optional[str] = None,
    system_prompt: Optional[str] = None,
    history: Optional[List[Dict[str, str]]] = None,
    enable_rag: bool = False,
    enable_plugins: bool = False,
    allowed_plugins: Optional[List[str]] = None,
    plugin_params: Optional[Dict[str, Dict]] = None,
) -> Dict:
    """
    对话接口

    Args:
        message: 用户消息
        session_id: 会话ID
        model: 模型ID
        system_prompt: 系统提示词
        history: 对话历史
        enable_rag: 是否启用RAG
        enable_plugins: 是否启用插件
        allowed_plugins: 允许的插件名称列表（从数据库加载）
        plugin_params: 插件运行时参数，格式：{'plugin_name': {'user_id': 'xxx', 'token': 'yyy'}}
    """
    agent = _get_agent(session_id, model=model)

    # 如果指定了插件，从插件注册表加载工具（动态创建，传入运行时参数）
    if enable_plugins and allowed_plugins:
        from core.plugin_registry import get_plugin_registry
        from core.plugins import extract_required_config

        plugin_registry = get_plugin_registry()

        # 验证插件配置参数
        for plugin_name in allowed_plugins:
            plugin = plugin_registry.get_by_name(plugin_name)
            if plugin and plugin.auth_type != "none":
                # 检查是否提供了运行时参数
                if not plugin_params or plugin_name not in plugin_params:
                    # 提取需要的配置
                    required_config = extract_required_config(
                        plugin.openapi_spec, plugin.auth_type
                    )
                    required_param_names = [
                        p["name"] for p in required_config["required_params"]
                    ]
                    logger.warning(
                        f"插件 {plugin_name} 需要配置参数 {required_param_names}，但未提供！"
                    )
                    raise ValueError(
                        f"插件 '{plugin_name}' 需要以下配置参数: {required_param_names}。"
                        f"请在 pluginParams 中提供。"
                    )

        # 动态创建工具，传入运行时参数
        tools = plugin_registry.get_tools_for_plugins(
            plugin_names=allowed_plugins, runtime_params=plugin_params or {}
        )

        # 设置到 agent 的 tool_map
        if tools:
            for tool in tools:
                agent.tool_map[tool.name] = tool
            logger.info(f"已加载 {len(tools)} 个插件工具")

    reply = agent.chat(
        message,
        history=history,
        enable_rag=enable_rag,
        enable_plugins=enable_plugins,
        allowed_plugins=allowed_plugins,
    )
    return {
        "session_id": session_id or DEFAULT_SESSION,
        "reply": reply,
        "history_length": len(agent.history),
        "has_summary": agent.summary is not None,
        "model": getattr(agent.llm, "model", None),
    }


def rag_search(
    query: str, session_id: Optional[str] = None, model: Optional[str] = None
) -> Dict:
    agent = _get_agent(session_id, model=model)
    results = agent.rag_retriever.retrieve(query, k=3)
    return {
        "session_id": session_id or DEFAULT_SESSION,
        "results": results,
        "model": getattr(agent.llm, "model", None),
    }


def set_model(session_id: Optional[str], model: str) -> Dict:
    """Explicitly set model for a session."""
    sid = session_id or DEFAULT_SESSION
    _session_models[sid] = model
    # Re-create agent to apply new model immediately
    _session_agents[sid] = LangChainAgent(
        api_key=API_KEY,
        txt_corpus_path=CORPUS_PATH,
        base_url=BASE_URL,
        model=model,
    )
    return {"session_id": sid, "model": model}


# --------- 插件管理给上层使用 ---------


def register_plugins_json(plugins: List[Dict]) -> Dict[str, Any]:
    """旧的 JSON 插件注册接口（已废弃，建议使用数据库插件）"""
    logger.warning("register_plugins_json 已废弃，请使用数据库插件系统")
    return {"success": False, "message": "此接口已废弃，请使用 /api/plugins 添加插件"}


def list_plugins() -> List[Dict[str, Any]]:
    """列出所有插件（从插件注册表）"""
    from core.plugin_registry import get_plugin_registry

    plugin_registry = get_plugin_registry()
    plugins = plugin_registry.list()

    return [
        {
            "plugin_id": p.plugin_id,
            "plugin_name": p.plugin_name,
            "description": p.description,
            "enabled": p.enabled,
        }
        for p in plugins
    ]


def reload_plugins_from_database():
    """从数据库重新加载插件"""
    from core.plugin_registry import get_plugin_registry

    plugin_registry = get_plugin_registry()
    plugin_registry.sync_from_database()
    logger.info("✅ 插件已从数据库重新加载")

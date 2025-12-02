"""
API数据模型
定义请求和响应的数据结构
"""

from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime


# ==================== 对话相关 ====================


class ChatRequest(BaseModel):
    """对话请求 - 简化版"""

    message: str = Field(..., description="用户问题")
    modelId: str = Field(..., description="模型ID")
    systemPrompt: Optional[str] = Field(None, description="系统提示词")
    pluginNames: Optional[List[str]] = Field(None, description="需要调用的插件列表")
    pluginParams: Optional[Dict[str, Dict[str, Any]]] = Field(
        None,
        description="插件运行时参数，格式：{'plugin_name': {'user_id': 'xxx', 'token': 'yyy'}}",
    )
    pluginsJson: Optional[List[Dict[str, Any]]] = Field(
        None, description="动态插件JSON配置"
    )
    enableRag: bool = Field(False, description="是否启用RAG检索")
    history: Optional[List[Dict[str, str]]] = Field(None, description="对话历史上下文")


class ChatResponse(BaseModel):
    """对话响应"""

    question: str
    answer: str
    success: bool = True
    error: Optional[str] = None
    model: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())


# ==================== RAG相关 ====================


class RAGRequest(BaseModel):
    """RAG检索请求"""

    query: str = Field(..., description="检索查询")
    topK: Optional[int] = Field(3, description="返回结果数量")


class RAGResponse(BaseModel):
    """RAG检索响应"""

    query: str
    results: List[Dict[str, Any]]
    success: bool = True
    error: Optional[str] = None
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())


# ==================== 模型管理 ====================


class ModelCreateRequest(BaseModel):
    """创建模型请求"""

    model_id: str = Field(..., description="模型唯一标识")
    display_name: str = Field(..., description="模型显示名称")
    api_key: str = Field(..., description="API密钥")
    base_url: Optional[str] = Field(None, description="API基础URL")
    model_type: str = Field(default="openai", description="模型类型")
    max_tokens: Optional[int] = Field(None, description="最大token数")
    temperature: Optional[float] = Field(None, description="温度参数")
    enabled: bool = Field(default=True, description="是否启用")
    metadata: Optional[Dict[str, Any]] = Field(None, description="额外配置")


class ModelUpdateRequest(BaseModel):
    """更新模型请求"""

    display_name: Optional[str] = None
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    model_type: Optional[str] = None
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None
    enabled: Optional[bool] = None
    metadata: Optional[Dict[str, Any]] = None


class ModelResponse(BaseModel):
    """模型响应"""

    model_id: str
    display_name: str
    model_type: str
    enabled: bool
    base_url: Optional[str] = None
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None
    metadata: Optional[Dict[str, Any]] = None
    created_at: Optional[str] = None
    updated_at: Optional[str] = None


# ==================== 插件相关 ====================


class PluginCreateRequest(BaseModel):
    """创建插件请求"""

    plugin_name: str = Field(..., description="插件名称")
    description: str = Field(..., description="插件描述")
    openapi_spec: Dict[str, Any] = Field(..., description="OpenAPI 3.0 规范")
    auth_type: Optional[str] = Field(
        "none", description="认证类型: none/bearer/api_key"
    )
    auth_config: Optional[Dict[str, Any]] = Field(None, description="认证配置")


class PluginUpdateRequest(BaseModel):
    """更新插件请求"""

    plugin_name: Optional[str] = None
    description: Optional[str] = None
    openapi_spec: Optional[Dict[str, Any]] = None
    enabled: Optional[bool] = None
    auth_type: Optional[str] = None
    auth_config: Optional[Dict[str, Any]] = None


class PluginSyncRequest(BaseModel):
    """插件同步请求"""

    plugins: List[Dict[str, Any]] = Field(..., description="插件JSON配置列表")


class PluginResponse(BaseModel):
    """插件响应"""

    plugin_name: str
    description: Optional[str] = None
    enabled: bool = True


# ==================== 批量处理 ====================


class BatchRequest(BaseModel):
    """批量处理请求"""

    requests: List[ChatRequest] = Field(..., description="批量请求列表")
    parallel: bool = Field(False, description="是否并行处理")


class BatchResponse(BaseModel):
    """批量处理响应"""

    results: List[ChatResponse]
    total: int
    success_count: int
    failure_count: int
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())


# ==================== 通用响应 ====================


class HealthResponse(BaseModel):
    """健康检查响应"""

    status: str
    database: str
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())


class ErrorResponse(BaseModel):
    """错误响应"""

    error: str
    detail: Optional[str] = None
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())

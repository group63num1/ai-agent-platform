"""
API数据模型
定义请求和响应的数据结构
"""

from pydantic import BaseModel, Field, ConfigDict, AliasChoices
from typing import Optional, List, Dict, Any
from datetime import datetime


# ==================== 对话相关 ====================


class ChatRequest(BaseModel):
    """对话请求"""

    model_config = ConfigDict(populate_by_name=True)

    message: str = Field(..., description="用户问题")
    model_id: str = Field(
        ..., description="模型ID", validation_alias=AliasChoices("model_id", "modelId")
    )
    session_id: Optional[str] = Field(
        None,
        description="会话ID，用于标记会话",
        validation_alias=AliasChoices("session_id", "sessionId"),
    )
    tools: Optional[List[str]] = Field(
        None, description="可用工具ID列表，为空或null表示不使用工具"
    )
    knowledge_bases: Optional[List[str]] = Field(
        None,
        description="可用知识库ID列表，为空或null表示不使用RAG",
        validation_alias=AliasChoices("knowledge_bases", "knowledgeBases"),
    )
    system_prompt: Optional[str] = Field(
        None,
        description="系统提示词",
        validation_alias=AliasChoices("system_prompt", "systemPrompt"),
    )
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


# ==================== 工具管理 ====================


class ToolCreateRequest(BaseModel):
    """创建工具请求"""

    name: str = Field(..., description="工具名称")
    purpose: Optional[str] = Field(None, description="工具用途")
    version: Optional[str] = Field(None, description="工具版本")
    call_method: str = Field(
        ..., description="调用方法，如 GET https://api.example.com/endpoint"
    )
    parameters: Optional[List[Dict[str, Any]]] = Field(None, description="参数定义列表")
    user_settings: Optional[Dict[str, Any]] = Field(
        None, description="用户设定的参数值"
    )


class ToolUpdateRequest(BaseModel):
    """更新工具请求"""

    name: Optional[str] = None
    purpose: Optional[str] = None
    version: Optional[str] = None
    call_method: Optional[str] = None
    parameters: Optional[List[Dict[str, Any]]] = None
    user_settings: Optional[Dict[str, Any]] = None


# ==================== 知识库管理 ====================


class KnowledgeBaseCreateRequest(BaseModel):
    """创建知识库请求"""

    user_id: str = Field(..., description="用户ID")
    name: str = Field(..., description="知识库名称")
    files: List[Dict[str, Any]] = Field(
        ..., description="上传的文件列表(包含filename和content)"
    )
    description: Optional[str] = Field(None, description="知识库描述")
    chunking_method: Optional[str] = Field(
        "recursive",
        description="chunking方式: recursive, fixed, semantic",
    )
    chunk_size: Optional[int] = Field(1000, description="chunk大小")
    chunk_overlap: Optional[int] = Field(200, description="chunk重叠")
    enabled: Optional[bool] = Field(True, description="是否启用")


class KnowledgeBaseUpdateRequest(BaseModel):
    """更新知识库请求"""

    name: Optional[str] = Field(None, description="知识库名称")
    description: Optional[str] = Field(None, description="知识库描述")
    files: Optional[List[Dict[str, Any]]] = Field(None, description="新的文件列表")
    chunking_method: Optional[str] = Field(None, description="chunking方式")
    chunk_size: Optional[int] = Field(None, description="chunk大小")
    chunk_overlap: Optional[int] = Field(None, description="chunk重叠")
    enabled: Optional[bool] = Field(None, description="是否启用")


class KnowledgeBaseQueryRequest(BaseModel):
    """查询知识库请求"""

    # 兼容旧字段名 query
    query_text: Optional[str] = Field(
        None,
        alias="query",
        description="查询文本（用于相似度搜索）",
    )
    similarity_threshold: Optional[float] = Field(
        None, description="相似度阈值（0-1）,大于此阈值返回前5条，否则返回前20条"
    )
    limit: Optional[int] = Field(None, description="返回数量限制")

    class Config:
        allow_population_by_field_name = True

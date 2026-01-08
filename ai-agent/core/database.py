"""
数据库连接和模型管理
"""

from typing import List, Optional, Generator
from sqlalchemy import (
    create_engine,
    Column,
    String,
    Boolean,
    Integer,
    Float,
    Text,
    JSON,
)
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session, relationship
from contextlib import contextmanager
import logging
import json

import config

logger = logging.getLogger(__name__)

# SQLAlchemy Base
Base = declarative_base()

# 数据库引擎
engine = None
SessionLocal = None


class ModelDB(Base):
    """模型数据库表"""

    __tablename__ = "models"

    model_id = Column(String(64), primary_key=True, comment="模型唯一标识")
    display_name = Column(String(128), nullable=False, comment="显示名称")
    api_key = Column(String(512), nullable=True, comment="API密钥")
    base_url = Column(String(512), nullable=True, comment="API基础URL")
    model = Column(String(128), nullable=False, comment="实际模型名称")
    max_tokens = Column(Integer, nullable=True, comment="最大输出长度")
    temperature = Column(Float, nullable=True, comment="创意/随机性")
    top_p = Column(Float, nullable=True, comment="nucleus sampling")
    top_k = Column(Integer, nullable=True, comment="RAG/核心采样候选词数量")
    frequency_penalty = Column(Float, nullable=True, comment="避免重复")
    presence_penalty = Column(Float, nullable=True, comment="鼓励新话题")
    stream = Column(Boolean, default=True, comment="流式输出")
    timeout = Column(Integer, nullable=True, comment="超时时间(秒)")
    retry_max_attempts = Column(Integer, nullable=True, comment="最大重试次数")
    retry_backoff_factor = Column(Integer, nullable=True, comment="重试退避因子")
    enabled = Column(Boolean, default=True, comment="是否启用")
    description = Column(Text, nullable=True, comment="模型描述")

    def to_dict(self):
        return {
            "model_id": self.model_id,
            "display_name": self.display_name,
            "api_key": self.api_key,
            "base_url": self.base_url,
            "model": self.model,
            "max_tokens": self.max_tokens,
            "temperature": self.temperature,
            "top_p": self.top_p,
            "top_k": self.top_k,
            "frequency_penalty": self.frequency_penalty,
            "presence_penalty": self.presence_penalty,
            "stream": self.stream,
            "timeout": self.timeout,
            "retry_max_attempts": self.retry_max_attempts,
            "retry_backoff_factor": self.retry_backoff_factor,
            "enabled": self.enabled,
            "description": self.description,
        }


class ToolDB(Base):
    """工具表 - 存储API工具的元数据和用户配置"""

    __tablename__ = "plugin_tools"

    tool_id = Column(String(64), primary_key=True, comment="工具唯一标识")
    name = Column(String(128), nullable=False, comment="工具名称")
    purpose = Column(Text, nullable=True, comment="工具用途")
    version = Column(String(32), nullable=True, comment="工具版本")
    call_method = Column(
        String(256),
        nullable=False,
        comment="调用方法，如 GET https://api.example.com/endpoint",
    )
    parameters = Column(
        JSON,
        nullable=True,
        comment="参数定义(JSON数组)，包含参数名、类型、必填、枚举值等",
    )
    user_settings = Column(
        JSON, nullable=True, comment="用户设定的参数值，如 {uuid: xxx, api_key: yyy}"
    )
    created_at = Column(String(64), nullable=True, comment="创建时间")
    updated_at = Column(String(64), nullable=True, comment="更新时间")

    def to_dict(self):
        return {
            "tool_id": self.tool_id,
            "name": self.name,
            "purpose": self.purpose,
            "version": self.version,
            "call_method": self.call_method,
            "parameters": self.parameters,
            "user_settings": self.user_settings,
            "created_at": self.created_at,
            "updated_at": self.updated_at,
        }


class KnowledgeBaseDB(Base):
    """知识库表 - 存储RAG向量知识库"""

    __tablename__ = "knowledge_bases"

    kb_id = Column(
        String(128), primary_key=True, comment="知识库唯一标识(user_id+name生成)"
    )
    user_id = Column(String(64), nullable=False, comment="所属用户ID")
    name = Column(String(128), nullable=False, comment="知识库名称")
    description = Column(Text, nullable=True, comment="知识库描述")
    file_paths = Column(JSON, nullable=False, comment="文档文件路径列表(JSON数组)")
    chunking_method = Column(
        String(64),
        nullable=True,
        default="recursive",
        comment="chunking方式: recursive, fixed, semantic",
    )
    chunk_size = Column(Integer, nullable=True, default=1000, comment="chunk大小")
    chunk_overlap = Column(Integer, nullable=True, default=200, comment="chunk重叠")
    total_chunks = Column(Integer, nullable=True, default=0, comment="总chunk数量")
    milvus_collection = Column(String(128), nullable=True, comment="Milvus集合名称")
    enabled = Column(Boolean, default=True, comment="是否启用")
    created_at = Column(String(64), nullable=True, comment="创建时间")
    updated_at = Column(String(64), nullable=True, comment="更新时间")

    def to_dict(self):
        return {
            "kb_id": self.kb_id,
            "user_id": self.user_id,
            "name": self.name,
            "description": self.description,
            "file_paths": self.file_paths,
            "chunking_method": self.chunking_method,
            "chunk_size": self.chunk_size,
            "chunk_overlap": self.chunk_overlap,
            "total_chunks": self.total_chunks,
            "milvus_collection": self.milvus_collection,
            "enabled": self.enabled,
            "created_at": self.created_at,
            "updated_at": self.updated_at,
        }


def init_database():
    """初始化数据库连接"""
    global engine, SessionLocal

    try:
        database_url = config.get_database_url()
        logger.info(
            f"连接数据库: {database_url.split('@')[-1] if '@' in database_url else database_url}"
        )

        # 创建引擎参数
        engine_kwargs = {"echo": config.DEBUG}

        # SQLite 不支持连接池参数
        if config.DATABASE_TYPE != "sqlite":
            engine_kwargs.update(
                {
                    "pool_size": config.DATABASE_POOL_SIZE,
                    "max_overflow": config.DATABASE_MAX_OVERFLOW,
                    "pool_pre_ping": True,
                }
            )

        # 创建引擎
        engine = create_engine(database_url, **engine_kwargs)

        # 创建会话工厂
        SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

        # 创建表
        Base.metadata.create_all(bind=engine)
        logger.info("✅ 数据库初始化成功")

        return True
    except Exception as e:
        logger.error(f"❌ 数据库初始化失败: {e}")
        raise


# ==================== 工具解析辅助函数 ====================


def parse_openapi_to_tool_fields(openapi_json: dict) -> list:
    """
    从 OpenAPI JSON 解析出工具字段列表
    返回每个端点对应的工具字段字典列表
    格式与 test.py 保持一致
    """
    tools = []

    # 安全获取顶层字段
    if not isinstance(openapi_json, dict):
        return tools

    paths = openapi_json.get("paths", {})
    version = openapi_json.get("info", {}).get("version", "N/A")
    servers = openapi_json.get("servers", [])

    # 更宽松的 server_url 获取
    server_url = ""
    if servers and isinstance(servers, list) and len(servers) > 0:
        server_url = servers[0].get("url", "") if isinstance(servers[0], dict) else ""

    for path, methods in paths.items():
        if not isinstance(methods, dict):
            continue

        for method, content in methods.items():
            if not isinstance(content, dict):
                continue

            # 工具名称
            tool_name = content.get("operationId", path)

            # 用途: summary + description
            summary = content.get("summary", "")
            description = content.get("description", "")
            purpose = summary
            if description:
                purpose += f" - {description}" if summary else description

            # 调用方法
            full_url = f"{server_url}{path}"
            call_method = f"{method.upper()} {full_url}"

            # 参数信息 - 格式化为人类可读的字符串
            params = []

            # query 或 header 参数
            if "parameters" in content and isinstance(content["parameters"], list):
                for p in content["parameters"]:
                    if not isinstance(p, dict):
                        continue

                    # 安全获取 schema 和 enum
                    schema = p.get("schema", {})
                    enum_values = (
                        schema.get("enum") if isinstance(schema, dict) else None
                    )
                    enum_text = f", 枚举值: {enum_values}" if enum_values else ""

                    param_desc = f"  - {p.get('name', 'unknown')} ({p.get('in', 'N/A')}): {p.get('description', '')}，必填: {p.get('required', False)}{enum_text}"
                    params.append(param_desc)

            # body 参数
            if "requestBody" in content:
                request_body = content.get("requestBody", {})
                if isinstance(request_body, dict):
                    body_content = request_body.get("content", {}).get(
                        "application/json", {}
                    )
                    body_schema = body_content.get("schema", {})
                    body_props = body_schema.get("properties", {})
                    required_fields = body_schema.get("required", [])

                    if isinstance(body_props, dict):
                        for name, prop in body_props.items():
                            if not isinstance(prop, dict):
                                continue

                            enum_values = prop.get("enum")
                            enum_text = (
                                f", 枚举值: {enum_values}" if enum_values else ""
                            )
                            param_desc = f"  - {name} (body): {prop.get('description', '')}，必填: {name in required_fields}{enum_text}"
                            params.append(param_desc)

            # 参数字符串
            parameters_str = "\n".join(params) if params else "无参数"

            # 收集工具信息
            tools.append(
                {
                    "name": tool_name,
                    "purpose": purpose,
                    "version": version,
                    "call_method": call_method,
                    "parameters": parameters_str,  # 人类可读的参数格式
                    "method": method.upper(),
                    "path": path,
                    "operation_id": tool_name,
                }
            )

    return tools


@contextmanager
def get_db() -> Generator[Session, None, None]:  # type: ignore
    """获取数据库会话上下文"""
    if SessionLocal is None:
        raise RuntimeError("数据库未初始化，请先调用 init_database()")

    db = SessionLocal()
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# 兼容性别名
get_session = get_db


# ==================== 模型CRUD操作 ====================


def create_model(
    model_id: str,
    display_name: str,
    api_key: Optional[str] = None,
    base_url: Optional[str] = None,
    model: Optional[str] = None,
    max_tokens: Optional[int] = None,
    temperature: Optional[float] = None,
    top_p: Optional[float] = None,
    top_k: Optional[int] = None,
    frequency_penalty: Optional[float] = None,
    presence_penalty: Optional[float] = None,
    stream: Optional[bool] = True,
    timeout: Optional[int] = None,
    retry_max_attempts: Optional[int] = None,
    retry_backoff_factor: Optional[int] = None,
    enabled: bool = True,
    description: Optional[str] = None,
) -> ModelDB:
    """创建模型"""
    with get_db() as db:
        model_db = ModelDB(
            model_id=model_id,
            display_name=display_name,
            api_key=api_key,
            base_url=base_url,
            model=model,
            max_tokens=max_tokens,
            temperature=temperature,
            top_p=top_p,
            top_k=top_k,
            frequency_penalty=frequency_penalty,
            presence_penalty=presence_penalty,
            stream=stream,
            timeout=timeout,
            retry_max_attempts=retry_max_attempts,
            retry_backoff_factor=retry_backoff_factor,
            enabled=enabled,
            description=description,
        )
        db.add(model_db)
        db.flush()
        return model_db


def get_model(model_id: str) -> Optional[ModelDB]:
    """获取单个模型"""
    with get_db() as db:
        return db.query(ModelDB).filter(ModelDB.model_id == model_id).first()


def list_models(enabled_only: bool = False) -> List[dict]:
    """列出所有模型"""
    with get_db() as db:
        query = db.query(ModelDB)
        if enabled_only:
            query = query.filter(ModelDB.enabled == True)
        models = query.all()
        # 转换为字典，避免 DetachedInstanceError
        return [
            {
                "model_id": m.model_id,
                "display_name": m.display_name,
                "model": m.model,
                "api_key": m.api_key,
                "base_url": m.base_url,
                "enabled": m.enabled,
                "description": m.description,
                "max_tokens": m.max_tokens,
                "temperature": m.temperature,
                "top_p": m.top_p,
                "top_k": m.top_k,
                "frequency_penalty": m.frequency_penalty,
                "presence_penalty": m.presence_penalty,
                "stream": m.stream,
                "timeout": m.timeout,
                "retry_max_attempts": m.retry_max_attempts,
                "retry_backoff_factor": m.retry_backoff_factor,
            }
            for m in models
        ]


def update_model(model_id: str, **kwargs) -> Optional[ModelDB]:
    """更新模型"""
    with get_db() as db:
        model_db = db.query(ModelDB).filter(ModelDB.model_id == model_id).first()
        if model_db:
            for key, value in kwargs.items():
                if hasattr(model_db, key):
                    setattr(model_db, key, value)
            db.flush()
        return model_db


def delete_model(model_id: str) -> bool:
    """删除模型"""
    with get_db() as db:
        model_db = db.query(ModelDB).filter(ModelDB.model_id == model_id).first()
        if model_db:
            db.delete(model_db)
            db.flush()
            return True
        return False


# ==================== 工具CRUD操作 ====================


def create_tool(
    tool_id: str,
    name: str,
    call_method: str,
    purpose: Optional[str] = None,
    version: Optional[str] = None,
    parameters: Optional[List[dict]] = None,
    user_settings: Optional[dict] = None,
) -> ToolDB:
    """创建工具

    Args:
        tool_id: 工具唯一标识
        name: 工具名称
        call_method: 调用方法，如 GET https://api.example.com/endpoint
        purpose: 工具用途
        version: 工具版本
        parameters: 参数定义列表
        user_settings: 用户设定的参数值
    """
    from datetime import datetime

    with get_db() as db:
        tool = ToolDB(
            tool_id=tool_id,
            name=name,
            purpose=purpose,
            version=version,
            call_method=call_method,
            parameters=parameters,
            user_settings=user_settings or {},
            created_at=datetime.now().isoformat(),
            updated_at=datetime.now().isoformat(),
        )
        db.add(tool)
        db.flush()
        return tool


def get_tool(tool_id: str) -> Optional[ToolDB]:
    """获取单个工具"""
    with get_db() as db:
        return db.query(ToolDB).filter(ToolDB.tool_id == tool_id).first()


def list_tools() -> List[dict]:
    """列出所有工具"""
    with get_db() as db:
        query = db.query(ToolDB)
        tools = query.all()
        return [t.to_dict() for t in tools]


def update_tool(tool_id: str, **kwargs) -> Optional[ToolDB]:
    """更新工具"""
    from datetime import datetime

    with get_db() as db:
        tool = db.query(ToolDB).filter(ToolDB.tool_id == tool_id).first()
        if tool:
            for key, value in kwargs.items():
                if hasattr(tool, key) and key != "created_at":
                    setattr(tool, key, value)
            tool.updated_at = datetime.now().isoformat()
            db.flush()
        return tool


def delete_tool(tool_id: str) -> bool:
    """删除工具"""
    with get_db() as db:
        tool = db.query(ToolDB).filter(ToolDB.tool_id == tool_id).first()
        if tool:
            db.delete(tool)
            db.flush()
            return True
        return False


# ==================== 知识库CRUD操作 ====================


def create_knowledge_base(
    kb_id: str,
    name: str,
    documents: List[str],
    description: Optional[str] = None,
    enabled: bool = True,
) -> KnowledgeBaseDB:
    """创建知识库"""
    from datetime import datetime

    with get_db() as db:
        kb = KnowledgeBaseDB(
            kb_id=kb_id,
            name=name,
            description=description,
            documents=documents,
            enabled=enabled,
            created_at=datetime.now().isoformat(),
            updated_at=datetime.now().isoformat(),
        )
        db.add(kb)
        db.flush()
        return kb


def get_knowledge_base(kb_id: str) -> Optional[dict]:
    """获取单个知识库"""
    with get_db() as db:
        kb = db.query(KnowledgeBaseDB).filter(KnowledgeBaseDB.kb_id == kb_id).first()
        if kb:
            # 刷新会话以加载所有属性
            db.refresh(kb)
            # 显式触发字段加载以确保数据完整
            _ = kb.milvus_collection
            _ = kb.file_paths
            _ = kb.total_chunks
            _ = kb.enabled
            _ = kb.chunking_method

            # 转换为字典并从会话中分离实体，避免跨会话访问问题
            kb_dict = kb.to_dict()
            db.expunge(kb)
            return kb_dict
        return None


def list_knowledge_bases(enabled_only: bool = False) -> List[dict]:
    """列出知识库"""
    with get_db() as db:
        query = db.query(KnowledgeBaseDB)
        if enabled_only:
            query = query.filter(KnowledgeBaseDB.enabled == True)
        kbs = query.all()
        return [kb.to_dict() for kb in kbs]


def update_knowledge_base(kb_id: str, **kwargs) -> Optional[KnowledgeBaseDB]:
    """更新知识库"""
    from datetime import datetime

    with get_db() as db:
        kb = db.query(KnowledgeBaseDB).filter(KnowledgeBaseDB.kb_id == kb_id).first()
        if kb:
            for key, value in kwargs.items():
                if hasattr(kb, key):
                    setattr(kb, key, value)
            kb.updated_at = datetime.now().isoformat()
            db.flush()
        return kb


def delete_knowledge_base(kb_id: str) -> bool:
    """删除知识库"""
    with get_db() as db:
        kb = db.query(KnowledgeBaseDB).filter(KnowledgeBaseDB.kb_id == kb_id).first()
        if kb:
            db.delete(kb)
            db.flush()
            return True
        return False

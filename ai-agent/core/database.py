"""
数据库连接和模型管理
"""

from typing import List, Optional, Generator
from sqlalchemy import create_engine, Column, String, Boolean, Integer, Text, JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from contextlib import contextmanager
import logging

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
    model = Column(String(128), nullable=False, comment="实际模型名称")
    api_key = Column(String(512), nullable=True, comment="API密钥")
    base_url = Column(String(512), nullable=True, comment="API基础URL")
    enabled = Column(Boolean, default=True, comment="是否启用")
    description = Column(Text, nullable=True, comment="模型描述")
    max_tokens = Column(Integer, nullable=True, comment="最大token数")
    temperature = Column(String(16), nullable=True, comment="温度参数")

    def to_dict(self):
        return {
            "model_id": self.model_id,
            "display_name": self.display_name,
            "model": self.model,
            "api_key": self.api_key,
            "base_url": self.base_url,
            "enabled": self.enabled,
            "description": self.description,
            "max_tokens": self.max_tokens,
            "temperature": self.temperature,
        }


class PluginDB(Base):
    """插件数据库表 - 存储 OpenAPI 3.0 格式的插件定义"""

    __tablename__ = "plugins"

    plugin_id = Column(String(64), primary_key=True, comment="插件唯一标识")
    plugin_name = Column(String(128), nullable=False, comment="插件名称")
    description = Column(Text, nullable=True, comment="插件描述")
    openapi_spec = Column(JSON, nullable=False, comment="OpenAPI 3.0 规范 JSON")
    enabled = Column(Boolean, default=True, comment="是否启用")
    auth_type = Column(
        String(32), nullable=True, comment="认证类型: none/bearer/apikey"
    )
    auth_config = Column(JSON, nullable=True, comment="认证配置")

    def to_dict(self):
        return {
            "plugin_id": self.plugin_id,
            "plugin_name": self.plugin_name,
            "description": self.description,
            "openapi_spec": self.openapi_spec,
            "enabled": self.enabled,
            "auth_type": self.auth_type,
            "auth_config": self.auth_config,
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
    model: str,
    api_key: Optional[str] = None,
    base_url: Optional[str] = None,
    enabled: bool = True,
    description: Optional[str] = None,
    max_tokens: Optional[int] = None,
    temperature: Optional[str] = None,
) -> ModelDB:
    """创建模型"""
    with get_db() as db:
        model_db = ModelDB(
            model_id=model_id,
            display_name=display_name,
            model=model,
            api_key=api_key,
            base_url=base_url,
            enabled=enabled,
            description=description,
            max_tokens=max_tokens,
            temperature=temperature,
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


def seed_default_models():
    """初始化默认模型（仅在表为空时）"""
    with get_db() as db:
        count = db.query(ModelDB).count()
        if count == 0:
            logger.info("📦 初始化默认模型...")
            default_models = [
                ModelDB(
                    model_id="qwen-plus",
                    display_name="通义千问Plus",
                    model="qwen-plus",
                    api_key="",  # 需要用户配置
                    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                    enabled=False,  # 默认禁用，等待用户配置API Key
                    description="阿里云通义千问Plus模型",
                ),
                ModelDB(
                    model_id="gpt-3.5-turbo",
                    display_name="GPT-3.5 Turbo",
                    model="gpt-3.5-turbo",
                    api_key="",
                    base_url="https://api.openai.com/v1",
                    enabled=False,
                    description="OpenAI GPT-3.5 Turbo模型",
                ),
            ]
            db.add_all(default_models)
            db.flush()
            logger.info(f"✅ 已添加 {len(default_models)} 个默认模型")


# ==================== 插件CRUD操作 ====================


def create_plugin(
    plugin_id: str,
    plugin_name: str,
    openapi_spec: dict,
    description: Optional[str] = None,
    enabled: bool = True,
    auth_type: Optional[str] = None,
    auth_config: Optional[dict] = None,
) -> PluginDB:
    """创建插件"""
    with get_db() as db:
        plugin_db = PluginDB(
            plugin_id=plugin_id,
            plugin_name=plugin_name,
            description=description,
            openapi_spec=openapi_spec,
            enabled=enabled,
            auth_type=auth_type,
            auth_config=auth_config,
        )
        db.add(plugin_db)
        db.flush()
        return plugin_db


def get_plugin(plugin_id: str) -> Optional[PluginDB]:
    """获取单个插件"""
    with get_db() as db:
        return db.query(PluginDB).filter(PluginDB.plugin_id == plugin_id).first()


def list_plugins(
    enabled_only: bool = False,
) -> List[dict]:
    """列出插件"""
    with get_db() as db:
        query = db.query(PluginDB)
        if enabled_only:
            query = query.filter(PluginDB.enabled == True)
        plugins = query.all()
        # 转换为字典，避免 DetachedInstanceError
        return [
            {
                "plugin_id": p.plugin_id,
                "plugin_name": p.plugin_name,
                "description": p.description,
                "enabled": p.enabled,
                "auth_type": p.auth_type,
                "auth_config": p.auth_config,
                "openapi_spec": p.openapi_spec,
            }
            for p in plugins
        ]


def update_plugin(plugin_id: str, **kwargs) -> Optional[PluginDB]:
    """更新插件"""
    with get_db() as db:
        plugin_db = db.query(PluginDB).filter(PluginDB.plugin_id == plugin_id).first()
        if plugin_db:
            for key, value in kwargs.items():
                if hasattr(plugin_db, key):
                    setattr(plugin_db, key, value)
            db.flush()
        return plugin_db


def delete_plugin(plugin_id: str) -> bool:
    """删除插件"""
    with get_db() as db:
        plugin_db = db.query(PluginDB).filter(PluginDB.plugin_id == plugin_id).first()
        if plugin_db:
            db.delete(plugin_db)
            db.flush()
            return True
        return False

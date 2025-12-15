"""
系统配置文件
优先读取环境变量，环境变量不存在时使用默认值
模型配置已迁移至数据库，请通过数据库管理
"""

import os
from typing import Optional

# ==================== 服务配置 ====================
HOST: str = os.getenv("HOST", "0.0.0.0")
PORT: int = int(os.getenv("PORT", "8000"))
DEBUG: bool = os.getenv("DEBUG", "false").lower() == "true"
LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")

# ==================== 数据库配置 ====================
# 数据库类型: mysql, postgresql, sqlite
DATABASE_TYPE: str = os.getenv("DATABASE_TYPE", "mysql")

# MySQL/PostgreSQL 配置
DATABASE_HOST: str = os.getenv("DATABASE_HOST", "localhost")
DATABASE_PORT: int = int(os.getenv("DATABASE_PORT", "3307"))
DATABASE_USER: str = os.getenv("DATABASE_USER", "root")
DATABASE_PASSWORD: str = os.getenv("DATABASE_PASSWORD", "123456")
DATABASE_NAME: str = os.getenv("DATABASE_NAME", "new_db")

# SQLite 配置（仅当 DATABASE_TYPE=sqlite 时使用）
SQLITE_PATH: str = os.getenv("SQLITE_PATH", "./data/ai_agent.db")

# 连接池配置
DATABASE_POOL_SIZE: int = int(os.getenv("DATABASE_POOL_SIZE", "5"))
DATABASE_MAX_OVERFLOW: int = int(os.getenv("DATABASE_MAX_OVERFLOW", "10"))

# ==================== Java 后端配置 ====================
JAVA_BACKEND_URL: str = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080")
JAVA_BACKEND_TIMEOUT: int = int(os.getenv("JAVA_BACKEND_TIMEOUT", "30"))

# ==================== Milvus 向量数据库配置 ====================
MILVUS_HOST: str = os.getenv("MILVUS_HOST", "127.0.0.1")
MILVUS_PORT: int = int(os.getenv("MILVUS_PORT", "19530"))
MILVUS_ALIAS: str = os.getenv("MILVUS_ALIAS", "default")
MILVUS_COLLECTION_NAME: str = os.getenv("MILVUS_COLLECTION_NAME", "knowledge_base")
MILVUS_ENABLE: bool = os.getenv("MILVUS_ENABLE", "true").lower() == "true"

# ==================== Agent 配置 ====================
MAX_ITERATIONS: int = int(os.getenv("MAX_ITERATIONS", "5"))
MAX_HISTORY_LENGTH: int = int(os.getenv("MAX_HISTORY_LENGTH", "20"))
SUMMARY_TRIGGER: int = int(os.getenv("SUMMARY_TRIGGER", "16"))
VERBOSE: bool = os.getenv("VERBOSE", "true").lower() == "true"

# ==================== 插件配置 ====================
PLUGIN_TIMEOUT: int = int(os.getenv("PLUGIN_TIMEOUT", "30"))
PLUGIN_MAX_RETRIES: int = int(os.getenv("PLUGIN_MAX_RETRIES", "3"))

# ==================== 安全配置 ====================
# CORS 配置
CORS_ORIGINS: list = os.getenv("CORS_ORIGINS", "*").split(",")

# 请求限制
MAX_REQUEST_SIZE: int = int(os.getenv("MAX_REQUEST_SIZE", "10485760"))  # 10MB

# ==================== 辅助函数 ====================


def get_database_url() -> str:
    """获取数据库连接 URL"""
    if DATABASE_TYPE == "sqlite":
        return f"sqlite:///{SQLITE_PATH}"
    elif DATABASE_TYPE == "postgresql":
        return f"postgresql://{DATABASE_USER}:{DATABASE_PASSWORD}@{DATABASE_HOST}:{DATABASE_PORT}/{DATABASE_NAME}"
    else:  # mysql
        return f"mysql+pymysql://{DATABASE_USER}:{DATABASE_PASSWORD}@{DATABASE_HOST}:{DATABASE_PORT}/{DATABASE_NAME}?charset=utf8mb4"


def validate_config() -> tuple[bool, Optional[str]]:
    """验证配置有效性"""
    if DATABASE_TYPE not in ["mysql", "postgresql", "sqlite"]:
        return False, f"不支持的数据库类型: {DATABASE_TYPE}"

    if DATABASE_TYPE != "sqlite":
        if not DATABASE_USER or not DATABASE_NAME:
            return False, "数据库用户名和数据库名不能为空"

    return True, None


# ==================== 启动时验证 ====================
if __name__ == "__main__":
    is_valid, error = validate_config()
    if not is_valid:
        print(f"❌ 配置验证失败: {error}")
        exit(1)

    print("✅ 配置验证通过")
    print(f"📊 数据库类型: {DATABASE_TYPE}")
    print(f"📊 数据库URL: {get_database_url()}")
    print(f"🌐 服务地址: {HOST}:{PORT}")

"""
AI Agent 主启动脚本
启动FastAPI服务器,提供HTTP API接口
"""

import uvicorn
from dotenv import load_dotenv
import os

# 加载环境变量
load_dotenv()

if __name__ == "__main__":
    # 导入配置
    import config

    # 验证配置
    is_valid, error = config.validate_config()
    if not is_valid:
        print(f"❌ 配置验证失败: {error}")
        exit(1)

    print("=" * 60)
    print("🚀 正在启动 AI Agent API 服务器...")
    print("=" * 60)
    print(f"📍 服务地址: http://{config.HOST}:{config.PORT}")
    print(f"📖 API文档: http://{config.HOST}:{config.PORT}/docs")
    print(f"❤️  健康检查: http://{config.HOST}:{config.PORT}/health")
    print(f"🗄️  数据库类型: {config.DATABASE_TYPE}")
    print("=" * 60)

    # 启动FastAPI服务器
    uvicorn.run(
        "app.main:app",
        host=config.HOST,
        port=config.PORT,
        reload=config.DEBUG,
        log_level=config.LOG_LEVEL.lower(),
    )

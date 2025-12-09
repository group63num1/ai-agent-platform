"""
导入模型配置到数据库的脚本
"""

import os
from core.database import init_database, create_model, get_model, update_model

# 模型配置
MODEL_CONFIG = {
    # ---------- API 身份认证 ----------
    "api_key": os.getenv("DASHSCOPE_API_KEY", "sk-24c630328e3d478aa7a8156ac1ab6dca"),
    "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1",
    # ---------- 模型 ----------
    "model": "qwen3-max",
    # ---------- 生成参数 ----------
    "max_tokens": 1024,  # 最大输出长度
    "temperature": 0.7,  # 创意 / 随机性
    "top_p": 0.9,  # nucleus sampling
    "top_k": 50,  # RAG / 核心采样候选词数量
    "frequency_penalty": 0.0,  # 避免重复
    "presence_penalty": 0.0,  # 鼓励新话题
    "stop_sequences": ["\n\n"],
    # ---------- 流式输出 ----------
    "stream": True,
    # ---------- 超时 & 重试 ----------
    "timeout": 30,
    "retry": {"max_attempts": 3, "backoff_factor": 2},
}


def import_model_from_config(config: dict, model_id: str, display_name: str = None):
    """
    从配置字典导入模型到数据库

    Args:
        config: 模型配置字典
        model_id: 模型ID（必填）
        display_name: 显示名称（可选，默认使用 model_id）
    """
    # 初始化数据库
    init_database()

    # 如果没有提供显示名称，使用 model_id
    if not display_name:
        display_name = model_id

    # 提取重试配置
    retry_config = config.get("retry", {})

    # 准备数据
    model_data = {
        "display_name": display_name,
        "api_key": config.get("api_key"),
        "base_url": config.get("base_url"),
        "model": config.get("model"),
        "max_tokens": config.get("max_tokens"),
        "temperature": config.get("temperature"),
        "top_p": config.get("top_p"),
        "top_k": config.get("top_k"),
        "frequency_penalty": config.get("frequency_penalty"),
        "presence_penalty": config.get("presence_penalty"),
        "stop_sequences": config.get("stop_sequences"),
        "stream": config.get("stream", True),
        "timeout": config.get("timeout"),
        "retry_max_attempts": retry_config.get("max_attempts"),
        "retry_backoff_factor": retry_config.get("backoff_factor"),
        "enabled": True,
        "description": f"自动导入的 {model_id} 模型配置",
    }

    # 校验必填字段
    if not model_data.get("api_key") or not model_data.get("base_url"):
        raise ValueError(f"❌ api_key 和 base_url 不能为空，请检查配置！")

    try:
        # 检查模型是否已存在
        existing = get_model(model_id)

        if existing:
            # 更新现有模型
            update_model(model_id, **model_data)
            print(f"🔄 成功更新模型: {model_id} ({display_name})")
        else:
            # 创建新模型
            create_model(model_id=model_id, **model_data)
            print(f"✅ 成功创建模型: {model_id} ({display_name})")

        return True
    except Exception as e:
        print(f"❌ 导入失败: {str(e)}")
        import traceback

        traceback.print_exc()
        return False


if __name__ == "__main__":
    # 使用固定的 model_id 以保持与测试脚本一致
    model_id = "qwen-max"

    success = import_model_from_config(
        MODEL_CONFIG,
        model_id=model_id,
        display_name="通义千问-Max",  # 显示名称仍可自定义
    )

    if success:
        print("✅ 模型配置导入完成！")
    else:
        print("❌ 模型配置导入失败！")

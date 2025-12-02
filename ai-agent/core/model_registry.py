"""
模型注册中心 - 从数据库加载模型配置
"""

from typing import Dict, List, Optional
from dataclasses import dataclass, asdict
import logging

logger = logging.getLogger(__name__)


@dataclass
class ModelInfo:
    """模型信息"""

    model_id: str
    display_name: str
    model: str  # LLM model name/id
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    enabled: bool = True
    description: Optional[str] = None
    max_tokens: Optional[int] = None
    temperature: Optional[str] = None


class ModelRegistry:
    """模型注册中心 - 内存缓存"""

    def __init__(self):
        self._models: Dict[str, ModelInfo] = {}

    def add(self, info: ModelInfo):
        """添加模型到注册中心"""
        self._models[info.model_id] = info

    def remove(self, model_id: str) -> bool:
        """从注册中心移除模型"""
        return self._models.pop(model_id, None) is not None

    def list(self, enabled_only: bool = False) -> List[Dict]:
        """列出所有模型"""
        items = [asdict(m) for m in self._models.values()]
        if enabled_only:
            items = [m for m in items if m.get("enabled", True)]
        return items

    def get(self, model_id: str) -> Optional[ModelInfo]:
        """获取指定模型"""
        return self._models.get(model_id)

    def sync_from_database(self):
        """从数据库同步模型到内存"""
        try:
            from core.database import list_models

            db_models = list_models()
            self._models.clear()

            for db_model in db_models:
                model_info = ModelInfo(
                    model_id=db_model["model_id"],
                    display_name=db_model["display_name"],
                    model=db_model["model"],
                    api_key=db_model["api_key"],
                    base_url=db_model["base_url"],
                    enabled=db_model["enabled"],
                    description=db_model["description"],
                    max_tokens=db_model["max_tokens"],
                    temperature=db_model["temperature"],
                )
                self._models[model_info.model_id] = model_info

            logger.info(f"✅ 从数据库同步了 {len(self._models)} 个模型")
        except Exception as e:
            logger.error(f"❌ 从数据库同步模型失败: {e}")
            raise


# Singleton
_registry: Optional[ModelRegistry] = None


def get_model_registry() -> ModelRegistry:
    """获取模型注册中心单例"""
    global _registry
    if _registry is None:
        _registry = ModelRegistry()
        logger.info("📋 模型注册中心已创建（等待数据库同步）")
    return _registry

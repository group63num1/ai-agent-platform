"""
插件注册表 - 插件信息的内存缓存
从数据库加载 OpenAPI 插件并缓存在内存中
"""

import logging
from typing import Dict, List, Optional
from dataclasses import dataclass
from core.plugins import parse_openapi_plugin

logger = logging.getLogger(__name__)


@dataclass
class PluginInfo:
    """插件信息（只存储元数据，不存储工具实例）"""

    plugin_id: str
    plugin_name: str
    description: str
    enabled: bool
    auth_type: str
    auth_config: Optional[Dict]  # 已弃用，配置由前端传递
    openapi_spec: Dict


class PluginRegistry:
    """插件注册表 - 管理所有插件"""

    def __init__(self):
        self._plugins: Dict[str, PluginInfo] = {}
        logger.info("初始化插件注册表")

    def register(self, plugin_info: PluginInfo):
        """注册插件到内存"""
        self._plugins[plugin_info.plugin_id] = plugin_info
        logger.info(f"注册插件: {plugin_info.plugin_name} ({plugin_info.plugin_id})")

    def get(self, plugin_id: str) -> Optional[PluginInfo]:
        """获取插件信息"""
        return self._plugins.get(plugin_id)

    def get_by_name(self, plugin_name: str) -> Optional[PluginInfo]:
        """根据插件名称获取插件"""
        for plugin in self._plugins.values():
            if plugin.plugin_name == plugin_name:
                return plugin
        return None

    def remove(self, plugin_id: str) -> bool:
        """移除插件"""
        if plugin_id in self._plugins:
            del self._plugins[plugin_id]
            logger.info(f"移除插件: {plugin_id}")
            return True
        return False

    def list(
        self,
        enabled_only: bool = False,
    ) -> List[PluginInfo]:
        """
        列出插件

        Args:
            enabled_only: 只返回启用的插件
        """
        plugins = list(self._plugins.values())

        if enabled_only:
            plugins = [p for p in plugins if p.enabled]

        return plugins

    def get_tools_for_plugins(
        self, plugin_names: List[str], runtime_params: Optional[Dict[str, Dict]] = None
    ) -> List:
        """
        获取指定插件的 LangChain Tools（动态创建）

        Args:
            plugin_names: 插件名称列表
            runtime_params: 运行时参数，格式：{'plugin_name': {'token': 'xxx', ...}}

        Returns:
            LangChain Tool 列表
        """
        from core.plugins import parse_openapi_plugin

        tools = []
        runtime_params = runtime_params or {}

        for name in plugin_names:
            plugin = self.get_by_name(name)
            if plugin and plugin.enabled:
                # 获取该插件的运行时参数
                plugin_runtime_params = runtime_params.get(name, {})

                # 动态创建工具（使用运行时参数）
                plugin_tools = parse_openapi_plugin(
                    plugin_id=plugin.plugin_id,
                    openapi_spec=plugin.openapi_spec,
                    auth_type=plugin.auth_type,
                    runtime_params=plugin_runtime_params,
                )
                tools.extend(plugin_tools)
                logger.info(
                    f"插件 {name} 创建了 {len(plugin_tools)} 个工具"
                    + (
                        f"（使用运行时参数: {list(plugin_runtime_params.keys())}）"
                        if plugin_runtime_params
                        else ""
                    )
                )
            else:
                logger.warning(f"插件未找到或未启用: {name}")
        return tools

    def sync_from_database(self):
        """从数据库同步插件到内存"""
        try:
            from core.database import list_plugins as db_list_plugins

            # 获取所有启用的插件
            db_plugins = db_list_plugins(enabled_only=True)

            # 清空当前注册表
            self._plugins.clear()

            # 加载插件
            for db_plugin in db_plugins:
                try:
                    # 转换为字典
                    plugin_dict = (
                        db_plugin.to_dict()
                        if hasattr(db_plugin, "to_dict")
                        else db_plugin
                    )

                    # 创建插件信息（不预创建工具，工具在调用时动态创建）
                    plugin_info = PluginInfo(
                        plugin_id=plugin_dict["plugin_id"],
                        plugin_name=plugin_dict["plugin_name"],
                        description=plugin_dict["description"],
                        enabled=plugin_dict["enabled"],
                        auth_type=plugin_dict.get("auth_type", "none"),
                        auth_config=plugin_dict.get("auth_config"),
                        openapi_spec=plugin_dict["openapi_spec"],
                    )

                    # 注册到内存
                    self.register(plugin_info)

                except Exception as e:
                    logger.error(
                        f"加载插件失败: {db_plugin.get('plugin_name')} - {str(e)}"
                    )
                    continue

            logger.info(f"✅ 已从数据库同步 {len(self._plugins)} 个插件")

        except Exception as e:
            logger.error(f"从数据库同步插件失败: {str(e)}")


# 全局单例
_plugin_registry = None


def get_plugin_registry() -> PluginRegistry:
    """获取插件注册表单例"""
    global _plugin_registry
    if _plugin_registry is None:
        _plugin_registry = PluginRegistry()
    return _plugin_registry

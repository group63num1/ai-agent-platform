"""
清空 Milvus 向量数据库的所有集合（慎用）。
- 连接到 Milvus
- 列出所有集合
- 逐个 drop_collection 删除
- 打印删除结果统计

使用方法（在项目根目录）：
    python test代码/clear_milvus_all.py
"""

import logging
from pymilvus import connections, utility

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def connect_milvus() -> bool:
    """连接到 Milvus 默认实例"""
    try:
        connections.connect(alias="default", host="127.0.0.1", port=19530, timeout=30)
        logger.info("✅ 已连接到 Milvus")
        return True
    except Exception as e:
        logger.error(f"❌ 连接失败: {e}")
        return False


def drop_all_collections() -> None:
    """删除所有集合"""
    try:
        collections = utility.list_collections(using="default")
        if not collections:
            logger.info("⚠️ 没有集合可删除，Milvus 为空")
            return

        logger.warning("⚠️ 将删除所有集合: %s", collections)
        deleted = []
        failed = []

        for name in collections:
            try:
                utility.drop_collection(name, using="default")
                logger.info("🗑️ 已删除集合: %s", name)
                deleted.append(name)
            except Exception as e:  # 保留错误，继续下一条
                logger.error("❌ 删除集合失败 %s: %s", name, e)
                failed.append((name, str(e)))

        logger.info("✅ 删除完成，成功 %d 个，失败 %d 个", len(deleted), len(failed))
        if failed:
            for name, err in failed:
                logger.info("   失败: %s -> %s", name, err)
    except Exception as e:
        logger.error(f"❌ 执行删除时出错: {e}")


def main() -> None:
    if not connect_milvus():
        return
    drop_all_collections()
    connections.disconnect("default")
    logger.info("👋 已断开连接")


if __name__ == "__main__":
    main()

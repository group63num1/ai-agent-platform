"""
查看 Milvus 向量数据库结构
显示集合信息、字段定义、索引配置和数据样本
"""

import logging
from pathlib import Path
from pymilvus import connections, utility, Collection

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def connect_milvus():
    """连接到 Milvus"""
    try:
        connections.connect(alias="default", host="127.0.0.1", port=19530, timeout=30)
        logger.info("✅ 已连接到 Milvus")
        return True
    except Exception as e:
        logger.error(f"❌ 连接失败: {e}")
        return False


def list_collections():
    """列出所有集合"""
    print("\n" + "=" * 80)
    print("📚 Milvus 集合列表")
    print("=" * 80)

    try:
        collections = utility.list_collections(using="default")

        if not collections:
            print("\n⚠️  未找到任何集合")
            return []

        print(f"\n找到 {len(collections)} 个集合:\n")

        collection_info = []
        for col_name in collections:
            col = Collection(col_name, using="default")
            num_entities = col.num_entities

            collection_info.append(
                {"集合名称": col_name, "实体数量": num_entities, "状态": "✅ 活跃"}
            )

        # 使用简单格式显示
        for info in collection_info:
            print(f"  • {info['集合名称']}")
            print(f"    实体数量: {info['实体数量']}")
            print(f"    状态: {info['状态']}")
            print()

        return collections

    except Exception as e:
        logger.error(f"❌ 列出集合失败: {e}")
        return []


def show_collection_schema(collection_name: str):
    """显示集合的 Schema 结构"""
    print("\n" + "=" * 80)
    print(f"📋 集合结构: {collection_name}")
    print("=" * 80)

    try:
        col = Collection(collection_name, using="default")
        schema = col.schema

        print(f"\n集合描述: {schema.description or '无'}")
        print(f"自动生成ID: {schema.auto_id}")
        print(f"支持动态字段: {schema.enable_dynamic_field}")

        # 显示字段信息
        print(f"\n📊 字段列表 (共 {len(schema.fields)} 个字段):\n")

        field_info = []
        for field in schema.fields:
            field_dict = {
                "字段名": field.name,
                "数据类型": str(field.dtype).split(".")[-1],
                "是否主键": "✅" if field.is_primary else "",
                "自动生成": "✅" if field.auto_id else "",
            }

            # 向量字段显示维度
            if hasattr(field, "dim"):
                field_dict["维度/长度"] = field.dim
            elif hasattr(field, "max_length"):
                field_dict["维度/长度"] = f"max={field.max_length}"
            else:
                field_dict["维度/长度"] = "-"

            field_dict["描述"] = field.description or "-"

            field_info.append(field_dict)

        # 使用简单格式显示字段信息
        for field in field_info:
            print(f"  字段: {field['字段名']}")
            print(f"    数据类型: {field['数据类型']}")
            if field["是否主键"]:
                print(f"    主键: {field['是否主键']}")
            if field["自动生成"]:
                print(f"    自动生成: {field['自动生成']}")
            print(f"    维度/长度: {field['维度/长度']}")
            print(f"    描述: {field['描述']}")
            print()

        return True

    except Exception as e:
        logger.error(f"❌ 获取 Schema 失败: {e}")
        return False


def show_collection_indexes(collection_name: str):
    """显示集合的索引信息"""
    print("\n" + "=" * 80)
    print(f"🔍 索引信息: {collection_name}")
    print("=" * 80)

    try:
        col = Collection(collection_name, using="default")

        # 获取所有字段的索引
        indexes = col.indexes

        if not indexes:
            print("\n⚠️  该集合没有索引")
            return

        print(f"\n共 {len(indexes)} 个索引:\n")

        for idx in indexes:
            print(f"📌 字段: {idx.field_name}")
            print(f"   索引类型: {idx.params.get('index_type', '未知')}")
            print(f"   相似度度量: {idx.params.get('metric_type', '未知')}")

            # 显示索引参数
            if "params" in idx.params:
                print(f"   参数: {idx.params['params']}")
            print()

        return True

    except Exception as e:
        logger.error(f"❌ 获取索引信息失败: {e}")
        return False


def show_collection_stats(collection_name: str):
    """显示集合的统计信息"""
    print("\n" + "=" * 80)
    print(f"📊 统计信息: {collection_name}")
    print("=" * 80)

    try:
        col = Collection(collection_name, using="default")

        stats = {
            "集合名称": collection_name,
            "实体总数": col.num_entities,
            "分区数量": len(col.partitions),
            "加载状态": "已加载" if utility.load_state(collection_name) else "未加载",
        }

        print()
        for key, value in stats.items():
            print(f"  {key}: {value}")

        # 显示分区信息
        if col.partitions:
            print(f"\n  分区列表:")
            for partition in col.partitions:
                print(f"    - {partition.name}: {partition.num_entities} 个实体")

        print()
        return True

    except Exception as e:
        logger.error(f"❌ 获取统计信息失败: {e}")
        return False


def show_sample_data(collection_name: str, limit: int = 5):
    """显示集合的样本数据"""
    print("\n" + "=" * 80)
    print(f"📝 样本数据: {collection_name} (前 {limit} 条)")
    print("=" * 80)

    try:
        col = Collection(collection_name, using="default")

        # 加载集合
        col.load()

        # 查询样本数据
        results = col.query(
            expr="id >= 0",
            output_fields=["id", "text", "source", "timestamp"],
            limit=limit,
        )

        if not results:
            print("\n⚠️  集合为空")
            return

        print(f"\n共查询到 {len(results)} 条数据:\n")

        for i, result in enumerate(results, 1):
            print(f"{'─'*80}")
            print(f"【数据 {i}】")
            print(f"  ID: {result.get('id')}")
            print(f"  来源: {result.get('source', '未知')}")
            print(f"  时间戳: {result.get('timestamp', 0)}")

            text = result.get("text", "")
            if len(text) > 150:
                print(f"  文本: {text[:150]}...")
            else:
                print(f"  文本: {text}")
            print()

        return True

    except Exception as e:
        logger.error(f"❌ 查询样本数据失败: {e}")
        return False


def interactive_mode():
    """交互式模式"""
    print("\n" + "╔" + "=" * 78 + "╗")
    print("║" + " " * 78 + "║")
    print("║" + "  🔍 Milvus 向量数据库结构查看工具".center(78) + "║")
    print("║" + " " * 78 + "║")
    print("╚" + "=" * 78 + "╝")

    # 连接 Milvus
    if not connect_milvus():
        return

    # 列出所有集合
    collections = list_collections()

    if not collections:
        print("\n⚠️  数据库为空，请先创建知识库")
        return

    while True:
        print("\n" + "─" * 80)
        print("请选择要查看的集合:")
        for i, col_name in enumerate(collections, 1):
            print(f"  {i}. {col_name}")
        print(f"  0. 退出")
        print("─" * 80)

        try:
            choice = input("\n请输入选项: ").strip()

            if choice == "0":
                print("\n👋 再见！")
                break

            idx = int(choice) - 1
            if 0 <= idx < len(collections):
                collection_name = collections[idx]

                # 显示该集合的所有信息
                show_collection_schema(collection_name)
                show_collection_indexes(collection_name)
                show_collection_stats(collection_name)
                show_sample_data(collection_name, limit=3)

                input("\n按 Enter 键继续...")
            else:
                print("❌ 无效的选项")

        except ValueError:
            print("❌ 请输入数字")
        except KeyboardInterrupt:
            print("\n\n👋 再见！")
            break
        except Exception as e:
            print(f"❌ 发生错误: {e}")

    # 断开连接
    connections.disconnect("default")
    logger.info("✅ 已断开连接")


def show_all_collections():
    """显示所有集合的信息（非交互模式）"""
    print("\n" + "╔" + "=" * 78 + "╗")
    print("║" + " " * 78 + "║")
    print("║" + "  🔍 Milvus 向量数据库完整结构".center(78) + "║")
    print("║" + " " * 78 + "║")
    print("╚" + "=" * 78 + "╝")

    if not connect_milvus():
        return

    collections = list_collections()

    if not collections:
        print("\n⚠️  数据库为空")
        return

    # 显示每个集合的详细信息
    for collection_name in collections:
        show_collection_schema(collection_name)
        show_collection_indexes(collection_name)
        show_collection_stats(collection_name)
        show_sample_data(collection_name, limit=2)
        print("\n" + "=" * 80 + "\n")

    connections.disconnect("default")


if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1 and sys.argv[1] == "all":
        # 显示所有集合信息
        show_all_collections()
    else:
        # 交互式模式
        interactive_mode()

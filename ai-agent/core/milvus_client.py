"""
Milvus 向量数据库客户端
管理连接、集合创建、向量存储和检索
"""

import logging
from typing import List, Dict, Any, Optional
from pymilvus import (
    connections,
    utility,
    Collection,
    FieldSchema,
    CollectionSchema,
    DataType,
)

logger = logging.getLogger(__name__)


class MilvusClient:
    """Milvus 向量数据库客户端"""

    def __init__(
        self,
        host: str = "127.0.0.1",
        port: int = 19530,
        alias: str = "default",
        collection_name: str = "knowledge_base",
    ):
        """
        初始化 Milvus 客户端

        Args:
            host: Milvus 服务器地址
            port: Milvus 服务器端口
            alias: 连接别名
            collection_name: 集合名称
        """
        self.host = host
        self.port = port
        self.alias = alias
        self.collection_name = collection_name
        self.collection = None
        self._connect()

    def _connect(self):
        """连接到 Milvus 服务器"""
        try:
            # 检查连接是否已存在
            if not connections.has_connection(self.alias):
                connections.connect(
                    alias=self.alias,
                    host=self.host,
                    port=self.port,
                    timeout=30,
                )
                logger.info(
                    f"✅ 已连接到 Milvus: {self.host}:{self.port} (alias={self.alias})"
                )
            else:
                logger.info(f"📌 已使用现有连接: {self.alias}")

            # 列出所有集合
            collections = utility.list_collections(using=self.alias)
            logger.info(f"当前 Milvus 中的集合: {collections}")

        except Exception as e:
            logger.error(f"❌ 连接 Milvus 失败: {e}")
            raise

    def create_collection_if_not_exists(
        self,
        collection_name: str = None,
        vector_dim: int = 384,
        similarity_metric: str = "IP",
    ):
        """
        创建集合（如果不存在）

        Args:
            collection_name: 集合名称，如果为 None 则使用初始化时的名称
            vector_dim: 向量维度（默认 384 用于 sentence-transformers）
            similarity_metric: 相似度计算方式 (L2, IP, COSINE)
        """
        col_name = collection_name or self.collection_name

        try:
            # 检查集合是否已存在
            if utility.has_collection(col_name, using=self.alias):
                logger.info(f"✅ 集合已存在: {col_name}")
                self.collection = Collection(col_name, using=self.alias)
                return

            logger.info(f"正在创建集合: {col_name}")

            # 定义字段
            fields = [
                FieldSchema(name="id", dtype=DataType.INT64, is_primary=True),
                FieldSchema(name="text", dtype=DataType.VARCHAR, max_length=65535),
                FieldSchema(
                    name="embedding", dtype=DataType.FLOAT_VECTOR, dim=vector_dim
                ),
                FieldSchema(name="source", dtype=DataType.VARCHAR, max_length=1024),
                FieldSchema(name="timestamp", dtype=DataType.INT64),
            ]

            # 定义集合模式
            schema = CollectionSchema(
                fields=fields,
                description=f"知识库向量集合 - {col_name}",
                enable_dynamic_field=True,
            )

            # 创建集合
            self.collection = Collection(name=col_name, schema=schema, using=self.alias)

            # 创建索引以加速搜索
            index_params = {
                "metric_type": similarity_metric,
                "index_type": "IVF_FLAT",
                "params": {"nlist": 128},
            }

            self.collection.create_index(
                field_name="embedding", index_params=index_params
            )
            logger.info(
                f"✅ 集合创建成功: {col_name} (维度: {vector_dim}, 相似度: {similarity_metric})"
            )

        except Exception as e:
            logger.error(f"❌ 创建集合失败: {e}")
            raise

    def insert_vectors(
        self,
        texts: List[str],
        embeddings: List[List[float]],
        sources: List[str] = None,
        batch_size: int = 1000,
    ) -> List[int]:
        """
        插入向量数据

        Args:
            texts: 文本列表
            embeddings: 向量列表 (每个向量应该是 float list)
            sources: 来源列表
            batch_size: 批量插入的大小

        Returns:
            插入的 ID 列表
        """
        if not self.collection:
            logger.error("集合未初始化，请先调用 create_collection_if_not_exists")
            return []

        try:
            import time

            all_ids = []

            # 分批插入
            for i in range(0, len(texts), batch_size):
                batch_texts = texts[i : i + batch_size]
                batch_embeddings = embeddings[i : i + batch_size]
                batch_sources = (
                    sources[i : i + batch_size]
                    if sources
                    else ["unknown"] * len(batch_texts)
                )

                # 生成 ID
                batch_ids = list(range(i, i + len(batch_texts)))
                timestamp = int(time.time() * 1000)

                data = [
                    batch_ids,
                    batch_texts,
                    batch_embeddings,
                    batch_sources,
                    [timestamp] * len(batch_texts),
                ]

                result = self.collection.insert(data)
                all_ids.extend(result.primary_keys)
                logger.info(
                    f"✅ 插入第 {i // batch_size + 1} 批: {len(batch_texts)} 条数据"
                )

            # 进行 flush 操作确保数据持久化
            self.collection.flush()
            logger.info(f"✅ 总共插入 {len(all_ids)} 条向量数据")

            return all_ids

        except Exception as e:
            logger.error(f"❌ 插入向量失败: {e}")
            return []

    def search(
        self,
        query_embedding: List[float],
        top_k: int = 5,
        expr: str = None,
    ) -> List[Dict[str, Any]]:
        """
        搜索相似向量

        Args:
            query_embedding: 查询向量
            top_k: 返回最相似的前 k 个结果
            expr: 过滤表达式 (可选)

        Returns:
            搜索结果列表，每个结果包含 id, text, source, distance
        """
        if not self.collection:
            # 如果集合未加载，尝试重新加载
            try:
                from pymilvus import Collection

                self.collection = Collection(self.collection_name, using=self.alias)
                logger.info(f"✅ 已重新加载集合: {self.collection_name}")
            except Exception as e:
                logger.error(f"❌ 集合加载失败: {e}")
                return []

        try:
            # 加载集合到内存
            self.collection.load()

            search_params = {"metric_type": "IP", "params": {"nprobe": 10}}

            results = self.collection.search(
                data=[query_embedding],
                anns_field="embedding",
                param=search_params,
                limit=top_k,
                expr=expr,
                output_fields=["id", "text", "source", "timestamp"],
            )

            # 解析结果
            search_results = []
            if results and len(results) > 0:
                for hit in results[0]:
                    search_results.append(
                        {
                            "id": hit.id,
                            "text": hit.entity.get("text", ""),
                            "source": hit.entity.get("source", ""),
                            "distance": hit.distance,
                            "timestamp": hit.entity.get("timestamp", 0),
                        }
                    )

            logger.info(f"✅ 搜索完成，找到 {len(search_results)} 条相似结果")
            return search_results

        except Exception as e:
            logger.error(f"❌ 搜索失败: {e}")
            return []

    def query_top(self, limit: int = 20) -> List[Dict[str, Any]]:
        """直接查询集合前 N 条记录（按内部顺序）"""
        if not self.collection:
            try:
                from pymilvus import Collection

                self.collection = Collection(self.collection_name, using=self.alias)
                logger.info(f"✅ 已重新加载集合: {self.collection_name}")
            except Exception as e:
                logger.error(f"❌ 集合加载失败: {e}")
                return []

        try:
            self.collection.load()
            results = self.collection.query(
                expr="id >= 0",
                output_fields=["id", "text", "source", "timestamp"],
                limit=limit,
            )

            parsed = []
            for item in results:
                parsed.append(
                    {
                        "id": item.get("id"),
                        "text": item.get("text", ""),
                        "source": item.get("source", ""),
                        "timestamp": item.get("timestamp", 0),
                    }
                )

            logger.info(f"✅ 查询前 {limit} 条记录，返回 {len(parsed)} 条")
            return parsed

        except Exception as e:
            logger.error(f"❌ 查询集合记录失败: {e}")
            return []

    def delete_collection(self, collection_name: str = None):
        """删除集合"""
        col_name = collection_name or self.collection_name

        try:
            if utility.has_collection(col_name, using=self.alias):
                utility.drop_collection(col_name, using=self.alias)
                logger.info(f"✅ 集合已删除: {col_name}")
        except Exception as e:
            logger.error(f"❌ 删除集合失败: {e}")

    def get_collection_info(self, collection_name: str = None) -> Dict[str, Any]:
        """获取集合信息"""
        col_name = collection_name or self.collection_name

        try:
            if utility.has_collection(col_name, using=self.alias):
                col = Collection(col_name, using=self.alias)
                num_entities = col.num_entities

                return {
                    "name": col_name,
                    "entities": num_entities,
                    "status": "active",
                }
            else:
                return {"name": col_name, "status": "not_exists"}
        except Exception as e:
            logger.error(f"❌ 获取集合信息失败: {e}")
            return {"name": col_name, "status": "error", "error": str(e)}

    def close(self):
        """关闭连接"""
        try:
            connections.disconnect(alias=self.alias)
            logger.info(f"✅ 已关闭连接: {self.alias}")
        except Exception as e:
            logger.error(f"❌ 关闭连接失败: {e}")


# 全局客户端实例（单例模式）
_milvus_client = None


def get_milvus_client(
    host: str = None,
    port: int = None,
    alias: str = None,
    collection_name: str = None,
) -> MilvusClient:
    """获取或创建全局 Milvus 客户端实例"""
    global _milvus_client

    if _milvus_client is None:
        from config import (
            MILVUS_HOST,
            MILVUS_PORT,
            MILVUS_ALIAS,
            MILVUS_COLLECTION_NAME,
        )

        _milvus_client = MilvusClient(
            host=host or MILVUS_HOST,
            port=port or MILVUS_PORT,
            alias=alias or MILVUS_ALIAS,
            collection_name=collection_name or MILVUS_COLLECTION_NAME,
        )

    return _milvus_client

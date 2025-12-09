"""
基于 Milvus 的 RAG 检索器
支持多个知识库同时查询
"""

import logging
from typing import List, Dict, Any, Optional
from core.milvus_client import MilvusClient
from core.database import get_knowledge_base

logger = logging.getLogger(__name__)


class MilvusRetriever:
    """Milvus 向量数据库检索器"""

    def __init__(self, milvus_host: str = "127.0.0.1", milvus_port: int = 19530):
        """
        初始化 Milvus 检索器

        Args:
            milvus_host: Milvus 服务器地址
            milvus_port: Milvus 服务器端口
        """
        self.milvus_host = milvus_host
        self.milvus_port = milvus_port
        self.embeddings = None
        self._init_embeddings()

    def _init_embeddings(self):
        """初始化嵌入模型"""
        try:
            from pathlib import Path
            from langchain_community.embeddings import HuggingFaceEmbeddings

            # 使用与知识库相同的 e5 模型
            e5_model_path = Path("knowledge/models--intfloat--e5-base-v2")

            if e5_model_path.exists():
                self.embeddings = HuggingFaceEmbeddings(model_name=str(e5_model_path))
                logger.info(f"✅ 嵌入模型加载成功: {e5_model_path}")
            else:
                # 如果本地没有 e5 模型，使用在线的 all-MiniLM-L6-v2
                self.embeddings = HuggingFaceEmbeddings(
                    model_name="sentence-transformers/all-MiniLM-L6-v2"
                )
                logger.info("✅ 已加载 all-MiniLM-L6-v2 模型（在线）")
        except Exception as e:
            logger.warning(f"嵌入模型加载失败，将尝试使用其他方式: {e}")

    def _get_embedding(self, text: str) -> List[float]:
        """获取文本的嵌入向量"""
        if not self.embeddings:
            logger.error("嵌入模型未初始化")
            return []
        try:
            # HuggingFaceEmbeddings 使用 embed_query 方法
            embedding = self.embeddings.embed_query(text)
            return embedding if isinstance(embedding, list) else embedding.tolist()
        except Exception as e:
            logger.error(f"生成嵌入向量失败: {e}")
            return []


class MilvusRetriever:
    """Milvus 向量数据库检索器"""

    def __init__(self, milvus_host: str = "127.0.0.1", milvus_port: int = 19530):
        """
        初始化 Milvus 检索器

        Args:
            milvus_host: Milvus 服务器地址
            milvus_port: Milvus 服务器端口
        """
        self.milvus_host = milvus_host
        self.milvus_port = milvus_port

    def retrieve(
        self,
        query_text: str,
        kb_ids: List[str],
        k: int = 10,
        score_threshold: float = 0.6,
    ) -> List[Dict[str, Any]]:
        """
        从多个知识库中检索相关内容

        Args:
            query_text: 查询文本
            kb_ids: 知识库ID列表，若为空则在所有知识库中查询
            k: 每个知识库返回的Top-K结果数
            score_threshold: 相似度阈值，低于此值的结果将被过滤（默认 0.6）

        Returns:
            过滤后的文档列表，按相似度降序排列，包含来源知识库信息
        """
        if not kb_ids:
            logger.warning("[Milvus检索] 未指定知识库ID，将返回空结果")
            return []

        # 生成查询向量
        query_embedding = self._get_embedding(query_text)
        if not query_embedding:
            logger.error("[Milvus检索] 无法生成查询向量")
            return []

        all_results = []

        # 遍历每个知识库进行检索
        for kb_id in kb_ids:
            try:
                # 从数据库获取知识库信息
                kb_info = get_knowledge_base(kb_id)
                if not kb_info:
                    logger.warning(f"[Milvus检索] 知识库不存在: {kb_id}")
                    continue

                # 获取 Milvus 集合名称
                milvus_collection = kb_info.get("milvus_collection") or kb_id
                logger.info(
                    f"[Milvus检索] 正在查询知识库 {kb_id}，集合名: {milvus_collection}"
                )

                # 连接到 Milvus 并查询
                milvus_client = MilvusClient(
                    host=self.milvus_host,
                    port=self.milvus_port,
                    collection_name=milvus_collection,
                )

                # 执行相似度查询
                docs_with_scores = milvus_client.search(
                    query_embedding=query_embedding, top_k=k
                )

                if not docs_with_scores:
                    logger.info(f"[Milvus检索] 知识库 {kb_id} 未找到相关内容")
                    continue

                # 过滤并整理结果
                for doc_result in docs_with_scores:
                    # 从搜索结果中提取信息
                    doc_id = doc_result.get("id", "")
                    distance = doc_result.get("distance", 1.0)

                    # 相似度转换：distance → similarity
                    # 对于IP（内积）距离，相似度就是距离本身
                    # 对于L2距离，similarity = 1 / (1 + distance)
                    # 这里假设使用 IP 距离
                    similarity = distance if distance is not None else 0

                    # 只保留满足阈值的结果
                    if similarity >= score_threshold:
                        all_results.append(
                            {
                                "doc_id": doc_id,
                                "content": doc_result.get("text", ""),
                                "source": doc_result.get("source", ""),
                                "kb_id": kb_id,
                                "kb_name": kb_info.get("name", ""),
                                "similarity_score": round(similarity, 4),
                            }
                        )

                logger.info(
                    f"[Milvus检索] 知识库 {kb_id} 返回 {len([r for r in all_results if r['kb_id'] == kb_id])} 条结果"
                )

            except Exception as e:
                logger.error(
                    f"[Milvus检索] 查询知识库 {kb_id} 失败: {e}", exc_info=True
                )
                continue

        # 对所有结果按相似度排序
        all_results.sort(key=lambda x: x["similarity_score"], reverse=True)

        logger.info(
            f"[Milvus检索] 查询: '{query_text}' | 知识库数: {len(kb_ids)} | 返回结果数: {len(all_results)}"
        )

        return all_results

    def retrieve_from_single_kb(
        self,
        query_text: str,
        kb_id: str,
        k: int = 10,
        score_threshold: float = 0.6,
    ) -> List[Dict[str, Any]]:
        """
        从单个知识库检索（便捷方法）

        Args:
            query_text: 查询文本
            kb_id: 知识库ID
            k: Top-K结果数
            score_threshold: 相似度阈值

        Returns:
            检索结果列表
        """
        return self.retrieve(
            query_text=query_text,
            kb_ids=[kb_id],
            k=k,
            score_threshold=score_threshold,
        )

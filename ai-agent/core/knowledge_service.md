"""
知识库服务 - 处理文档上传、chunking、向量化和存储
"""

import os
import hashlib
import logging
from pathlib import Path
from typing import List, Dict, Optional, Any
from datetime import datetime
import json

from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.embeddings import FastEmbedEmbeddings

from core.milvus_client import MilvusClient
from core.database import get_session, KnowledgeBaseDB, get_knowledge_base

logger = logging.getLogger(__name__)


class KnowledgeBaseService:
    """知识库服务类"""

    def __init__(self):
        """初始化服务"""
        # 延迟加载 embedding 模型（只在需要时加载）
        self.embeddings = None
        self.document_root = Path("knowledge/origin_document")
        self.document_root.mkdir(parents=True, exist_ok=True)
        logger.info(f"✅ 知识库服务初始化完成")

    def _ensure_embeddings_loaded(self):
        """确保 embedding 模型已加载（延迟加载）"""
        if self.embeddings is not None:
            return

        logger.info("⏳ 正在加载 embedding 模型 (FastEmbed - 轻量版)...")
        try:
            # 轻量级 ONNX 模型，避免安装 torch
            self.embeddings = FastEmbedEmbeddings(model_name="BAAI/bge-small-zh-v1.5")
            logger.info("✅ 已加载 FastEmbed 模型: BAAI/bge-small-zh-v1.5")
        except Exception as e:
            logger.error(f"❌ 加载 embedding 模型失败: {e}")
            raise

    def generate_kb_id(self, user_id: str, kb_name: str) -> str:
        """
        生成知识库 ID

        Args:
            user_id: 用户 ID
            kb_name: 知识库名称

        Returns:
            知识库 ID
        """
        # 使用 user_id + kb_name 的组合生成唯一 ID
        raw_id = f"{user_id}_{kb_name}"
        kb_id = hashlib.md5(raw_id.encode()).hexdigest()[:16]
        return f"{user_id}_{kb_id}"

    def save_uploaded_files(
        self,
        kb_id: str,
        files: List[Dict[str, Any]],
    ) -> List[str]:
        """
        保存上传的文件到本地

        Args:
            kb_id: 知识库 ID
            files: 文件列表，每个文件包含 filename 和 content

        Returns:
            保存后的文件路径列表
        """
        kb_dir = self.document_root / kb_id
        kb_dir.mkdir(parents=True, exist_ok=True)

        saved_paths = []

        for file_info in files:
            filename = file_info.get("filename")
            content = file_info.get("content")

            if not filename or content is None:
                logger.warning(f"⚠️  文件信息不完整，跳过")
                continue

            # 保存文件
            file_path = kb_dir / filename
            try:
                # 如果 content 是字节流，直接写入；如果是字符串，按文本写入
                if isinstance(content, bytes):
                    with open(file_path, "wb") as f:
                        f.write(content)
                else:
                    with open(file_path, "w", encoding="utf-8") as f:
                        f.write(content)

                saved_paths.append(str(file_path))
                logger.info(f"✅ 已保存文件: {file_path}")
            except Exception as e:
                logger.error(f"❌ 保存文件失败 {filename}: {e}")

        return saved_paths

    def load_document_content(self, file_path: str) -> str:
        """
        加载文档内容

        Args:
            file_path: 文件路径

        Returns:
            文档内容
        """
        file_path = Path(file_path)

        if not file_path.exists():
            logger.error(f"❌ 文件不存在: {file_path}")
            return ""

        try:
            # 根据文件类型加载内容
            suffix = file_path.suffix.lower()

            if suffix == ".txt":
                with open(file_path, "r", encoding="utf-8") as f:
                    return f.read()

            elif suffix == ".md":
                with open(file_path, "r", encoding="utf-8") as f:
                    return f.read()

            elif suffix == ".pdf":
                # TODO: 实现 PDF 解析
                logger.warning(f"⚠️  暂不支持 PDF 格式: {file_path}")
                return ""

            else:
                logger.warning(f"⚠️  不支持的文件格式: {suffix}")
                return ""

        except Exception as e:
            logger.error(f"❌ 加载文件失败 {file_path}: {e}")
            return ""

    def chunk_documents(
        self,
        file_paths: List[str],
        chunking_method: str = "recursive",
        chunk_size: int = 1000,
        chunk_overlap: int = 200,
    ) -> List[Dict[str, Any]]:
        """
        对文档进行 chunking

        Args:
            file_paths: 文件路径列表
            chunking_method: chunking 方式
            chunk_size: chunk 大小
            chunk_overlap: chunk 重叠

        Returns:
            chunk 列表，每个包含 text, source, chunk_idx
        """
        # 根据 chunking_method 选择分割器
        # 支持的方式: recursive, fixed, sentence, token, markdown
        if chunking_method == "recursive":
            # 递归字符分割（推荐，智能处理段落、句子）
            splitter = RecursiveCharacterTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
                length_function=len,
            )
        elif chunking_method == "fixed":
            # 固定字符分割（按换行符）
            from langchain_text_splitters import CharacterTextSplitter

            splitter = CharacterTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
                separator="\n",
            )
        elif chunking_method == "sentence":
            # 按句子分割（适合对话、问答）
            from langchain_text_splitters import SentenceTransformersTokenTextSplitter

            splitter = SentenceTransformersTokenTextSplitter(
                chunk_overlap=chunk_overlap,
                tokens_per_chunk=chunk_size // 4,  # 转换为 token 数
            )
        elif chunking_method == "token":
            # 按 token 分割（精确控制 token 数量）
            from langchain_text_splitters import TokenTextSplitter

            splitter = TokenTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
            )
        elif chunking_method == "markdown":
            # Markdown 文档专用分割（保留结构）
            from langchain_text_splitters import MarkdownTextSplitter

            splitter = MarkdownTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
            )
        else:
            # 默认使用 recursive
            logger.warning(
                f"⚠️  未知的 chunking 方式: {chunking_method}，使用默认 recursive"
            )
            splitter = RecursiveCharacterTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
                length_function=len,
            )

        all_chunks = []

        for file_path in file_paths:
            content = self.load_document_content(file_path)
            if not content:
                continue

            try:
                chunks = splitter.split_text(content)
                for chunk_idx, chunk in enumerate(chunks):
                    all_chunks.append(
                        {
                            "text": chunk,
                            "source": str(Path(file_path).name),
                            "file_path": str(file_path),
                            "chunk_idx": chunk_idx,
                        }
                    )
                logger.info(f"✅ {Path(file_path).name} 分割为 {len(chunks)} 个 chunks")
            except Exception as e:
                logger.error(f"❌ 分割文件失败 {file_path}: {e}")

        return all_chunks

    def create_knowledge_base(
        self,
        user_id: str,
        name: str,
        files: List[Dict[str, Any]],
        description: str = "",
        chunking_method: str = "recursive",
        chunk_size: int = 1000,
        chunk_overlap: int = 200,
        enabled: bool = True,
    ) -> Dict[str, Any]:
        """
        创建知识库

        Args:
            user_id: 用户 ID
            name: 知识库名称
            files: 上传的文件列表
            description: 知识库描述
            chunking_method: chunking 方式
            chunk_size: chunk 大小
            chunk_overlap: chunk 重叠
            enabled: 是否启用

        Returns:
            创建结果
        """
        try:
            # 1. 生成知识库 ID
            kb_id = self.generate_kb_id(user_id, name)
            logger.info(f"������ 创建知识库: {kb_id}")

            # 2. 保存上传的文件
            file_paths = self.save_uploaded_files(kb_id, files)
            if not file_paths:
                return {"success": False, "error": "没有成功保存任何文件"}

            # 3. 对文档进行 chunking
            chunks = self.chunk_documents(
                file_paths,
                chunking_method=chunking_method,
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
            )

            if not chunks:
                return {
                    "success": False,
                    "error": "文档 chunking 失败，没有生成任何 chunk",
                }

            logger.info(f"✅ 总共生成 {len(chunks)} 个 chunks")

            # 4. 生成 embeddings
            self._ensure_embeddings_loaded()
            texts = [chunk["text"] for chunk in chunks]
            embeddings = self.embeddings.embed_documents(texts)
            logger.info(f"✅ 已生成 {len(embeddings)} 个向量")

            # 5. 创建 Milvus 集合并存储向量
            milvus_client = MilvusClient(collection_name=kb_id)

            # 创建集合
            milvus_client.create_collection_if_not_exists(
                collection_name=kb_id,
                vector_dim=len(embeddings[0]),
                similarity_metric="IP",
            )

            # 插入向量
            sources = [chunk["source"] for chunk in chunks]
            ids = milvus_client.insert_vectors(
                texts=texts,
                embeddings=embeddings,
                sources=sources,
                batch_size=100,
            )

            logger.info(f"✅ 已存储 {len(ids)} 个向量到 Milvus 集合: {kb_id}")

            # 6. 保存知识库信息到数据库
            with get_session() as session:
                kb = KnowledgeBaseDB(
                    kb_id=kb_id,
                    user_id=user_id,
                    name=name,
                    description=description,
                    file_paths=file_paths,
                    chunking_method=chunking_method,
                    chunk_size=chunk_size,
                    chunk_overlap=chunk_overlap,
                    total_chunks=len(chunks),
                    milvus_collection=kb_id,
                    enabled=enabled,
                    created_at=datetime.now().isoformat(),
                    updated_at=datetime.now().isoformat(),
                )
                session.add(kb)
                session.commit()
                logger.info(f"✅ 知识库信息已保存到数据库")

            milvus_client.close()

            return {
                "success": True,
                "kb_id": kb_id,
                "name": name,
                "total_files": len(file_paths),
                "total_chunks": len(chunks),
                "milvus_collection": kb_id,
            }

        except Exception as e:
            logger.error(f"❌ 创建知识库失败: {e}")
            return {"success": False, "error": str(e)}

    def query_knowledge_base(
        self,
        kb_id: str,
        query: str,
        top_k: int = 5,
        similarity_threshold: float = 0.7,
    ) -> List[Dict[str, Any]]:
        """
        查询知识库

        Args:
            kb_id: 知识库 ID
            query: 查询文本
            top_k: 返回结果数量
            similarity_threshold: 相似度阈值

        Returns:
            查询结果列表
        """
        try:
            # 生成查询向量
            self._ensure_embeddings_loaded()
            query_embedding = self.embeddings.embed_query(query)

            # 从 Milvus 搜索
            milvus_client = MilvusClient(collection_name=kb_id)
            results = milvus_client.search(query_embedding, top_k=top_k * 2)

            # 过滤相似度
            filtered_results = []
            for result in results:
                similarity = min(result["distance"], 1.0)
                if similarity >= similarity_threshold:
                    filtered_results.append({**result, "similarity": similarity})

            milvus_client.close()

            # 排序并限制数量
            filtered_results.sort(key=lambda x: x["similarity"], reverse=True)
            return filtered_results[:top_k]

        except Exception as e:
            logger.error(f"❌ 查询知识库失败: {e}")
            return []

    def search_similar_content(
        self,
        query_text: str,
        kb_id: str = None,
        user_id: str = None,
        limit: int = 5,
        similarity_threshold: float = 0.0,
    ) -> List[Dict[str, Any]]:
        """
        搜索相似内容
        - kb_id: 指定知识库搜索
        - user_id: 用户ID（若kb_id为空，则跨该用户所有知识库搜索）
        """
        try:
            from core.database import list_knowledge_bases, get_knowledge_base
            from core.milvus_retriever import MilvusRetriever

            # 1. 确定搜索的知识库
            if kb_id:
                # 指定知识库搜索
                kb_info = get_knowledge_base(kb_id)
                if not kb_info:
                    logger.warning(f"知识库不存在: {kb_id}")
                    return []
                kb_ids = [kb_id]
                kb_name_map = {kb_id: kb_info["name"]}
            elif user_id:
                # 用户的所有知识库
                kbs = list_knowledge_bases(enabled_only=True)
                kbs = [kb for kb in kbs if kb.get("user_id") == user_id]
                if not kbs:
                    logger.warning(f"用户 {user_id} 没有启用的知识库")
                    return []
                kb_ids = [kb["kb_id"] for kb in kbs]
                kb_name_map = {kb["kb_id"]: kb["name"] for kb in kbs}
            else:
                logger.warning("必须指定 kb_id 或 user_id")
                return []

            logger.info(
                f"������ 在 {len(kb_ids)} 个知识库中搜索前 {limit} 条: {query_text[:50]}..."
            )

            retriever = MilvusRetriever()

            # 只取前 top_k（默认 5）
            results = retriever.retrieve(
                query_text=query_text,
                kb_ids=kb_ids,
                k=limit,
                score_threshold=0.0,
            )

            # 添加知识库名称
            for result in results:
                result_kb_id = result.get("kb_id")
                result["kb_name"] = kb_name_map.get(result_kb_id, "未知知识库")

            # 阈值过滤（保留满足阈值的全部，最多 limit 条）
            filtered = [
                r
                for r in results
                if r.get("similarity_score", 0) >= similarity_threshold
            ]
            logger.info(
                f"✅ 阈值 {similarity_threshold}, 原始 {len(results)} 条，过滤后 {len(filtered)} 条"
            )

            return filtered[:limit]

        except Exception as e:
            logger.error(f"❌ 搜索相似内容失败: {e}", exc_info=True)
            return []

    def get_top_chunks(
        self, kb_id: str = None, user_id: str = None, limit: int = 20
    ) -> List[Dict[str, Any]]:
        """
        无 query 时返回前 N 条 chunk
        - kb_id: 指定知识库（返回该知识库的前 N 条）
        - user_id: 用户ID（若kb_id为空，返回用户所有知识库的前 N 条）
        """
        try:
            from core.database import list_knowledge_bases, get_knowledge_base

            # 1. 确定查询的知识库
            if kb_id:
                # 指定知识库
                kb_info = get_knowledge_base(kb_id)
                if not kb_info:
                    logger.warning(f"知识库不存在: {kb_id}")
                    return []
                kbs = [kb_info]
            elif user_id:
                # 用户的所有知识库
                kbs = list_knowledge_bases(enabled_only=True)
                kbs = [kb for kb in kbs if kb.get("user_id") == user_id]
                if not kbs:
                    logger.warning(f"用户 {user_id} 没有启用的知识库")
                    return []
            else:
                logger.warning("必须指定 kb_id 或 user_id")
                return []

            remaining = limit
            results: List[Dict[str, Any]] = []

            for kb in kbs:
                if remaining <= 0:
                    break

                take = remaining
                milvus_client = MilvusClient(collection_name=kb["kb_id"])
                try:
                    top_chunks = milvus_client.query_top(limit=take)
                    for item in top_chunks:
                        item["kb_id"] = kb["kb_id"]
                        item["kb_name"] = kb["name"]
                    results.extend(top_chunks)
                    remaining = max(0, remaining - len(top_chunks))
                finally:
                    milvus_client.close()

            logger.info(f"✅ 无 query，返回 {len(results)} 条 chunk (limit={limit})")
            return results[:limit]

        except Exception as e:
            logger.error(f"❌ 获取 top chunk 失败: {e}", exc_info=True)
            return []

    def rebuild_knowledge_base(
        self,
        kb_id: str,
        files: Optional[List[Dict[str, Any]]] = None,
        chunking_method: Optional[str] = None,
        chunk_size: Optional[int] = None,
        chunk_overlap: Optional[int] = None,
        name: Optional[str] = None,
        description: Optional[str] = None,
        enabled: Optional[bool] = None,
    ) -> Dict[str, Any]:
        """
        重构知识库向量

        当文件内容、chunking 方式或参数发生变化时，需要重新向量化

        Args:
            kb_id: 知识库 ID
            files: 新的文件列表（可选，如果提供则替换旧文件）
            chunking_method: 新的 chunking 方式
            chunk_size: 新的 chunk 大小
            chunk_overlap: 新的 chunk 重叠
            name: 新的知识库名称（可选）
            description: 新的描述（可选）
            enabled: 新的启用状态（可选）

        Returns:
            重构结果
        """
        try:
            from core.database import get_knowledge_base, update_knowledge_base

            # 1. 获取现有知识库信息
            kb = get_knowledge_base(kb_id)

            if not kb:
                return {"success": False, "error": f"知识库不存在: {kb_id}"}

            logger.info(f"������ 开始重构知识库: {kb_id}")

            # 2. 确定新的参数（如果未提供则使用旧值）
            new_chunking_method = chunking_method or kb.chunking_method
            new_chunk_size = chunk_size or kb.chunk_size
            new_chunk_overlap = chunk_overlap or kb.chunk_overlap

            # 3. 处理文件
            if files is not None:
                # 有新文件：删除旧文件，保存新文件
                old_dir = self.document_root / kb_id
                if old_dir.exists():
                    import shutil

                    shutil.rmtree(old_dir)
                    logger.info(f"������️  已删除旧文件目录")

                file_paths = self.save_uploaded_files(kb_id, files)
                if not file_paths:
                    return {"success": False, "error": "没有成功保存任何文件"}
            else:
                # 无新文件：使用旧文件路径
                file_paths = (
                    kb.file_paths
                    if isinstance(kb.file_paths, list)
                    else json.loads(kb.file_paths or "[]")
                )

            # 4. 重新 chunk 文档
            chunks = self.chunk_documents(
                file_paths,
                chunking_method=new_chunking_method,
                chunk_size=new_chunk_size,
                chunk_overlap=new_chunk_overlap,
            )

            if not chunks:
                return {"success": False, "error": "文档 chunking 失败"}

            logger.info(f"✅ 重新生成 {len(chunks)} 个 chunks")

            # 5. 生成新的 embeddings
            self._ensure_embeddings_loaded()
            texts = [chunk["text"] for chunk in chunks]
            embeddings = self.embeddings.embed_documents(texts)
            logger.info(f"✅ 已生成 {len(embeddings)} 个向量")

            # 6. 删除旧的 Milvus 集合
            old_milvus_collection = kb.milvus_collection
            milvus_client = MilvusClient(collection_name=old_milvus_collection)
            try:
                milvus_client.drop_collection()
                logger.info(f"������️  已删除旧的 Milvus 集合: {old_milvus_collection}")
            except Exception as e:
                logger.warning(f"⚠️  删除旧集合失败（可能不存在）: {e}")

            # 7. 创建新的 Milvus 集合并存储向量
            milvus_client.create_collection_if_not_exists(
                collection_name=kb_id,
                vector_dim=len(embeddings[0]),
                similarity_metric="IP",
            )

            sources = [chunk["source"] for chunk in chunks]
            ids = milvus_client.insert_vectors(
                texts=texts,
                embeddings=embeddings,
                sources=sources,
                batch_size=100,
            )

            logger.info(f"✅ 已存储 {len(ids)} 个向量到新集合: {kb_id}")
            milvus_client.close()

            # 8. 更新数据库记录
            updates = {
                "file_paths": file_paths,
                "chunking_method": new_chunking_method,
                "chunk_size": new_chunk_size,
                "chunk_overlap": new_chunk_overlap,
                "total_chunks": len(chunks),
                "milvus_collection": kb_id,
                "updated_at": datetime.now().isoformat(),
            }

            # 添加可选更新字段
            if name is not None:
                updates["name"] = name
            if description is not None:
                updates["description"] = description
            if enabled is not None:
                updates["enabled"] = enabled

            with get_session() as session:
                update_knowledge_base(session, kb_id, **updates)

            logger.info(f"✅ 知识库重构完成: {kb_id}")

            return {
                "success": True,
                "kb_id": kb_id,
                "total_files": len(file_paths),
                "total_chunks": len(chunks),
                "message": "知识库重构成功",
            }

        except Exception as e:
            logger.error(f"❌ 重构知识库失败: {e}", exc_info=True)
            return {"success": False, "error": str(e)}

    def delete_knowledge_base(self, kb_id: str) -> Dict[str, Any]:
        """
        删除知识库

        同时删除 Milvus 集合、文件目录和数据库记录

        Args:
            kb_id: 知识库 ID

        Returns:
            删除结果
        """
        try:
            from core.database import delete_knowledge_base as db_delete_kb

            logger.info(f"{'='*60}")
            logger.info(f"������ 开始执行删除知识库操作")
            logger.info(f"{'='*60}")
            logger.info(f"   kb_id: {kb_id}")

            logger.info(f"������ 第 1 步: 从数据库查询知识库信息")

            # 1. 从数据库获取知识库信息（在 session 内获取所有属性）
            with get_session() as session:
                logger.info(f"   - 已打开数据库 session")
                kb = (
                    session.query(KnowledgeBaseDB)
                    .filter(KnowledgeBaseDB.kb_id == kb_id)
                    .first()
                )
                logger.info(f"   - 查询完成, kb 对象: {kb}")

                if not kb:
                    logger.warning(f"   ⚠️  知识库不存在: {kb_id}")
                    return {"success": False, "error": f"知识库不存在: {kb_id}"}

                # 在 session 内获取所有需要的属性值
                milvus_collection = kb.milvus_collection
                logger.info(f"   ✅ 获取成功:")
                logger.info(f"      - kb_id: {kb.kb_id}")
                logger.info(f"      - name: {kb.name}")
                logger.info(f"      - milvus_collection: {milvus_collection}")
                logger.info(f"      - total_chunks: {kb.total_chunks}")

            logger.info(f"������ 第 2 步: 删除 Milvus 集合")

            # 2. 删除 Milvus 集合
            milvus_client = MilvusClient(collection_name=milvus_collection)
            try:
                logger.info(f"   - 准备删除集合: {milvus_collection}")
                milvus_client.delete_collection()
                logger.info(f"   ✅ 已删除 Milvus 集合: {milvus_collection}")
            except Exception as e:
                logger.warning(f"   ⚠️  删除 Milvus 集合失败: {e}")
            finally:
                milvus_client.close()
                logger.info(f"   - Milvus 客户端已关闭")

            logger.info(f"������ 第 3 步: 删除文件目录")

            # 3. 删除文件目录
            kb_dir = self.document_root / kb_id
            if kb_dir.exists():
                import shutil

                logger.info(f"   - 目录存在: {kb_dir}")
                shutil.rmtree(kb_dir)
                logger.info(f"   ✅ 已删除文件目录: {kb_dir}")
            else:
                logger.info(f"   ⚠️  文件目录不存在: {kb_dir}")

            logger.info(f"������ 第 4 步: 删除数据库记录")

            # 4. 删除数据库记录
            logger.info(f"   - 调用数据库删除函数")
            ok = db_delete_kb(kb_id)
            logger.info(f"   - 删除结果: {ok}")

            if not ok:
                logger.warning(f"   ⚠️  删除数据库记录失败或记录不存在: {kb_id}")
            else:
                logger.info(f"   ✅ 数据库记录已删除")

            logger.info(f"{'='*60}")
            logger.info(f"✅ 知识库删除完成: {kb_id}")
            logger.info(f"{'='*60}")

            return {
                "success": True,
                "message": "知识库删除成功",
            }

        except Exception as e:
            logger.error(f"{'='*60}")
            logger.error(f"❌ 删除知识库失败: {e}", exc_info=True)
            logger.error(f"{'='*60}")
            return {"success": False, "error": str(e)}


# 全局服务实例
_kb_service = None


def get_kb_service() -> KnowledgeBaseService:
    """获取知识库服务单例"""
    global _kb_service
    if _kb_service is None:
        _kb_service = KnowledgeBaseService()
    return _kb_service


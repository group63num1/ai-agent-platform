"""
LangChain Agent with API calling and RAG capabilities (core)
"""

import json
import re
import requests
from typing import List, Dict, Any, Optional
from pathlib import Path
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage


class SimpleOpenAILLM(BaseChatModel):
    api_key: str
    base_url: str
    model: str
    streaming: bool = False
    _bound_tools: List[Any] = []

    def __init__(
        self,
        api_key: str,
        base_url: str = None,
        model: str = None,
        streaming: bool = False,
    ):
        if base_url is None or model is None:
            raise ValueError("必须提供 api_key, base_url 和 model 参数")
        super().__init__(
            api_key=api_key, base_url=base_url, model=model, streaming=streaming
        )

    @property
    def _llm_type(self) -> str:
        return "simple_openai_chat"

    def bind_tools(self, tools: list, **kwargs: Any) -> "SimpleOpenAILLM":
        bound = self.__class__(
            api_key=self.api_key,
            base_url=self.base_url,
            model=self.model,
            streaming=self.streaming,
        )
        bound._bound_tools = tools
        return bound

    def _generate(
        self,
        messages: List[BaseMessage],
        stop: Optional[List[str]] = None,
        run_manager: Optional[Any] = None,
        **kwargs: Any,
    ) -> Any:
        from langchain_core.outputs import ChatResult, ChatGeneration

        api_messages = []
        for msg in messages:
            if isinstance(msg, HumanMessage):
                api_messages.append({"role": "user", "content": msg.content})
            elif isinstance(msg, AIMessage):
                api_messages.append({"role": "assistant", "content": msg.content})
            else:
                api_messages.append({"role": "user", "content": str(msg.content)})
        try:
            headers = {
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
            }
            data = {
                "model": self.model,
                "messages": api_messages,
                "max_tokens": 1000,
                "temperature": 0.7,
                "stream": self.streaming,
            }
            if hasattr(self, "_bound_tools") and self._bound_tools:
                tools_schema = []
                for tool in self._bound_tools:
                    tools_schema.append(
                        {
                            "type": "function",
                            "function": {
                                "name": tool.name,
                                "description": tool.description,
                                "parameters": {
                                    "type": "object",
                                    "properties": {},
                                    "required": [],
                                },
                            },
                        }
                    )
                data["tools"] = tools_schema
            response = requests.post(
                f"{self.base_url}/chat/completions",
                headers=headers,
                json=data,
                timeout=30,
                stream=self.streaming,
            )
            response.raise_for_status()
            if self.streaming:
                content = ""
                for line in response.iter_lines():
                    if line:
                        line = line.decode("utf-8")
                        if line.startswith("data: "):
                            line = line[6:]
                        if line == "[DONE]":
                            break
                        try:
                            chunk = json.loads(line)
                            if "choices" in chunk and len(chunk["choices"]) > 0:
                                delta = chunk["choices"][0].get("delta", {})
                                if "content" in delta:
                                    content += delta["content"]
                                    print(delta["content"], end="", flush=True)
                        except json.JSONDecodeError:
                            continue
                message = AIMessage(content=content)
            else:
                result = response.json()
                choice = result["choices"][0]
                msg = choice["message"]
                if msg.get("tool_calls"):
                    message = AIMessage(
                        content=msg.get("content", ""),
                        additional_kwargs={"tool_calls": msg["tool_calls"]},
                    )
                else:
                    content = msg.get("content", "")
                    message = AIMessage(content=content)
            generation = ChatGeneration(message=message)
            return ChatResult(generations=[generation])
        except Exception as e:
            from langchain_core.outputs import ChatResult, ChatGeneration

            message = AIMessage(content=f"API调用失败: {str(e)}")
            generation = ChatGeneration(message=message)
            return ChatResult(generations=[generation])


class RAGRetriever:
    def __init__(
        self,
        txt_corpus_path: str,
        chunk_size: int = None,
        chunk_overlap: int = None,
        retrieval_k: int = None,
    ):
        import config

        self.txt_corpus_path = txt_corpus_path
        self.chunk_size = chunk_size or config.CHUNK_SIZE
        self.chunk_overlap = chunk_overlap or config.CHUNK_OVERLAP
        self.retrieval_k = retrieval_k or config.RETRIEVAL_K
        # 轻量版：仅使用文本切分和简单关键字匹配，不依赖向量数据库/大模型嵌入
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            length_function=len,
        )
        self._chunks: List[Document] = self._build_corpus()

    def _load_txt_files(self) -> List[Document]:
        """加载语料库中的 txt 文件，构造成 Document 列表。"""
        documents: List[Document] = []
        corpus_path = Path(self.txt_corpus_path)
        if not corpus_path.exists():
            print(f"语料库路径不存在: {self.txt_corpus_path}")
            return documents
        for txt_file in corpus_path.rglob("*.txt"):
            try:
                with open(txt_file, "r", encoding="utf-8") as f:
                    content = f.read()
                    documents.append(
                        Document(
                            page_content=content, metadata={"source": str(txt_file)}
                        )
                    )
                    print(f"加载文件: {txt_file}")
            except Exception as e:
                print(f"加载文件失败 {txt_file}: {e}")
        return documents

    def _build_corpus(self) -> List[Document]:
        """构建轻量级语料切片列表（无向量库、无嵌入）。"""
        print("正在构建轻量级 RAG 语料...")
        documents = self._load_txt_files()
        if not documents:
            print("未找到任何 txt 文件，将返回空检索结果")
            return []
        split_docs = self.text_splitter.split_documents(documents)
        print(f"文档分割完成，共 {len(split_docs)} 个片段")
        return split_docs

    def retrieve(self, query: str, k: int = None) -> List[Dict[str, Any]]:
        """基于简单关键字匹配的轻量版检索，实现快速无依赖的 RAG。"""
        try:
            k = k or self.retrieval_k
            if not self._chunks:
                return []

            query_lower = query.lower()
            scored: List[Dict[str, Any]] = []
            for d in self._chunks:
                text = d.page_content or ""
                text_lower = text.lower()
                if not text_lower:
                    continue
                # 简单打分：包含次数 + 覆盖比例
                count = text_lower.count(query_lower) if query_lower else 0
                coverage = len(query_lower) / max(len(text_lower), 1) if query_lower else 0
                score = count + coverage
                if score > 0:
                    scored.append(
                        {
                            "content": text,
                            "source": d.metadata.get("source", "unknown"),
                            "score": score,
                        }
                    )

            # 根据分数排序，取前 k 条
            scored.sort(key=lambda x: x["score"], reverse=True)
            top = scored[:k]
            return [
                {
                    "content": item["content"],
                    "source": item["source"],
                    "relevance_score": item["score"],
                }
                for item in top
            ]
        except Exception as e:
            print(f"检索失败: {e}")
            return []


class LangChainAgent:
    def __init__(
        self,
        api_key: str,
        txt_corpus_path: str = None,
        base_url: str = None,
        model: str = None,
    ):
        if txt_corpus_path is None:
            from config import CORPUS_PATH

            txt_corpus_path = CORPUS_PATH

        if base_url is None or model is None:
            raise ValueError("必须提供 api_key, base_url 和 model 参数")

        self.llm = SimpleOpenAILLM(
            api_key=api_key, base_url=base_url, model=model, streaming=True
        )
        self.rag_retriever = RAGRetriever(txt_corpus_path)
        self.tools = {}  # 工具映射
        self.tool_map = {}  # 工具映射
        self.history: List[Dict[str, str]] = []
        self.summary: Optional[str] = None

        # 从配置加载参数
        import config

        self.max_rounds: int = config.MAX_HISTORY_LENGTH
        self.summary_trigger: int = config.SUMMARY_TRIGGER
        self.max_iterations: int = config.MAX_ITERATIONS

    def _format_recent_history(self) -> str:
        recent = self.history[-self.max_rounds :]
        return "\n".join(
            [
                ("用户" if t["role"] == "user" else "助手") + f": {t['content']}"
                for t in recent
            ]
        )

    def _maybe_summarize(self):
        if len(self.history) < self.summary_trigger:
            return
        if self.summary and len(self.history) < self.summary_trigger * 2:
            return
        summary_llm = SimpleOpenAILLM(
            api_key=self.llm.api_key,
            base_url=self.llm.base_url,
            model=self.llm.model,
            streaming=False,
        )
        raw_text = self._format_recent_history()
        prompt = (
            "请总结以下对话的关键信息(用户需求,偏好,未解决问题,上下文主题),用简洁要点:\n"
            + raw_text
        )
        try:
            result = summary_llm._generate([HumanMessage(content=prompt)])
            self.summary = result.generations[0].message.content.strip()
            self.history = self.history[-4:]
        except Exception:
            pass

    def chat(
        self,
        user_input: str,
        history: Optional[List[Dict[str, str]]] = None,
        enable_rag: bool = False,
        enable_plugins: bool = False,
        allowed_plugins: Optional[List[str]] = None,
    ) -> str:
        try:
            original_history = None
            if history is not None:
                original_history = self.history
                self.history = history.copy()
            self.history.append({"role": "user", "content": user_input})
            self._maybe_summarize()

            # 使用传入的工具映射（由 agent_service 管理）
            filtered_tools: Dict[str, Any] = {}

            # 如果启用插件，使用 self.tool_map
            if enable_plugins and allowed_plugins:
                for name in allowed_plugins:
                    if name in self.tool_map:
                        filtered_tools[name] = self.tool_map[name]

            # 如果启用 RAG 且 RAG 工具存在
            if enable_rag and "rag_search" in self.tool_map:
                filtered_tools["rag_search"] = self.tool_map["rag_search"]
            tool_descriptions = (
                "当前无可用插件"
                if not filtered_tools
                else (
                    "可用插件列表:\n"
                    + "\n".join(
                        [
                            f"✓ {n}: {filtered_tools[n].description}"
                            for n in filtered_tools
                        ]
                    )
                )
            )
            recent_context = self._format_recent_history()
            summary_block = f"对话摘要:\n{self.summary}\n\n" if self.summary else ""
            decision_prompt = f"""你是一个AI助手,可以使用以下工具来帮助用户:

{tool_descriptions}

{summary_block}最近对话:\n{recent_context}\n\n当前用户最新问题: {user_input}

请分析用户问题,判断是否需要使用工具。如果需要,请按照以下JSON格式回复:
{{"use_tool": true, "tool_name": "工具名称", "tool_input": "工具输入参数"}}

如果不需要使用工具,请按照以下JSON格式回复:
{{"use_tool": false}}

注意:
- weather_query 工具的输入参数应该是城市名(如"北京")
- rag_search 工具的输入参数应该是搜索查询
- news_query 工具的输入参数应该是新闻关键词
- api_call 工具的输入格式为: url|method|headers|data
- iot_plugin 工具的输入格式为: sensor
只返回JSON,不要有其他内容。"""
            decision_llm = SimpleOpenAILLM(
                api_key=self.llm.api_key,
                base_url=self.llm.base_url,
                model=self.llm.model,
                streaming=False,
            )
            decision_response = decision_llm._generate(
                [HumanMessage(content=decision_prompt)]
            )
            decision_text = decision_response.generations[0].message.content.strip()
            print(f"[DEBUG] LLM 决策: {decision_text}")
            try:
                json_match = re.search(r"\{.*\}", decision_text, re.DOTALL)
                if json_match:
                    decision_text = json_match.group(0)
                decision = json.loads(decision_text)
                if decision.get("use_tool", False):
                    tool_name = decision.get("tool_name")
                    tool_input = decision.get("tool_input")
                    if tool_name in filtered_tools:
                        print(f"[DEBUG] 调用工具: {tool_name}({tool_input})")
                        tool_result = filtered_tools[tool_name].invoke(tool_input)
                        if tool_name == "rag_search":
                            final_prompt = f"基于以下检索信息回答用户问题:\n\n{tool_result}\n\n用户问题: {user_input}"
                        else:
                            final_prompt = f"工具返回结果:\n{tool_result}\n\n请基于上述结果用自然语言回复用户。用户问题: {user_input}"
                        final_response = self.llm._generate(
                            [HumanMessage(content=final_prompt)]
                        )
                        answer = final_response.generations[0].message.content
                        self.history.append({"role": "assistant", "content": answer})
                        return answer
                    else:
                        print("[DEBUG] 当前可用工具:", list(filtered_tools.keys()))
                        return f"工具 '{tool_name}' 不存在"
            except json.JSONDecodeError as e:
                print(f"[DEBUG] JSON 解析失败,直接回复: {e}")
            response = self.llm._generate([HumanMessage(content=user_input)])
            answer = response.generations[0].message.content
            self.history.append({"role": "assistant", "content": answer})
            return answer
        except Exception as e:
            return f"Agent执行失败: {str(e)}"
        finally:
            if history is not None and original_history is not None:
                self.history = original_history

"""
LangChain Agent with API calling and RAG capabilities (core)
"""

import json
import re
import requests
import logging
from typing import List, Dict, Any, Optional
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage

logger = logging.getLogger(__name__)


class SimpleOpenAILLM(BaseChatModel):
    api_key: str
    base_url: str
    model: str
    streaming: bool = False
    _bound_tools: List[Any] = []

    # 生成参数 - 声明为可选字段
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None
    top_p: Optional[float] = None
    top_k: Optional[int] = None
    frequency_penalty: Optional[float] = None
    presence_penalty: Optional[float] = None
    stop_sequences: Optional[List[str]] = None
    timeout: Optional[int] = None

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
            # [DEBUG] 输出当前属性状态
            print(
                f"[DEBUG _generate] self.max_tokens={getattr(self, 'max_tokens', 'NOT_SET')}"
            )
            print(
                f"[DEBUG _generate] self.temperature={getattr(self, 'temperature', 'NOT_SET')}"
            )
            print(
                f"[DEBUG _generate] self.timeout={getattr(self, 'timeout', 'NOT_SET')}"
            )

            headers = {
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
            }
            # getattr 的默认值只在属性不存在时生效,如果显式设置为 None 则返回 None
            # 因此需要额外检查
            max_tokens_val = getattr(self, "max_tokens", None)
            if max_tokens_val is None:
                max_tokens_val = 1000

            temperature_val = getattr(self, "temperature", None)
            if temperature_val is None:
                temperature_val = 0.7

            print(
                f"[DEBUG _generate] 最终值: max_tokens={max_tokens_val}, temperature={temperature_val}"
            )

            data = {
                "model": self.model,
                "messages": api_messages,
                "max_tokens": max_tokens_val,
                "temperature": temperature_val,
                "stream": self.streaming,
            }
            # 可选生成参数
            if hasattr(self, "top_p") and self.top_p is not None:
                data["top_p"] = self.top_p
            if (
                hasattr(self, "frequency_penalty")
                and self.frequency_penalty is not None
            ):
                data["frequency_penalty"] = self.frequency_penalty
            if hasattr(self, "presence_penalty") and self.presence_penalty is not None:
                data["presence_penalty"] = self.presence_penalty
            if hasattr(self, "stop_sequences") and self.stop_sequences:
                # OpenAI 兼容接口为 'stop'
                data["stop"] = self.stop_sequences
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

            # 使用数据库配置的 timeout,如果没有或为 None 则默认 30 秒
            timeout_val = getattr(self, "timeout", None)
            if timeout_val is None:
                timeout_val = 30

            print(
                f"[DEBUG _generate] 最终 timeout={timeout_val} (type: {type(timeout_val)})"
            )

            response = requests.post(
                f"{self.base_url}/chat/completions",
                headers=headers,
                json=data,
                timeout=timeout_val,
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


class LangChainAgent:
    def __init__(
        self,
        api_key: str,
        base_url: str = None,
        model: str = None,
    ):
        if base_url is None or model is None:
            raise ValueError("必须提供 api_key, base_url 和 model 参数")

        self.llm = SimpleOpenAILLM(
            api_key=api_key, base_url=base_url, model=model, streaming=True
        )
        self._milvus_retriever = None  # Milvus 检索器
        self._knowledge_bases = []  # 当前会话的知识库ID列表
        self.history: List[Dict[str, str]] = []
        self.summary: Optional[str] = None

        # 从配置加载参数
        import config

        self.max_rounds: int = config.MAX_HISTORY_LENGTH
        self.summary_trigger: int = config.SUMMARY_TRIGGER
        self.max_iterations: int = config.MAX_ITERATIONS

    @property
    def milvus_retriever(self):
        """延迟初始化 Milvus 检索器"""
        if self._milvus_retriever is None:
            from core.milvus_retriever import MilvusRetriever

            self._milvus_retriever = MilvusRetriever()
        return self._milvus_retriever

    def _format_recent_history(self) -> str:
        recent = self.history[-self.max_rounds :]
        return "\n".join(
            [
                ("用户" if t["role"] == "user" else "助手") + f": {t['content']}"
                for t in recent
            ]
        )

    def _maybe_summarize(self):
        if self.summary_trigger is None or len(self.history) < self.summary_trigger:
            return
        if self.summary and len(self.history) < self.summary_trigger * 2:
            return
        summary_llm = SimpleOpenAILLM(
            api_key=self.llm.api_key,
            base_url=self.llm.base_url,
            model=self.llm.model,
            streaming=False,
        )
        # 复制生成参数
        for attr in [
            "max_tokens",
            "temperature",
            "top_p",
            "top_k",
            "frequency_penalty",
            "presence_penalty",
            "stop_sequences",
            "timeout",
        ]:
            if hasattr(self.llm, attr):
                setattr(summary_llm, attr, getattr(self.llm, attr))
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

    def chat_stream(
        self,
        user_input: str,
        history: Optional[List[Dict[str, str]]] = None,
        enable_rag: bool = False,
        knowledge_bases: Optional[List[str]] = None,
        enable_plugins: bool = False,
        allowed_plugins: Optional[List[str]] = None,
        system_prompt: Optional[str] = None,
        tools_info: Optional[List[Dict[str, Any]]] = None,
    ):
        """
        流式对话 - 逐 token 生成
        统一的推理流程：模型自己判断是否需要调用工具

        Args:
            user_input: 用户问题
            history: 对话历史
            enable_rag: 是否启用RAG
            knowledge_bases: 知识库ID列表（使用 Milvus 向量数据库）
            enable_plugins: 是否启用工具
            allowed_plugins: 允许的工具列表（已废弃，改用 tools_info）
            system_prompt: 系统提示词
            tools_info: 工具信息列表，每个包含 purpose, call_method, parameters, user_settings

        Yields:
            Dict: {"content": "token文本", "done": False, "summary": null} 或
                  {"content": "", "done": True, "metadata": {...}}
        """
        try:
            original_history = None
            if history is not None:
                original_history = self.history
                self.history = history.copy()

            self.history.append({"role": "user", "content": user_input})
            self._maybe_summarize()

            # 如果生成了摘要，在第一句话就给出并作为返回字段返回
            summary_to_return = self.summary if self.summary else None

            recent_context = self._format_recent_history()
            summary_block = f"对话摘要:\n{self.summary}\n\n" if self.summary else ""
            base_prompt = system_prompt or "你是一个专业的AI助手。"
            default_system_prompt = f"{base_prompt}\n在与用户交互时，如果你需要引用内部工具或知识库的相关细节，请用自然、流畅的方式表达，使用户感受不到机械感或技术生硬的措辞。"

            # ============ 推理循环 ============
            reasoning_steps = 0
            max_reasoning_steps = 5
            has_sufficient_info = False
            final_answer = None

            # 构建工具库信息（可能为空）
            tools_library = ""
            if enable_plugins and tools_info:
                tools_library = self._build_tools_library(tools_info)

            # 保存知识库ID列表（用于 RAG 查询）
            self._knowledge_bases = knowledge_bases or []

            # 构建rag_library内容（如需支持RAG，可在此处生成）
            rag_library = None
            if enable_rag and self._knowledge_bases:
                # 提供详细的 RAG 使用说明
                rag_library = f"""知识库检索功能已启用（知识库数: {len(self._knowledge_bases)}）。
使用说明:
1. 检索策略: 系统会从指定知识库中取 Top-10 个相关结果，但只返回相似度 ≥ 0.6 的内容
2. 多知识库: 同时搜索 {len(self._knowledge_bases)} 个知识库，结果按相似度排序
2. 查询优化: 不要直接使用用户的原始问题，而应该提取关键信息或重新表述
   - 例如: 用户问"你能告诉我机器学习是什么吗?" → 查询应为"机器学习定义"
   - 例如: 用户问"深度学习有哪些应用场景?" → 查询应为"深度学习应用场景"
3. 调用时机: 当需要查询专业知识、历史信息或文档内容时使用以及你觉得缺少信息时可以使用试试"""

            while reasoning_steps < max_reasoning_steps and not has_sufficient_info:
                reasoning_steps += 1

                # 统一的推理提示词（模型自己判断是否调用工具）
                reasoning_prompt = self._build_unified_reasoning_prompt(
                    system_prompt=default_system_prompt,
                    user_input=user_input,
                    tools_library=tools_library,
                    recent_context=recent_context,
                    summary_block=summary_block,
                    reasoning_step=reasoning_steps,
                    max_steps=max_reasoning_steps,
                    rag_library=rag_library,
                )

                # 执行推理（非流式）
                reasoning_llm = SimpleOpenAILLM(
                    api_key=self.llm.api_key,
                    base_url=self.llm.base_url,
                    model=self.llm.model,
                    streaming=False,
                )
                for attr in [
                    "max_tokens",
                    "temperature",
                    "top_p",
                    "top_k",
                    "frequency_penalty",
                    "presence_penalty",
                    "stop_sequences",
                    "timeout",
                ]:
                    if hasattr(self.llm, attr):
                        setattr(reasoning_llm, attr, getattr(self.llm, attr))

                reasoning_response = reasoning_llm._generate(
                    [HumanMessage(content=reasoning_prompt)]
                )
                reasoning_result = reasoning_response.generations[
                    0
                ].message.content.strip()

                logger.info(
                    f"[推理步骤 {reasoning_steps}] LLM 输出长度: {len(reasoning_result)}, 内容: {reasoning_result}"
                )

                # 解析推理结果
                (
                    decision,
                    tool_call_instruction,
                    final_answer,
                    has_sufficient_info,
                ) = self._parse_unified_reasoning_output(reasoning_result, tools_info)

                logger.info(
                    f"[推理步骤 {reasoning_steps}] 决策: {decision}, 工具调用: {tool_call_instruction is not None}, 最终答案: {final_answer is not None}"
                )

                # 如果决策是调用工具
                if decision == "TOOL_CALL" and tool_call_instruction:
                    tool_result = self._execute_tool_instruction(
                        tool_call_instruction, tools_info
                    )
                    # 更新上下文，继续推理
                    user_input = f"{user_input}\n\n[工具调用结果]\n{tool_result}"
                    continue

                # 如果决策是直接回答，标记为有足够信息
                if decision == "DIRECT_ANSWER":
                    has_sufficient_info = True

            # ============ 推理循环结束 ============

            # 如果没有生成最终回答，强制生成一个兜底回答
            if not final_answer:
                logger.warning(
                    f"[chat_stream] 推理 {reasoning_steps} 步后未生成答案，强制生成兜底回答"
                )
                # 构建兜底提示词，让 LLM 直接回答
                fallback_prompt = f"{default_system_prompt}\n\n用户问题：{user_input}\n\n请直接回答用户的问题，即使信息不完整也要尽力给出有用的回答。"

                fallback_llm = SimpleOpenAILLM(
                    api_key=self.llm.api_key,
                    base_url=self.llm.base_url,
                    model=self.llm.model,
                    streaming=False,
                )
                # 复制生成参数
                for attr in [
                    "max_tokens",
                    "temperature",
                    "top_p",
                    "top_k",
                    "frequency_penalty",
                    "presence_penalty",
                    "stop_sequences",
                    "timeout",
                ]:
                    if hasattr(self.llm, attr):
                        setattr(fallback_llm, attr, getattr(self.llm, attr))

                try:
                    fallback_response = fallback_llm._generate(
                        [HumanMessage(content=fallback_prompt)]
                    )
                    final_answer = fallback_response.generations[
                        0
                    ].message.content.strip()
                    logger.info(
                        f"[chat_stream] 兜底回答生成成功，长度: {len(final_answer)}"
                    )
                except Exception as e:
                    logger.error(f"[chat_stream] 兜底回答生成失败: {e}")
                    final_answer = "抱歉，我在处理您的问题时遇到了一些困难。请您换个方式描述问题，或者稍后再试。"

            # 生成最终回答
            logger.info(
                f"[chat_stream] 准备返回最终回答，内容长度: {len(final_answer)}"
            )
            full_answer = final_answer
            first_chunk = True
            stream_count = 0

            # 直接逐字 yield 最终答案（不需要再调用 LLM）
            for char in final_answer:
                stream_count += 1
                if first_chunk:
                    yield {
                        "content": char,
                        "done": False,
                        "summary": summary_to_return,
                    }
                    first_chunk = False
                else:
                    yield {"content": char, "done": False, "summary": None}

            logger.info(
                f"[chat_stream] 流式输出完成，总 {stream_count} 个字符，总长 {len(full_answer)}"
            )
            self.history.append({"role": "assistant", "content": full_answer})
            yield {
                "content": "",
                "done": True,
                "metadata": {
                    "history_length": len(self.history),
                    "reasoning_steps": reasoning_steps,
                },
            }

        except Exception as e:
            yield {"content": "", "done": True, "error": str(e)}
        finally:
            if history is not None and original_history is not None:
                self.history = original_history

    def _build_tools_library(self, tools_info: List[Dict[str, Any]]) -> str:
        """
        构建工具库说明
        从数据库中直接引用工具的描述、使用方法、参数、示例和用户设定
        """
        if not tools_info:
            return "当前无可用工具"

        tools_desc = []
        for tool in tools_info:
            tool_name = tool.get("name", "未知工具")
            purpose = tool.get("purpose", "")
            call_method = tool.get("call_method", "")
            parameters = tool.get("parameters", [])
            user_settings = tool.get("user_settings", {})

            params_desc = ""
            if parameters:
                if isinstance(parameters, str):
                    params_desc = parameters
                else:
                    params_desc = json.dumps(parameters, ensure_ascii=False, indent=2)

            user_settings_desc = ""
            if user_settings:
                user_settings_desc = (
                    f"\n用户预设参数：{json.dumps(user_settings, ensure_ascii=False)}"
                )

            tool_desc = f"""
【{tool_name}】
  用途：{purpose}
  调用方法：{call_method}
  参数定义：
{params_desc}{user_settings_desc}
"""
            tools_desc.append(tool_desc)

        return "可用工具库：\n" + "\n".join(tools_desc)

    def _stream_generate(self, prompt: str):
        """使用流式 LLM 生成内容"""
        import requests
        import json

        logger.info(f"[_stream_generate] 开始流式生成，prompt长度: {len(prompt)}")

        # 创建流式 LLM
        streaming_llm = SimpleOpenAILLM(
            api_key=self.llm.api_key,
            base_url=self.llm.base_url,
            model=self.llm.model,
            streaming=True,
        )

        # 复制生成参数
        for attr in [
            "max_tokens",
            "temperature",
            "top_p",
            "top_k",
            "frequency_penalty",
            "presence_penalty",
            "stop_sequences",
            "timeout",
        ]:
            if hasattr(self.llm, attr):
                setattr(streaming_llm, attr, getattr(self.llm, attr))

        # 调用流式生成
        headers = {
            "Authorization": f"Bearer {streaming_llm.api_key}",
            "Content-Type": "application/json",
        }

        max_tokens_val = getattr(streaming_llm, "max_tokens", None) or 1000
        temperature_val = getattr(streaming_llm, "temperature", None) or 0.7
        timeout_val = getattr(streaming_llm, "timeout", None) or 30

        data = {
            "model": streaming_llm.model,
            "messages": [{"role": "user", "content": prompt}],
            "max_tokens": max_tokens_val,
            "temperature": temperature_val,
            "stream": True,
        }

        if hasattr(streaming_llm, "top_p") and streaming_llm.top_p is not None:
            data["top_p"] = streaming_llm.top_p
        if (
            hasattr(streaming_llm, "frequency_penalty")
            and streaming_llm.frequency_penalty is not None
        ):
            data["frequency_penalty"] = streaming_llm.frequency_penalty
        if (
            hasattr(streaming_llm, "presence_penalty")
            and streaming_llm.presence_penalty is not None
        ):
            data["presence_penalty"] = streaming_llm.presence_penalty
        if hasattr(streaming_llm, "stop_sequences") and streaming_llm.stop_sequences:
            data["stop"] = streaming_llm.stop_sequences

        logger.info(
            f"[_stream_generate] 调用 API: {streaming_llm.base_url}/chat/completions"
        )
        logger.info(f"[_stream_generate] 模型: {streaming_llm.model}, stream=True")

        try:
            response = requests.post(
                f"{streaming_llm.base_url}/chat/completions",
                headers=headers,
                json=data,
                timeout=timeout_val,
                stream=True,
            )
            response.raise_for_status()

            logger.info(f"[_stream_generate] API 响应状态: {response.status_code}")

            chunk_count = 0
            for line in response.iter_lines():
                if line:
                    line = line.decode("utf-8")
                    if line.startswith("data: "):
                        line = line[6:]
                    if line == "[DONE]":
                        logger.info(
                            f"[_stream_generate] 收到 [DONE]，共生成 {chunk_count} 个chunk"
                        )
                        break
                    try:
                        chunk = json.loads(line)
                        if "choices" in chunk and len(chunk["choices"]) > 0:
                            delta = chunk["choices"][0].get("delta", {})
                            if "content" in delta:
                                content = delta["content"]
                                chunk_count += 1
                                logger.debug(
                                    f"[_stream_generate] delta chunk: {repr(content)}"
                                )
                                yield content
                    except json.JSONDecodeError as e:
                        logger.warning(
                            f"[_stream_generate] JSON 解析失败: {e}, line={line}"
                        )
                        continue

            logger.info(f"[_stream_generate] 流式生成完成")

        except Exception as e:
            logger.error(f"[_stream_generate] 流式生成失败: {e}", exc_info=True)
            raise

    def _build_unified_reasoning_prompt(
        self,
        system_prompt: str,
        user_input: str,
        tools_library: str = None,
        recent_context: str = "",
        summary_block: str = "",
        reasoning_step: int = 1,
        max_steps: int = 5,
        rag_library: str = None,
    ) -> str:
        """
        构建统一推理提示词，工具和rag部分按字段有无动态插入
        模型自己判断是否需要调用工具，如需要则输出JSON格式的调用指令
        """
        prompt = f"{system_prompt}\n"

        # 上下文信息
        if summary_block or recent_context:
            prompt += f"【上下文信息】\n"
            if summary_block:
                prompt += f"{summary_block}"
            if recent_context:
                prompt += f"最近对话：\n{recent_context}\n"

        # 当前问题
        prompt += f"【当前问题】\n{user_input}\n"

        # RAG知识库部分
        if rag_library:
            prompt += f'\n【知识库检索】\n{rag_library}\n如需调用知识库检索，请输出如下JSON：\n{{"rag_query": "你的检索问题"}}\n'

        # 工具部分
        if tools_library and tools_library != "当前无可用工具":
            prompt += f"\n【可用工具】\n{tools_library}\n工具调用请输出包含工具名和参数的JSON格式。\n若调用过程中已知信息不足，无法对必填字段取值，则直接输出所缺字段。\n"

        # 推理规范与过程合并
        if (tools_library and tools_library != "当前无可用工具") or rag_library:
            prompt += f"\n【推理流程】\n- 推理轮次：{reasoning_step}/{max_steps}\n- 每步判断信息是否足够，足够则直接输出最终回答，不足则继续推理或调用工具/知识库。\n- 达到推理上限（{max_steps}次）时必须给出最终回答。\n"

        # 输出格式
        if tools_library and tools_library != "当前无可用工具":
            prompt += '\n【输出格式】\n工具调用请输出JSON格式：\n{\n  "tool_call": {\n    "tool_name": "工具名称",\n    "method": "GET/POST",\n    "url": "https://api.example.com/endpoint",\n    "parameters": {"param1": "value1", "param2": "value2"}\n  }\n}\n若必填参数信息不足，输出：\n【缺少必填字段】\n缺少字段：xxx, yyy\n'
        if rag_library:
            prompt += '\n知识库检索请输出如下JSON：\n{"rag_query": "你的检索问题"}\n'
        prompt += "\n如果直接回答，输出格式如下：\n\n【最终回答】\n[这里输出完整的用自然语言的回答]\n"

        return prompt

    def _parse_unified_reasoning_output(
        self,
        reasoning_result: str,
        tools_info: Optional[List[Dict[str, Any]]],
    ) -> tuple:
        """
        解析统一推理结果
        返回: (decision, tool_call_instruction, final_answer, has_sufficient_info)
        decision 可能值: "TOOL_CALL", "DIRECT_ANSWER", "CONTINUE_REASONING"
        """
        decision = "CONTINUE_REASONING"
        tool_call_instruction = None
        final_answer = None
        has_sufficient_info = False

        # 检查是否是最终回答
        answer_match = re.search(
            r"【最终回答】\s*(.*?)(?=【|$)", reasoning_result, re.DOTALL
        )
        if answer_match:
            final_answer = answer_match.group(1).strip()
            if final_answer:
                decision = "DIRECT_ANSWER"
                has_sufficient_info = True
                return (
                    decision,
                    tool_call_instruction,
                    final_answer,
                    has_sufficient_info,
                )

        # 检查是否是JSON格式的工具调用或RAG查询
        try:
            # 尝试直接解析整个返回为JSON
            parsed_json = json.loads(reasoning_result)

            # 检查是否是 RAG 查询
            if "rag_query" in parsed_json:
                rag_query_text = parsed_json["rag_query"]
                # 使用特殊标记表示这是 RAG 调用
                tool_call_instruction = json.dumps(
                    {"type": "rag_query", "query": rag_query_text}
                )
                decision = "TOOL_CALL"
                return (
                    decision,
                    tool_call_instruction,
                    final_answer,
                    has_sufficient_info,
                )

            # 检查是否是工具调用
            if "tool_call" in parsed_json:
                tool_call_instruction = json.dumps(parsed_json["tool_call"])
                decision = "TOOL_CALL"
                return (
                    decision,
                    tool_call_instruction,
                    final_answer,
                    has_sufficient_info,
                )
        except json.JSONDecodeError:
            # 如果整体解析失败，尝试查找JSON片段
            try:
                # 查找 { 开头到匹配的 } 结尾的JSON
                start_idx = reasoning_result.find("{")
                if start_idx != -1:
                    # 简单的括号匹配
                    bracket_count = 0
                    end_idx = start_idx
                    for i in range(start_idx, len(reasoning_result)):
                        if reasoning_result[i] == "{":
                            bracket_count += 1
                        elif reasoning_result[i] == "}":
                            bracket_count -= 1
                            if bracket_count == 0:
                                end_idx = i + 1
                                break

                    if end_idx > start_idx:
                        json_str = reasoning_result[start_idx:end_idx]
                        tool_call_json = json.loads(json_str)

                        # 检查是否是 RAG 查询
                        if "rag_query" in tool_call_json:
                            rag_query_text = tool_call_json["rag_query"]
                            tool_call_instruction = json.dumps(
                                {"type": "rag_query", "query": rag_query_text}
                            )
                            decision = "TOOL_CALL"
                            return (
                                decision,
                                tool_call_instruction,
                                final_answer,
                                has_sufficient_info,
                            )

                        if "tool_call" in tool_call_json:
                            tool_call_instruction = json.dumps(
                                tool_call_json["tool_call"]
                            )
                            decision = "TOOL_CALL"
                            return (
                                decision,
                                tool_call_instruction,
                                final_answer,
                                has_sufficient_info,
                            )
            except json.JSONDecodeError:
                pass

        # 兼容旧的HTTP指令格式（寻找GET/POST/PUT/DELETE等HTTP方法）
        http_methods = ["GET", "POST", "PUT", "DELETE", "PATCH"]
        for method in http_methods:
            pattern = rf"{method}\s+https?://\S+"
            match = re.search(pattern, reasoning_result)
            if match:
                # 提取完整的调用指令（包括Body如果有）
                instruction = match.group(0)
                # 如果是POST/PUT等，尝试提取Body
                if method in ["POST", "PUT", "PATCH"]:
                    body_match = re.search(
                        rf"{instruction}.*?\nBody:\s*(.*?)(?=\n[A-Z]|$)",
                        reasoning_result,
                        re.DOTALL,
                    )
                    if body_match:
                        instruction = (
                            f"{instruction}\nBody: {body_match.group(1).strip()}"
                        )

                tool_call_instruction = instruction
                decision = "TOOL_CALL"
                return (
                    decision,
                    tool_call_instruction,
                    final_answer,
                    has_sufficient_info,
                )

        # 如果没有明确标记，检查是否是可以直接回答的内容
        if len(reasoning_result) > 100 and "http" not in reasoning_result.lower():
            final_answer = reasoning_result
            decision = "DIRECT_ANSWER"
            has_sufficient_info = True

        return decision, tool_call_instruction, final_answer, has_sufficient_info

    def _execute_tool_instruction(
        self,
        tool_instruction: str,
        tools_info: Optional[List[Dict[str, Any]]],
    ) -> str:
        """
        执行工具调用指令（支持JSON格式、RAG查询和旧的HTTP指令格式）
        JSON格式: {"tool_name": "xxx", "method": "GET/POST", "url": "...", "parameters": {...}}
        RAG格式: {"type": "rag_query", "query": "查询文本"}
        HTTP格式: "GET https://api.example.com/endpoint?param=value"
        """
        try:
            # 尝试解析JSON格式
            try:
                tool_call = json.loads(tool_instruction)

                # 检查是否是 RAG 查询
                if tool_call.get("type") == "rag_query":
                    query = tool_call.get("query", "")
                    logger.info(f"[执行RAG查询] 提取的查询: '{query}'")

                    # 使用 Milvus 检索器从知识库中查询（Top-K=10, 阈值=0.6）
                    if not self._knowledge_bases:
                        logger.warning("[RAG查询] 未指定知识库ID")
                        return "【知识库检索结果】\n未指定知识库，无法执行检索。"

                    results = self.milvus_retriever.retrieve(
                        query_text=query,
                        kb_ids=self._knowledge_bases,
                        k=10,
                        score_threshold=0.6,
                    )

                    if not results:
                        return "【知识库检索结果】\n未找到相关内容，相似度均低于阈值(0.6)。"

                    # 格式化检索结果
                    result_text = (
                        f"【知识库检索结果】\n找到 {len(results)} 条相关内容:\n\n"
                    )
                    for i, item in enumerate(results, 1):
                        result_text += f"[{i}] (相似度: {item['similarity_score']}, 知识库: {item['kb_name']})\n"
                        result_text += f"来源: {item['source']}\n"
                        # 限制每条内容的长度，避免太长
                        content = item["content"]
                        if len(content) > 500:
                            content = content[:500] + "..."
                        result_text += f"内容: {content}\n\n"

                    logger.info(
                        f"[RAG检索完成] 从 {len(self._knowledge_bases)} 个知识库返回 {len(results)} 条结果"
                    )
                    return result_text
                if "method" in tool_call and "url" in tool_call:
                    method = tool_call["method"].upper()
                    url = tool_call["url"]
                    parameters = tool_call.get("parameters", {})

                    # 根据HTTP方法构建请求
                    headers = {"Content-Type": "application/json"}

                    if method == "GET":
                        # GET请求将parameters作为query参数
                        if parameters:
                            query_string = "&".join(
                                [f"{k}={v}" for k, v in parameters.items()]
                            )
                            url = f"{url}?{query_string}"
                        response = requests.get(url, headers=headers, timeout=10)
                    elif method == "POST":
                        response = requests.post(
                            url, json=parameters, headers=headers, timeout=10
                        )
                    elif method == "PUT":
                        response = requests.put(
                            url, json=parameters, headers=headers, timeout=10
                        )
                    elif method == "DELETE":
                        response = requests.delete(url, headers=headers, timeout=10)
                    else:
                        return f"不支持的HTTP方法: {method}"

                    response.raise_for_status()
                    return response.text
            except (json.JSONDecodeError, KeyError):
                pass

            # 兼容旧的HTTP指令格式
            parts = tool_instruction.split(maxsplit=1)
            if len(parts) != 2:
                return f"调用指令格式错误: {tool_instruction}"

            method = parts[0].upper()
            url_and_body = parts[1]

            # 处理POST/PUT等有Body的请求
            if method in ["POST", "PUT", "PATCH"]:
                # 寻找 Body: 的标记
                if "\nBody:" in url_and_body or "\nBody " in url_and_body:
                    url_part, body_part = url_and_body.split("\nBody", 1)
                    url = url_part.strip()
                    body_part = body_part.lstrip(": ")
                    try:
                        body_data = json.loads(body_part)
                    except json.JSONDecodeError:
                        body_data = body_part
                else:
                    url = url_and_body.strip()
                    body_data = None
            else:
                url = url_and_body.strip()
                body_data = None

            # 执行HTTP请求
            headers = {"Content-Type": "application/json"}

            if method == "GET":
                response = requests.get(url, headers=headers, timeout=10)
            elif method == "POST":
                response = requests.post(
                    url, json=body_data, headers=headers, timeout=10
                )
            elif method == "PUT":
                response = requests.put(
                    url, json=body_data, headers=headers, timeout=10
                )
            elif method == "DELETE":
                response = requests.delete(url, headers=headers, timeout=10)
            else:
                return f"不支持的HTTP方法: {method}"

            response.raise_for_status()
            return response.text

        except Exception as e:
            return f"工具调用失败: {str(e)}"

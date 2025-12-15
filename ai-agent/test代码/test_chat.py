"""
测试 chat 接口
集成 RAG 和工具调用功能
"""

import requests
import json
import sys
from typing import Generator

# 进程内会话历史缓存（仅用于测试进程内）
_SESSION_HISTORY: dict = {}
# 最多保留的对话轮数（每轮包含 user + assistant）
_MAX_TURNS = 5


def _append_user(session_id: str, message: str):
    _SESSION_HISTORY.setdefault(session_id, []).append(
        {"role": "user", "content": message}
    )
    max_msgs = _MAX_TURNS * 2
    if len(_SESSION_HISTORY[session_id]) > max_msgs:
        _SESSION_HISTORY[session_id] = _SESSION_HISTORY[session_id][-max_msgs:]


def _append_assistant(session_id: str, message: str):
    _SESSION_HISTORY.setdefault(session_id, []).append(
        {"role": "assistant", "content": message}
    )
    max_msgs = _MAX_TURNS * 2
    if len(_SESSION_HISTORY[session_id]) > max_msgs:
        _SESSION_HISTORY[session_id] = _SESSION_HISTORY[session_id][-max_msgs:]


def _get_history(session_id: str):
    return _SESSION_HISTORY.get(session_id, [])


def stream_chat(
    message: str,
    session_id: str = "test_session",
    user_id: str = "test_user",
    enable_rag: bool = False,
    enable_tools: bool = True,
    model_id: str = "qwen3-max",
) -> Generator[str, None, None]:
    """
    流式聊天请求

    Args:
        message: 用户消息
        session_id: 会话ID
        kb_id: 知识库ID
        user_id: 用户ID
        enable_rag: 是否启用RAG
        enable_tools: 是否启用工具
        model_id: 模型ID（默认 qwen3-max）

    Yields:
        响应内容
    """
    url = "http://localhost:8000/api/chat"

    # 将用户消息写入本地会话历史（测试进程内缓存）
    _append_user(session_id, message)

    # 构建请求体（history 从本地缓存读取；enable_rag 时填入知识库ID）
    payload = {
        "message": message,
        "session_id": session_id,
        "model_id": model_id,  # 使用正确的模型ID
        "system_prompt": None,
        "history": _get_history(session_id),
        "tools": (
            []
            if not enable_tools
            else [
                "test_user_controlDevice",
                "test_user_executePreset",
                "test_user_getSensorData",
            ]
        ),  # 工具列表
        "knowledge_bases": [] if not enable_rag else ["e227b5ceed636db7"],
    }

    try:
        response = requests.post(url, json=payload, stream=True, timeout=120)
        response.raise_for_status()

        assistant_full = ""
        for line in response.iter_lines():
            if line:
                line = line.decode("utf-8")
                if line == "data: [DONE]":
                    break
                elif line.startswith("data: "):
                    data_str = line[6:]
                    try:
                        data = json.loads(data_str)
                        if "content" in data and data["content"]:
                            assistant_full += data["content"]
                            yield data["content"]
                        elif "error" in data:
                            yield f"\n❌ 错误: {data['error']}\n"
                    except json.JSONDecodeError:
                        continue

        # 流结束后把完整回复追加到本地历史
        if assistant_full:
            _append_assistant(session_id, assistant_full)
    except Exception as e:
        yield f"\n❌ 请求失败: {e}\n"


def non_stream_chat(
    message: str,
    session_id: str = "test_session",
    user_id: str = "test_user",
    enable_rag: bool = False,
    enable_tools: bool = True,
    model_id: str = "qwen3-max",
) -> dict:
    """
    非流式聊天请求（如果有这个端点的话）
    """
    url = "http://localhost:8000/api/chat"

    # 将用户消息写入本地会话历史（测试进程内缓存）
    _append_user(session_id, message)

    payload = {
        "message": message,
        "session_id": session_id,
        "model_id": model_id,  # 使用正确的模型ID
        "system_prompt": None,
        "history": _get_history(session_id),
        "tools": (
            []
            if not enable_tools
            else [
                "test_user_controlDevice",
                "test_user_executePreset",
                "test_user_getSensorData",
            ]
        ),
        "knowledge_bases": [] if not enable_rag else ["e227b5ceed636db7"],
    }

    try:
        response = requests.post(url, json=payload, timeout=120)
        response.raise_for_status()

        # 收集流式数据或按行拼接
        full_response = ""
        for line in response.iter_lines():
            if line:
                line = line.decode("utf-8")
                if line.startswith("data: "):
                    data_str = line[6:]
                    try:
                        data = json.loads(data_str)
                        if "content" in data:
                            full_response += data.get("content", "")
                    except json.JSONDecodeError:
                        continue

        # 把 assistant 回复追加到本地历史
        if full_response:
            _append_assistant(session_id, full_response)

        return {"success": True, "response": full_response}
    except Exception as e:
        return {"success": False, "error": str(e)}


def check_server():
    """检查服务器是否运行"""
    try:
        response = requests.get("http://localhost:8000/health", timeout=5)
        return response.status_code == 200
    except:
        return False


def test_basic_rag():
    """测试基础RAG功能"""
    print("\n" + "=" * 80)
    print("测试 1: 基础RAG查询")
    print("=" * 80 + "\n")

    questions = [
        "什么是机器学习？",
        "深度学习有哪些应用？",
        "人工智能的发展历史是什么？",
    ]

    for i, question in enumerate(questions, 1):
        print(f"\n【问题 {i}】{question}")
        print("-" * 80)
        print("AI: ", end="", flush=True)

        for chunk in stream_chat(
            question, enable_rag=True, enable_tools=False, model_id="qwen3-max"
        ):
            print(chunk, end="", flush=True)

        print("\n")


def test_tools_call():
    """测试工具调用"""
    print("\n" + "=" * 80)
    print("测试 2: 工具调用")
    print("=" * 80 + "\n")

    questions = [
        "帮我查询北京的天气",
        "搜索最新的AI发展动态",
    ]

    for i, question in enumerate(questions, 1):
        print(f"\n【问题 {i}】{question}")
        print("-" * 80)
        print("AI: ", end="", flush=True)

        for chunk in stream_chat(
            question, enable_rag=False, enable_tools=True, model_id="qwen3-max"
        ):
            print(chunk, end="", flush=True)

        print("\n")


def test_combined():
    """测试RAG + 工具组合"""
    print("\n" + "=" * 80)
    print("测试 3: RAG + 工具组合")
    print("=" * 80 + "\n")

    questions = [
        "请从知识库检索机器学习的信息，然后搜索最新的发展动态",
        "深度学习有什么应用？最近有什么新闻吗？",
    ]

    for i, question in enumerate(questions, 1):
        print(f"\n【问题 {i}】{question}")
        print("-" * 80)
        print("AI: ", end="", flush=True)

        for chunk in stream_chat(
            question, enable_rag=True, enable_tools=True, model_id="qwen3-max"
        ):
            print(chunk, end="", flush=True)

        print("\n")


def test_multi_turn():
    """测试多轮对话"""
    print("\n" + "=" * 80)
    print("测试 4: 多轮对话")
    print("=" * 80 + "\n")

    session_id = "test_session_multiround"

    questions = ["什么是机器学习？", "那深度学习呢？", "它们有什么区别？"]

    for i, question in enumerate(questions, 1):
        print(f"\n【轮次 {i}】{question}")
        print("-" * 80)
        print("AI: ", end="", flush=True)

        for chunk in stream_chat(
            question,
            session_id=session_id,
            enable_rag=True,
            enable_tools=True,
            model_id="qwen3-max",
        ):
            print(chunk, end="", flush=True)

        print("\n")


def interactive_mode():
    """交互模式"""
    print("\n" + "=" * 80)
    print("交互模式 - 与AI对话")
    print("=" * 80)
    print("\n说明:")
    print("  - 输入问题与AI对话")
    print("  - 输入 'quit' 退出")
    print("  - 知识库已启用，工具也已启用\n")

    session_id = "interactive_session"

    while True:
        try:
            question = input("你: ").strip()

            if question.lower() == "quit":
                print("👋 再见！")
                break

            if not question:
                continue

            print("AI: ", end="", flush=True)

            for chunk in stream_chat(
                question,
                session_id=session_id,
                enable_rag=True,
                enable_tools=True,
                model_id="qwen3-max",
            ):
                print(chunk, end="", flush=True)

            print("\n")

        except KeyboardInterrupt:
            print("\n\n👋 对话已中断")
            break
        except Exception as e:
            print(f"❌ 错误: {e}\n")


def main():
    """主函数"""
    print("╔" + "=" * 78 + "╗")
    print("║" + " " * 78 + "║")
    print("║" + "  Chat 接口测试".center(78) + "║")
    print("║" + "  知识库ID: e227b5ceed636db7 | 用户ID: test_user".center(78) + "║")
    print("║" + " " * 78 + "║")
    print("╚" + "=" * 78 + "╝\n")

    # 检查服务器
    print("检查服务器连接...", end="", flush=True)
    if not check_server():
        print("\n❌ 无法连接到服务器")
        print("   请先启动API服务: python main.py\n")
        sys.exit(1)

    print(" ✅\n")

    # 菜单
    print("选择测试模式:")
    print("  1. 基础RAG查询")
    print("  2. 工具调用")
    print("  3. RAG + 工具组合")
    print("  4. 多轮对话")
    print("  5. 交互模式")
    print("  0. 退出\n")

    while True:
        try:
            choice = input("请选择 (0-5): ").strip()

            if choice == "0":
                print("👋 再见！")
                break
            elif choice == "1":
                test_basic_rag()
                input("\n按 Enter 返回菜单...")
            elif choice == "2":
                test_tools_call()
                input("\n按 Enter 返回菜单...")
            elif choice == "3":
                test_combined()
                input("\n按 Enter 返回菜单...")
            elif choice == "4":
                test_multi_turn()
                input("\n按 Enter 返回菜单...")
            elif choice == "5":
                interactive_mode()
                input("\n按 Enter 返回菜单...")
            else:
                print("❌ 无效选择\n")

        except KeyboardInterrupt:
            print("\n\n👋 再见！")
            break
        except Exception as e:
            print(f"❌ 错误: {e}\n")


if __name__ == "__main__":
    main()

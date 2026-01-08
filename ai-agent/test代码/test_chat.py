"""
AI Agent 交互式对话测试
"""

import requests
import json
import sys


def chat(
    message: str, session_id: str, model_id: str, enable_rag: bool, enable_tools: bool
):
    """发送聊天请求并流式输出"""
    url = "http://localhost:8000/api/chat"

    payload = {
        "message": message,
        "session_id": session_id,
        "model_id": model_id,
        "tools": (
            [
                "test_user_getWeatherInfo",
                "test_user_executePreset",
                "test_user_controlDevice",
            ]
            if enable_tools
            else []
        ),
        "knowledge_bases": ["e227b5ceed636db7"] if enable_rag else [],
    }

    try:
        response = requests.post(url, json=payload, stream=True, timeout=120)
        response.raise_for_status()

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
                            print(data["content"], end="", flush=True)
                        elif "error" in data:
                            print(f"\n❌ 错误: {data['error']}")
                    except json.JSONDecodeError:
                        continue
        print()  # 换行
    except Exception as e:
        print(f"\n❌ 请求失败: {e}\n")


def interactive_mode():
    """交互对话模式"""
    print("\n" + "=" * 80)
    print("AI Agent 交互式对话")
    print("=" * 80)
    print("\n配置:")
    print("  - 模型: deepseek-r1")
    print("  - RAG: 已启用 (知识库ID: e227b5ceed636db7)")
    print("  - 工具: 已启用 (天气查询)")
    print("\n操作:")
    print("  - 输入问题与AI对话")
    print("  - 输入 'quit' 或 'exit' 退出\n")

    session_id = "interactive_session"
    model_id = "qwen3-max"
    enable_rag = False
    enable_tools = True

    while True:
        try:
            question = input("你: ").strip()

            if question.lower() in ["quit", "exit", "q"]:
                print("👋 再见！")
                break

            if not question:
                continue

            print("AI: ", end="", flush=True)
            chat(question, session_id, model_id, enable_rag, enable_tools)
            print()

        except KeyboardInterrupt:
            print("\n\n👋 对话已中断")
            break
        except Exception as e:
            print(f"❌ 错误: {e}\n")


def check_server():
    """检查服务器是否运行"""
    try:
        response = requests.get("http://localhost:8000/health", timeout=5)
        return response.status_code == 200
    except:
        return False


def main():
    """主函数"""
    print("╔" + "=" * 78 + "╗")
    print("║" + "  AI Agent 对话测试".center(78) + "║")
    print("╚" + "=" * 78 + "╝")

    # 检查服务器
    print("\n正在连接服务器...", end="", flush=True)
    if not check_server():
        print(" ❌")
        print("无法连接到 http://localhost:8000")
        print("请先启动API服务: python main.py\n")
        sys.exit(1)
    print(" ✅")

    # 启动交互模式
    interactive_mode()


if __name__ == "__main__":
    main()

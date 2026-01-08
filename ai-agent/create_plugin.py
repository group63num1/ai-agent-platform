"""
测试创建插件接口的程序
调用 POST /api/tools 接口创建工具/插件
"""

import json
import requests
from pathlib import Path


def create_plugin_tool(base_url: str, user_id: str, openapi_file: str) -> dict:
    """
    创建插件工具

    Args:
        base_url: API服务器地址
        user_id: 用户ID
        openapi_file: OpenAPI JSON配置文件路径

    Returns:
        API响应结果
    """
    # 读取OpenAPI配置文件
    json_path = Path(openapi_file)
    if not json_path.exists():
        raise FileNotFoundError(f"配置文件不存在: {openapi_file}")

    openapi_json = json.loads(json_path.read_text(encoding="utf-8"))

    # 构造创建工具的请求体 - 正确格式
    request_data = {"user_id": user_id, "openapi": openapi_json}

    # 发送POST请求
    url = f"{base_url}/api/tools"
    headers = {"Content-Type": "application/json"}

    print(f"正在调用API: {url}")
    print(f"用户ID: {user_id}")
    print(f"OpenAPI配置文件: {openapi_file}")
    print(f"OpenAPI标题: {openapi_json.get('info', {}).get('title', 'N/A')}")
    print(f"OpenAPI版本: {openapi_json.get('info', {}).get('version', 'N/A')}")

    response = requests.post(url, json=request_data, headers=headers)

    # 打印结果
    print(f"\n状态码: {response.status_code}")
    print(f"响应内容:")
    result = response.json()
    print(json.dumps(result, ensure_ascii=False, indent=2))

    return result


def main():
    # 配置参数
    BASE_URL = "http://localhost:8000"
    USER_ID = "test_user"  # 用户ID，会作为tool_id的前缀
    OPENAPI_FILE = "coze-plugin-lite.json"

    try:
        result = create_plugin_tool(BASE_URL, USER_ID, OPENAPI_FILE)

        if result.get("success"):
            print("\n✓ 插件创建成功!")
            if "tools" in result:
                print(f"创建的工具:")
                for tool in result["tools"]:
                    print(f"  - {tool['tool_id']}: {tool['name']}")
            print(f"\n提示: 可以通过 PUT /api/tools/{{tool_id}} 设置 user_settings")
            print(
                f'例如: {{"user_settings": {{"uuid": "c10366db-d8c2-4914-92cb-b5a888520488"}}}}'
            )
        else:
            print("\n✗ 插件创建失败!")

    except requests.exceptions.ConnectionError:
        print(f"\n错误: 无法连接到API服务器 {BASE_URL}")
        print("请确保服务器正在运行 (uvicorn main:app --reload)")
    except Exception as e:
        print(f"\n错误: {e}")


if __name__ == "__main__":
    main()

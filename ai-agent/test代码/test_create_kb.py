"""
测试知识库创建接口
"""

import requests
import json
from pathlib import Path

BASE_URL = "http://127.0.0.1:8000/api/knowledge-bases"

# 测试文件路径
TEST_FILE = Path(__file__).parent / "test_document.txt"


def test_create_kb():
    """测试创建知识库接口"""

    print("\n" + "=" * 70)
    print("📝 测试知识库创建接口")
    print("=" * 70)

    # 准备知识库数据
    import datetime

    user_id = "test_user"
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    kb_name = f"测试知识库_{timestamp}"

    # 读取测试文件内容
    files_data = []
    if TEST_FILE.exists():
        with open(TEST_FILE, "r", encoding="utf-8") as f:
            content = f.read()
        files_data = [{"filename": TEST_FILE.name, "content": content}]
        print(f"   ✓ 已读取测试文件: {TEST_FILE.name} (大小: {len(content)} 字符)")
    else:
        print(f"   ⚠️  测试文件不存在，将创建空知识库")

    create_payload = {
        "user_id": user_id,
        "name": kb_name,
        "description": "这是一个测试用的知识库",
        "files": files_data,
        "chunking_method": "recursive",
        "chunk_size": 500,
        "chunk_overlap": 50,
        "enabled": True,
    }

    try:
        print(f"\n📌 发送创建请求:")
        print(f"   URL: {BASE_URL}")
        print(f"   用户ID: {user_id}")
        print(f"   知识库名称: {kb_name}")

        response = requests.post(f"{BASE_URL}", json=create_payload, timeout=30)
        print(f"\n   状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            print(f"   ✅ 创建成功!")
            print(f"   - kb_id: {result.get('kb_id')}")
            print(f"   - 总分块数: {result.get('total_chunks')}")
            print(f"   - 消息: {result.get('message')}")
            return result.get("kb_id")
        else:
            print(f"   ❌ 创建失败")
            print(f"   响应: {response.text}")
            return None

    except requests.exceptions.Timeout:
        print("   ❌ 请求超时")
        return None
    except requests.exceptions.RequestException as e:
        print(f"   ❌ 请求错误: {str(e)}")
        return None


if __name__ == "__main__":
    print("\n🚀 开始测试知识库创建接口")
    kb_id = test_create_kb()

    if kb_id:
        print(f"\n✅ 测试完成，知识库ID: {kb_id}")
    else:
        print("\n❌ 测试失败")

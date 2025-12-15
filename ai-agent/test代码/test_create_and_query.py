"""
测试知识库【仅创建】接口（不包含查询）
"""

import requests
import json
from pathlib import Path

BASE_URL = "http://127.0.0.1:8000/api/knowledge-bases"


def test_create_only():
    """仅创建知识库，不做查询"""

    print("\n" + "=" * 70)
    print("📝 测试增加和查询知识库接口")
    print("=" * 70)

    # 准备知识库数据
    user_id = "test_user"
    kb_name = "test_kb_create_query"

    # 构造“完全无文件”测试：files 发送空列表
    test_files = []
    print("   ✓ 已构造空文件列表: files=[]")

    # 步骤1: 创建知识库
    print("\n📌 步骤 1: 创建知识库")
    print("-" * 70)

    create_payload = {
        "user_id": user_id,
        "name": kb_name,
        "description": "用于测试的知识库",
        "files": test_files,
        "chunking_method": "recursive",
        "chunk_size": 500,
        "chunk_overlap": 50,
        "enabled": True,
    }

    try:
        response = requests.post(f"{BASE_URL}", json=create_payload, timeout=30)
        print(f"   状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            print(f"   ✅ 创建成功")
            print(f"   - kb_id: {result.get('kb_id')}")
            print(f"   - 总分块数: {result.get('total_chunks')}")
            print(f"   - 创建时间: {result.get('created_at')}")
            kb_id = result.get("kb_id")
        else:
            print(f"   ❌ 创建失败")
            print(f"   响应: {response.text}")
            return
    except Exception as e:
        print(f"   ❌ 请求异常: {e}")
        return

    # 步骤2: 列出知识库（确认创建成功）
    print("\n📌 步骤 2: 列出知识库")
    print("-" * 70)

    try:
        response = requests.get(f"{BASE_URL}?user_id={user_id}", timeout=30)
        print(f"   状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            kbs = result.get("knowledge_bases", [])
            print(f"   ✅ 查询成功，共 {len(kbs)} 个知识库")

            for kb in kbs:
                print(f"      - {kb.get('name')} (chunks: {kb.get('total_chunks')})")
        else:
            print(f"   ❌ 查询失败")
            print(f"   响应: {response.text}")
    except Exception as e:
        print(f"   ❌ 请求异常: {e}")

    print("\n" + "=" * 70)
    print("✅ 创建测试完成")
    print("=" * 70)


if __name__ == "__main__":
    test_create_only()

"""
测试知识库更新接口（不包含创建/删除）：
- 更新名称、描述、启用状态
- 可选：更新 chunking 参数（如 chunk_size/overlap/method）
- 更新文件内容
- 使用已有 kb_id
"""

import requests
from pathlib import Path

BASE_URL = "http://127.0.0.1:8000/api/knowledge-bases"

# 配置：请先确保 kb_id 已存在
USER_ID = "test_user"
KB_ID = "test_user_c1980f3a1ac27f97"  # TODO: 填写已有的 kb_id

# 测试文件路径
TEST_FILE = Path(__file__).parent / "test_document.txt"


def test_update_basic():
    print("\n" + "=" * 70)
    print("🛠️  测试更新知识库（基础信息）")
    print("=" * 70)

    payload = {
        "user_id": USER_ID,
        "name": "updated_kb_name",
        "description": "更新后的描述",
        "enabled": True,
    }

    resp = requests.put(f"{BASE_URL}/{KB_ID}", json=payload, timeout=30)
    print(f"状态码: {resp.status_code}")
    if resp.status_code != 200:
        print(f"❌ 失败: {resp.text}")
        return
    print(f"✅ 更新成功: {resp.json()}")


def test_update_chunking():
    print("\n" + "=" * 70)
    print("📐 测试更新 chunking 参数（会触发重建向量）")
    print("=" * 70)

    payload = {
        "user_id": USER_ID,
        "chunking_method": "recursive",
        "chunk_size": 400,
        "chunk_overlap": 80,
    }

    resp = requests.put(f"{BASE_URL}/{KB_ID}", json=payload, timeout=120)
    print(f"状态码: {resp.status_code}")
    if resp.status_code != 200:
        print(f"❌ 失败: {resp.text}")
        return
    print(f"✅ 更新并重建成功: {resp.json()}")


def test_update_with_file():
    print("\n" + "=" * 70)
    print("📄 测试更新知识库并添加文件")
    print("=" * 70)

    if not TEST_FILE.exists():
        print(f"❌ 测试文件不存在: {TEST_FILE}")
        return

    # 读取文件内容
    with open(TEST_FILE, "r", encoding="utf-8") as f:
        content = f.read()

    # files 参数应该是字典列表，包含 filename 和 content
    payload = {
        "user_id": USER_ID,
        "name": "updated_kb_with_file",
        "description": "更新后包含测试文档的知识库",
        "files": [{"filename": TEST_FILE.name, "content": content}],
        "enabled": True,
    }

    print(f"📎 将添加文件: {TEST_FILE.name} (大小: {len(content)} 字符)")
    resp = requests.put(f"{BASE_URL}/{KB_ID}", json=payload, timeout=120)
    print(f"状态码: {resp.status_code}")
    if resp.status_code != 200:
        print(f"❌ 失败: {resp.text}")
        return

    result = resp.json()
    print(f"✅ 更新成功:")
    print(f"   - 总分块数: {result.get('total_chunks', 'N/A')}")
    print(f"   - 消息: {result.get('message', 'N/A')}")


if __name__ == "__main__":
    if not KB_ID:
        print("⚠️ 请先在脚本顶部填写已有的 KB_ID 再运行。")
    else:
        test_update_basic()
        test_update_chunking()
        test_update_with_file()

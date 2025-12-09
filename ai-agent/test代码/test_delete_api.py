"""
测试知识库删除接口（直接删除已存在的知识库）
"""

import requests

BASE_URL = "http://127.0.0.1:8000/api/knowledge-bases"

# 配置：需要删除的知识库 ID
KB_ID_TO_DELETE = "test_user_be573f5d7e5f8cd6"
USER_ID = "test_user"


def test_delete():
    """直接删除指定的知识库"""

    print("\n" + "=" * 70)
    print("🗑️  测试删除知识库接口")
    print("=" * 70)

    if not KB_ID_TO_DELETE:
        print("⚠️ 请先在脚本顶部设置 KB_ID_TO_DELETE")
        return

    # 步骤1: 删除知识库
    print(f"\n📌 步骤 1: 删除知识库 {KB_ID_TO_DELETE}")
    print("-" * 70)

    try:
        response = requests.delete(f"{BASE_URL}/{KB_ID_TO_DELETE}", timeout=30)
        print(f"   状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                print(f"   ✅ 删除成功")
                print(f"   - 消息: {result.get('message')}")
            else:
                print(f"   ⚠️  删除返回失败")
                print(f"   - 错误: {result.get('error')}")
        else:
            print(f"   ❌ 删除失败")
            print(f"   响应: {response.text}")
            return
    except Exception as e:
        print(f"   ❌ 请求异常: {e}")
    # 步骤2: 验证删除 - 尝试再次删除应该失败
    print("\n📌 步骤 2: 验证删除（再次尝试删除应失败）")
    print("-" * 70)

    try:
        response = requests.delete(f"{BASE_URL}/{KB_ID_TO_DELETE}", timeout=30)
        print(f"   状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            if result.get("success") is False:
                print(f"   ✅ 验证成功 - 知识库已被删除")
                print(f"   - 错误信息: {result.get('error')}")
            else:
                print(f"   ⚠️  警告 - 不应该再次成功删除")
        else:
            print(f"   ⚠️  返回错误状态码（这可能表示知识库已删除）")
            print(f"   - 状态码: {response.status_code}")
    except Exception as e:
        print(f"   ❌ 请求异常: {e}")
        print(f"   ❌ 请求异常: {e}")

    # 步骤3: 列出知识库验证
    print("\n📌 步骤 3: 列出知识库验证删除")
    print("-" * 70)

    try:
        response = requests.get(f"{BASE_URL}?user_id={USER_ID}", timeout=30)
        print(f"   状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            kbs = result.get("knowledge_bases", [])

            # 检查被删除的KB是否还在列表中
            deleted_kb = [k for k in kbs if k.get("kb_id") == KB_ID_TO_DELETE]

            if not deleted_kb:
                print(f"   ✅ 验证成功 - 已删除的知识库不在列表中")
            else:
                print(f"   - 用户 {USER_ID} 现有知识库数: {len(kbs)}")
            if kbs:
                print(f"   - 现有知识库:")
                for kb in kbs:
                    print(f"     * {kb.get('name')} ({kb.get('kb_id')})")
        else:
            print(f"   ❌ 查询失败")
            print(f"   响应: {response.text}")
    except Exception as e:
        print(f"   ❌ 请求异常: {e}")

    print("\n" + "=" * 70)
    print("✅ 测试完成")
    print("=" * 70)


if __name__ == "__main__":
    test_delete()

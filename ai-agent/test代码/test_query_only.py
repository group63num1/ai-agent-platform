"""
仅测试查询接口：
1) 有 query_text 且带 similarity_threshold：取 top5，再按阈值过滤，返回满足阈值的结果
2) 无 query_text：返回各知识库前 20 个 chunk
"""

import requests

BASE_URL = "http://127.0.0.1:8000/api/knowledge-bases/query"

# 配置区域
USER_ID = "test_user"
KB_IDS = []  # 可选：指定要查询的 kb_id 列表；为空则使用用户启用的全部知识库


def query_with_text(query_text: str, threshold: float = 0.7):
    print("\n" + "-" * 70)
    print(f"🔍 查询（带文本）: '{query_text}' | 阈值: {threshold}")
    payload = {
        "user_id": USER_ID,
        "query_text": query_text,
        "similarity_threshold": threshold,
    }
    if KB_IDS:
        payload["kb_ids"] = KB_IDS

    resp = requests.post(BASE_URL, json=payload, timeout=30)
    print(f"状态码: {resp.status_code}")
    if resp.status_code != 200:
        print(f"❌ 失败: {resp.text}")
        return

    data = resp.json()
    results = data.get("results", [])
    print(f"✅ 返回 {len(results)} 条（top5 过滤后）")
    for i, item in enumerate(results, 1):
        sim = item.get("similarity") or item.get("similarity_score")
        text = item.get("text", "")
        kb_name = item.get("kb_name") or item.get("kb_id")
        print(f"  #{i} kb: {kb_name} sim: {sim if sim is not None else 'N/A'}")
        if text:
            preview = text if len(text) <= 120 else text[:120] + "..."
            print(f"     text: {preview}")


def query_without_text(limit: int = 20):
    print("\n" + "-" * 70)
    print(f"📦 无查询词，取前 {limit} 条 chunk")
    payload = {
        "user_id": USER_ID,
        "limit": limit,
        "query_text": None,
    }
    if KB_IDS:
        payload["kb_ids"] = KB_IDS

    resp = requests.post(BASE_URL, json=payload, timeout=30)
    print(f"状态码: {resp.status_code}")
    if resp.status_code != 200:
        print(f"❌ 失败: {resp.text}")
        return

    data = resp.json()
    results = data.get("results", [])
    print(f"✅ 返回 {len(results)} 条 chunk")
    for i, item in enumerate(results[:5], 1):  # 预览前 5 条
        kb_name = item.get("kb_name") or item.get("kb_id")
        text = item.get("text", "")
        print(f"  #{i} kb: {kb_name}")
        if text:
            preview = text if len(text) <= 120 else text[:120] + "..."
            print(f"     text: {preview}")


if __name__ == "__main__":
    # 示例：有 query 的两次调用
    query_with_text("Python 函数定义", threshold=0.7)
    query_with_text("装饰器和生成器", threshold=0.6)

    # 示例：无 query，取前 20 条 chunk
    query_without_text(limit=20)

from pathlib import Path
import sys

# 添加项目根目录到 Python 路径
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from core.knowledge_service import KnowledgeBaseService


def find_pdfs(root="."):
    p = Path(root)
    if not p.exists():
        print(f"目录不存在: {root}")
        return []
    return list(p.rglob("*.pdf"))


def main():
    pdfs = find_pdfs()
    if not pdfs:
        print(
            "未发现 PDF 文件。请将 PDF 放到 knowledge/origin_document 下，或传入具体文件路径。"
        )
        return

    svc = KnowledgeBaseService()
    for pdf in pdfs:
        print("=== 解析: ", pdf)
        text = svc.load_document_content(str(pdf))
        if not text:
            print("解析结果为空（可能缺少 PyPDF2 或解析失败）")
        else:
            print(text[:1000])
            print("... (截断)")


if __name__ == "__main__":
    main()

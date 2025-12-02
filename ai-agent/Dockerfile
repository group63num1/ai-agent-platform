# 使用 Python 3.11 作为基础镜像
FROM python:3.11-slim

LABEL maintainer="ai-agent-platform"

# 设置工作目录
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# 复制依赖文件
COPY requirements.txt .

# ==================== 关键修改开始 ====================
# 1. 先单独安装 torch（使用官方 CPU 版本，体积小）
RUN pip install --no-cache-dir \
    --index-url https://download.pytorch.org/whl/cpu \
    torch==2.0.1+cpu

# 2. 创建不包含 torch 的临时 requirements 文件
RUN grep -v "^torch" requirements.txt > /tmp/requirements_no_torch.txt

# 3. 安装其他依赖（使用阿里云镜像，通常更快）
RUN pip install --no-cache-dir \
    -i https://mirrors.aliyun.com/pypi/simple/ \
    --trusted-host mirrors.aliyun.com \
    --default-timeout=300 \
    -r /tmp/requirements_no_torch.txt
# ==================== 关键修改结束 ====================

# 复制应用代码
COPY . .

# 创建数据目录（用于 SQLite，如果使用）
RUN mkdir -p /app/data

# 暴露端口
EXPOSE 8000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD python -c "import requests; requests.get('http://localhost:8000/health', timeout=5)" || exit 1

# 启动命令
CMD ["python", "main.py"]
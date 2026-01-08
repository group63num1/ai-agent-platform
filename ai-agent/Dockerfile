FROM python:3.10-slim

# 基础环境
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PIP_DEFAULT_TIMEOUT=120

WORKDIR /app

# 可选自定义源（构建时传入，便于加速）
ARG PIP_INDEX_URL
ARG PIP_EXTRA_INDEX_URL
# 将镜像源写入环境变量，pip 会自动读取
ENV PIP_INDEX_URL=${PIP_INDEX_URL}
ENV PIP_EXTRA_INDEX_URL=${PIP_EXTRA_INDEX_URL}

# 精简系统依赖（pymilvus 需 curl，常用轮子无需编译）
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# 先装依赖，利用缓存提速
COPY requirements.txt .
RUN pip install --no-cache-dir --prefer-binary --retries 3 --default-timeout 120 -r requirements.txt

# 拷贝项目代码
COPY . .

EXPOSE 8000

# 使用 python main.py 启动（内部会调用 uvicorn）
CMD ["python", "main.py"]

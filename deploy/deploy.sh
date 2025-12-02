#!/bin/bash

set -e  # 遇到错误立即退出

# ==================== 颜色定义 ====================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'  # 无颜色

# ==================== 日志函数 ====================
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_step() {
    echo -e "\n${BLUE}==================== $1 ====================${NC}\n"
}

# ==================== 检查命令是否存在 ====================
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 未安装，请先安装 $1"
        exit 1
    fi
}

# ==================== 彻底清理容器 ====================
cleanup_containers() {
    log_step "5. 停止和清理旧服务"
    
    log_info "步骤 1: 使用 docker-compose 停止服务..."
    docker-compose down --remove-orphans --timeout 30 || true
    
    log_info "步骤 2: 清理所有可能冲突的容器..."
    
    # 定义所有可能的容器名称
    containers=(
        "ai-agent-mysql"
        "ai-agent-backend" 
        "ai-agent-web-admin"
        "ai-agent-ai-agent"
        "ai-agent-mobile"
        "ai-agent-phpmyadmin"
    )
    
    for container in "${containers[@]}"; do
        if docker ps -a | grep -q "$container"; then
            log_warn "强制删除容器: $container"
            docker rm -f "$container" 2>/dev/null || true
        fi
    done
    
    log_info "步骤 3: 使用项目名称清理..."
    # 使用 docker-compose 项目名称清理（如果有定义的话）
    if [ -f "docker-compose.yml" ]; then
        local project_name=$(basename "$PWD" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]//g')
        log_info "检测到项目名称: $project_name"
        
        # 清理该项目下的所有容器
        docker ps -a --filter "label=com.docker.compose.project=$project_name" --format "{{.Names}}" | xargs -r docker rm -f
    fi
    
    log_info "步骤 4: 最终确认清理..."
    # 最终检查是否还有冲突容器
    conflicting_containers=$(docker ps -a --format "{{.Names}}" | grep -E "ai-agent-|web-admin|mysql|backend|mobile|phpmyadmin" || true)
    
    if [ -n "$conflicting_containers" ]; then
        log_warn "发现以下可能冲突的容器，正在清理:"
        echo "$conflicting_containers"
        echo "$conflicting_containers" | xargs -r docker rm -f
    fi
    
    log_info "容器清理完成 ✓"
}

# ==================== 开始部署 ====================
log_step "开始部署"
log_info "时间: $(date '+%Y-%m-%d %H:%M:%S')"

# ==================== 1. 环境检查 ====================
log_step "1. 环境检查"

log_info "检查 Docker..."
check_command docker
docker --version

log_info "检查 Docker Compose..."
check_command docker-compose
docker-compose --version

log_info "检查 .env 文件..."
if [ ! -f .env ]; then
    log_error ".env 文件不存在，请先创建配置文件"
    exit 1
fi
log_info ".env 文件存在 ✓"

# ==================== 2. 准备 Maven 缓存 ====================
log_step "2. 准备 Maven 缓存"

MAVEN_CACHE_VOLUME="health-agent-maven-repo"

if docker volume inspect $MAVEN_CACHE_VOLUME &> /dev/null; then
    log_info "Maven 缓存卷已存在: $MAVEN_CACHE_VOLUME"
else
    log_info "创建 Maven 缓存卷: $MAVEN_CACHE_VOLUME"
    docker volume create $MAVEN_CACHE_VOLUME
fi

# ==================== 3. 容器化构建后端 JAR 包 ====================
log_step "3. 容器化构建后端 JAR 包"

log_info "使用 Maven Docker 容器构建后端..."
log_warn "首次构建可能需要 5-10 分钟（下载依赖）"

docker run --rm \
    --network=host \
    -v $MAVEN_CACHE_VOLUME:/root/.m2 \
    -v "$PWD/../backend":/usr/src/app \
    -w /usr/src/app \
    maven:3.9-eclipse-temurin-17 \
    mvn clean package -DskipTests -q

# 检查 JAR 包是否生成
if ls ../backend/target/*.jar 1> /dev/null 2>&1; then
    log_info "后端 JAR 包构建成功 ✓"
    ls -lh ../backend/target/*.jar
else
    log_error "后端 JAR 包构建失败"
    exit 1
fi

# ==================== 4. 构建 Docker 镜像 ====================
log_step "4. 构建 Docker 镜像"

log_info "开始构建所有服务的 Docker 镜像..."
log_warn "前端构建可能需要 5-10 分钟（下载 npm 依赖）"
log_warn "AI Agent 构建可能需要 5-10 分钟（下载 Python 依赖）"

# 构建时不使用缓存，确保获取最新代码
docker-compose build --no-cache

log_info "所有镜像构建完成 ✓"

# ==================== 5. 清理阶段 ====================
cleanup_containers

# ==================== 6. 启动新服务 ====================
log_step "6. 启动新服务"

log_info "启动所有服务..."
docker-compose up -d

# 等待服务启动
log_info "等待服务启动..."
sleep 10

# ==================== 7. 验证部署 ====================
log_step "7. 验证部署"

log_info "检查服务状态..."
docker-compose ps

# 检查容器是否正常运行
if docker-compose ps | grep -q "Up"; then
    log_info "所有服务启动成功 ✓"
else
    log_error "部分服务启动失败，请检查日志"
    docker-compose logs
    exit 1
fi

# ==================== 8. 部署完成 ====================
log_step "部署完成！"

echo -e "${GREEN}"
echo "========================================"
echo "✓ 部署成功！服务访问地址："
echo "========================================"
echo "前端管理端:  http://localhost"
echo "后端 API:    http://localhost:8080"
echo "API 文档:    http://localhost:8080/doc.html"
echo "AI Agent:    http://localhost:8000"
echo "AI 文档:     http://localhost:8000/docs"
echo "PHPMyAdmin:  http://localhost:8081"
echo "========================================"
echo -e "${NC}"

log_info "查看日志命令: docker-compose logs -f"
log_info "停止服务命令: docker-compose down"

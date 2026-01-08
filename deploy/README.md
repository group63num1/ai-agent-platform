# 自动化部署说明

本项目包含完整的自动化部署配置，支持一键部署到云服务器。

## 📁 项目结构

```
ai-agent-platform/
├── frontend/          # 前端项目 (Vue3)
├── backend/           # 后端项目 (Spring Boot)
├── database/          # 数据库初始化脚本
├── ai-agent/          # AI 代理服务 (FastAPI)
└── deploy/            # 自动化部署配置
    ├── docker-compose.yml    # Docker Compose 配置
    ├── deploy.sh             # 部署脚本
    ├── Makefile              # 简化命令接口
    └── env.example           # 环境变量模板
```

## 🚀 快速开始

### 1. 环境准备

确保服务器已安装：
- Docker (>= 20.10)
- Docker Compose (>= 2.0)

### 2. 配置环境变量

```bash
cd deploy
cp env.example .env
# 编辑 .env 文件，根据实际情况修改配置
```

### 3. 一键部署

```bash
# 方式一：使用 Makefile（推荐）
make deploy

# 方式二：直接运行脚本
./deploy.sh
```

> 数据库初始化：MySQL 容器首次启动时会自动执行 `database/backend_init/demo_db.sql`（后端 demo_db）与 `database/sql/init.sql`（AI Agent new_db），无需手工导入。

## 📋 部署脚本功能

`deploy.sh` 脚本会自动完成以下步骤：

1. ✅ **环境检查** - 检查 Docker 和 Docker Compose
2. ✅ **Maven 缓存准备** - 创建 Maven 缓存卷
3. ✅ **构建后端 JAR** - 使用 Maven 容器构建 Spring Boot 应用
4. ✅ **构建 Docker 镜像** - 构建所有服务的镜像
5. ✅ **清理旧服务** - 停止并删除旧容器
6. ✅ **启动新服务** - 启动所有服务
7. ✅ **验证部署** - 检查服务状态

## 🐳 服务说明

部署后，以下服务会自动启动：

| 服务 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| MySQL | ai-agent-mysql | 3306 | 数据库服务 |
| PHPMyAdmin | ai-agent-phpmyadmin | 8081 | 数据库管理工具 |
| Milvus | ai-agent-milvus-standalone | 19530/9091 | 向量数据库 |
| MinIO（Milvus依赖） | ai-agent-milvus-minio | 9000/9001 | 对象存储 |
| Backend | ai-agent-backend | 8080 | Spring Boot 后端 API |
| AI Agent | ai-agent-ai-agent | 8000 | FastAPI AI 代理服务 |
| Frontend | ai-agent-web-admin | 80 | Vue3 前端管理界面 |

## 🔧 常用命令

### 使用 Makefile（推荐）

```bash
# 查看帮助
make help

# 部署相关
make deploy              # 一键部署
make build              # 构建所有镜像
make build-backend      # 只构建后端
make build-frontend     # 只构建前端
make build-ai-agent     # 只构建 AI Agent

# 服务控制
make start              # 启动所有服务
make stop               # 停止所有服务
make restart            # 重启所有服务
make ps                 # 查看服务状态

# 日志查看
make logs               # 查看所有服务日志
make logs-backend       # 查看后端日志
make logs-frontend      # 查看前端日志
make logs-ai-agent      # 查看 AI Agent 日志
make logs-mysql         # 查看数据库日志

# 运维管理
make status             # 检查服务健康状态
make backup             # 备份数据库
make clean              # 清理所有容器和镜像
```

### 使用 Docker Compose

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 查看日志
docker-compose logs -f [service_name]

# 重启服务
docker-compose restart [service_name]

# 查看状态
docker-compose ps
```

## 🌐 访问地址

部署成功后，可通过以下地址访问：

- **前端管理端**: http://your-server-ip
- **后端 API**: http://your-server-ip:8080
- **API 文档**: http://your-server-ip:8080/doc.html
- **AI Agent API**: http://your-server-ip:8000
- **AI Agent 文档**: http://your-server-ip:8000/docs
- **PHPMyAdmin**: http://your-server-ip:8081
- **Milvus metrics**: http://your-server-ip:9091/healthz

## ⚙️ 环境变量配置

主要环境变量说明（详见 `env.example`）：

### 数据库配置
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_ROOT_PASSWORD`: MySQL root 密码
- `AI_AGENT_DB_NAME`: AI Agent 专用数据库（默认 new_db，自动用 init.sql 初始化）

### 服务端口
- `BACKEND_PORT`: 后端服务端口（默认 8080）
- `AI_AGENT_PORT`: AI Agent 服务端口（默认 8000）
- `WEB_ADMIN_PORT`: 前端服务端口（默认 80）

### AI Agent 配置
- `AI_AGENT_DEBUG`: 调试模式（true/false）
- `AI_AGENT_LOG_LEVEL`: 日志级别（INFO/DEBUG/WARNING）
- `AI_AGENT_CORS_ORIGINS`: CORS 允许的源（多个用逗号分隔）
- `MILVUS_ENABLE`/`MILVUS_HOST`/`MILVUS_PORT`: 向量数据库连接配置

## 🔍 故障排查

### 查看服务日志

```bash
# 查看所有服务日志
make logs

# 查看特定服务日志
make logs-backend
make logs-ai-agent
```

### 检查服务状态

```bash
# 查看容器状态
make ps

# 检查服务健康状态
make status
```

### 常见问题

1. **端口被占用**
   - 修改 `.env` 文件中的端口配置

2. **数据库连接失败**
   - 检查 MySQL 容器是否正常启动
   - 检查数据库密码配置是否正确

3. **构建失败**
   - 检查网络连接（需要下载依赖）
   - 查看构建日志：`docker-compose build --no-cache`

## 📝 注意事项

1. **首次部署**：首次部署可能需要较长时间（下载依赖）
2. **数据持久化**：数据库数据存储在 Docker 卷中，删除容器不会丢失数据
3. **备份数据**：定期使用 `make backup` 备份数据库
4. **生产环境**：生产环境请修改默认密码和配置

## 🔄 更新部署

当代码更新后，重新部署：

```bash
# 方式一：使用 Makefile
make deploy

# 方式二：手动更新
git pull
cd deploy
./deploy.sh
```

## 📞 技术支持

如有问题，请查看：
- 服务日志：`make logs`
- Docker 日志：`docker-compose logs`
- 容器状态：`docker-compose ps`


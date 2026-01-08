#!/usr/bin/env bash
set -euo pipefail

# 零参数一键启动后端：默认连接 docker 的 MySQL (localhost:3306 / demo_db)
# 使用：
#   cd /root/ai-agent-platform/backend && ./run_with_docker_mysql.sh

cd "$(dirname "$0")"

########################################
# 自动检测 Docker MySQL 容器与账号密码
########################################

# 默认 JDBC URL（指向宿主机 3306 -> 容器 mysql:3306）
DATASOURCE_URL="jdbc:mysql://localhost:3306/demo_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"

# 寻找映射到 0.0.0.0:3306 的 MySQL 容器名（优先 ai-agent-mysql）
MYSQL_CONTAINER=$(docker ps --format '{{.Names}} {{.Ports}}' | awk '/0.0.0.0:3306->3306/{print $1; exit}')
if [[ -z "${MYSQL_CONTAINER:-}" ]]; then
  # 兜底：尝试常见容器名
  for name in ai-agent-mysql mysql8-demo-2 mysql; do
    if docker inspect "$name" >/dev/null 2>&1; then
      MYSQL_CONTAINER="$name"
      break
    fi
  done
fi

# 读取容器内的 root 密码（MYSQL_ROOT_PASSWORD），用于检测 demo_user 是否存在
ROOT_PASS=""
if [[ -n "${MYSQL_CONTAINER:-}" ]]; then
  ROOT_PASS=$(docker inspect "$MYSQL_CONTAINER" --format '{{range .Config.Env}}{{println .}}{{end}}' | awk -F= '$1=="MYSQL_ROOT_PASSWORD"{print $2}')
fi

# 默认尝试业务账号 demo_user/demo_pass_123；若容器未创建该用户，则回退到 root/ROOT_PASS
DATASOURCE_USERNAME="demo_user"
DATASOURCE_PASSWORD="demo_pass_123"

if [[ -n "${MYSQL_CONTAINER:-}" && -n "${ROOT_PASS:-}" ]]; then
  # 在容器内用 root 检查是否存在 demo_user
  if docker exec "$MYSQL_CONTAINER" mysql -uroot -p"$ROOT_PASS" -e "SELECT COUNT(*) FROM mysql.user WHERE user='demo_user';" >/tmp/mysql_user_check 2>/dev/null; then
    COUNT=$(tail -n1 /tmp/mysql_user_check | awk '{print $1}')
    if [[ "$COUNT" != "1" ]]; then
      # demo_user 不存在，切换到 root
      DATASOURCE_USERNAME="root"
      DATASOURCE_PASSWORD="$ROOT_PASS"
    fi
  else
    # 无法用 root 检查，直接回退 root
    DATASOURCE_USERNAME="root"
    DATASOURCE_PASSWORD="$ROOT_PASS"
  fi
fi

export DATASOURCE_URL
export DATASOURCE_USERNAME
export DATASOURCE_PASSWORD

echo "======================================"
echo "Starting Backend (one-click)"
echo "DB Host        : localhost"
echo "DB Port        : 3306"
echo "DB Name        : demo_db"
echo "DB User        : $DATASOURCE_USERNAME"
echo "DATASOURCE_URL : $DATASOURCE_URL"
if [[ -n "${MYSQL_CONTAINER:-}" ]]; then
  echo "Docker MySQL   : $MYSQL_CONTAINER"
fi
echo "======================================"

# 端口连通性提示（不阻断）
if command -v ss >/dev/null 2>&1; then
  if ! ss -ltn | grep -q ":3306 "; then
    echo "[WARN] 3306 未监听，确保 docker MySQL(ai-agent-mysql) 已启动。" >&2
  fi
fi

# 启动后端（开发方式）
if [[ -x ./mvnw ]]; then
  exec ./mvnw spring-boot:run
elif command -v mvn >/dev/null 2>&1; then
  exec mvn spring-boot:run
else
  echo "[ERROR] 未找到 mvnw 或 mvn，请安装 Maven 或使用项目自带的 ./mvnw。" >&2
  exit 1
fi

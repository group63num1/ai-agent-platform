#!/usr/bin/env bash
set -euo pipefail

# AI agent deploy script: optional deps install, restart service
# Usage: ./deploy.sh [--install]

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [[ "${1:-}" == "--install" ]]; then
  "$ROOT_DIR/.venv/bin/pip" install -r "$ROOT_DIR/ai-agent/requirements.txt"
fi

systemctl restart ai-agent.service
systemctl status ai-agent.service --no-pager --lines=50

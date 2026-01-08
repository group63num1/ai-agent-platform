#!/usr/bin/env bash
set -euo pipefail

# Frontend deploy script: build and restart nginx
# Usage: ./deploy.sh [--install]

cd "$(dirname "$0")"

if [[ "${1:-}" == "--install" ]] || [[ ! -d node_modules ]]; then
  npm install
fi

npm run build
systemctl restart nginx
systemctl status nginx --no-pager --lines=20

#!/usr/bin/env bash
set -euo pipefail

# Backend deploy script: build jar and restart service
# Usage: ./deploy.sh [--skip-build]

cd "$(dirname "$0")"

if [[ "${1:-}" != "--skip-build" ]]; then
  ./mvnw -DskipTests package
fi

systemctl restart ai-backend.service
systemctl status ai-backend.service --no-pager --lines=50

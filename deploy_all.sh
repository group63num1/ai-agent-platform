#!/usr/bin/env bash
set -euo pipefail

# One-click deploy for all services
# Flags:
#   --install-ai    Install/upgrade Python deps for ai-agent
#   --install-fe    Run npm install for frontend
#   --backend-prod  Use prod profile for backend (requires service to read env)

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

INSTALL_AI=false
INSTALL_FE=false
BACKEND_PROFILE=""

for arg in "$@"; do
  case "$arg" in
    --install-ai) INSTALL_AI=true ;;
    --install-fe) INSTALL_FE=true ;;
    --backend-prod) BACKEND_PROFILE="prod" ;;
    *) echo "Unknown arg: $arg" ; exit 1 ;;
  esac
done

pushd "$ROOT_DIR/backend" >/dev/null
  if [[ -n "$BACKEND_PROFILE" ]]; then
    echo "Deploying backend with profile=$BACKEND_PROFILE"
    SPRING_PROFILES_ACTIVE="$BACKEND_PROFILE" ./deploy.sh
  else
    ./deploy.sh
  fi
popd >/dev/null

pushd "$ROOT_DIR/ai-agent" >/dev/null
  if $INSTALL_AI; then
    ./deploy.sh --install
  else
    ./deploy.sh
  fi
popd >/dev/null

pushd "$ROOT_DIR/frontend" >/dev/null
  if $INSTALL_FE; then
    ./deploy.sh --install
  else
    ./deploy.sh
  fi
popd >/dev/null

echo "All services deployed."\n
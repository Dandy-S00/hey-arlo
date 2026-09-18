#!/usr/bin/env sh
set -eu

APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$APP_DIR"

command -v docker >/dev/null 2>&1 || { echo "Docker is required." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "Docker Compose is required." >&2; exit 1; }

if [ "${1:-}" = "--pull" ]; then
  git pull --ff-only
fi

docker compose up -d --build --remove-orphans
docker compose ps

echo "Arlo is available on port ${ARLO_PORT:-8080}."

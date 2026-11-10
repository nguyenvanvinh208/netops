#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
set -a; source .env; set +a
git pull --ff-only
docker compose up -d --build
docker image prune -f
sleep 8
curl -fsS "https://${DOMAIN}/swagger/v1/swagger.json" > /dev/null && echo "OK: https://${DOMAIN}" || echo "Chua len HTTPS, xem: docker compose logs"

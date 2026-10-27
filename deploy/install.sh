#!/usr/bin/env bash
set -euo pipefail
cd /opt/rtsmartpro
umask 077
if [ ! -f .env ]; then
  printf 'DB_PASSWORD=%s\n' "$(openssl rand -hex 32)" > .env
fi
sudo docker compose up -d --build
test -f bootstrap.env
set -a
source bootstrap.env
set +a
sudo docker compose exec -T -e ADMIN_PHONE -e ADMIN_NAME -e COMMUNITY_NAME -e SEED_DEMO api python bootstrap.py > initial-access.txt
chmod 600 initial-access.txt
if ! grep -q '^rtsmartpro.amarlo.online {' /home/ubuntu/AI-chat/Caddyfile; then
  cp /home/ubuntu/AI-chat/Caddyfile /home/ubuntu/AI-chat/Caddyfile.before-rtsmartpro
  printf '\n' >> /home/ubuntu/AI-chat/Caddyfile
  cat deploy/Caddyfile.fragment >> /home/ubuntu/AI-chat/Caddyfile
fi
sudo docker exec ai-chat-caddy caddy validate --config /etc/caddy/Caddyfile
sudo docker exec ai-chat-caddy caddy reload --config /etc/caddy/Caddyfile
curl --fail --retry 10 --retry-delay 3 http://127.0.0.1:38081/api/v1/health

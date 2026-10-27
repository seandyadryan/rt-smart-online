#!/usr/bin/env bash
set -euo pipefail
target=/opt/rtsmartpro
source_dir="$(cd "$(dirname "$0")/.." && pwd)"
test -f "$target/.env"
cd "$target"
bash deploy/backup.sh
if [ "$source_dir" != "$target" ]; then
  cp -a "$source_dir/backend" "$target/"
  cp -a "$source_dir/deploy" "$target/"
  cp "$source_dir/compose.yml" "$target/compose.yml"
fi
sudo docker compose up -d --build
python3 deploy/update-caddy.py
for attempt in $(seq 1 15); do
  if curl -fsS http://127.0.0.1:38081/api/v1/health; then exit 0; fi
  sleep 3
done
exit 1

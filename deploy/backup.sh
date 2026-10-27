#!/usr/bin/env bash
set -euo pipefail
cd /opt/rtsmartpro
umask 077
mkdir -p backups
target="backups/rtsmartpro-$(date -u +%Y%m%dT%H%M%SZ).sql.gz"
sudo docker compose exec -T db pg_dump -U rtsmartpro --no-owner --no-acl rtsmartpro | gzip > "$target"
test -s "$target"
# Daily rotation removes files after 13 full days, retaining at most 14 calendar days.
find /opt/rtsmartpro/backups -maxdepth 1 -name 'rtsmartpro-*.sql.gz' -mtime +12 -delete

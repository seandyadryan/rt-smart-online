#!/usr/bin/env bash
set -euo pipefail
cd /opt/rtsmartpro
checkdb=rtsmartpro_restore_check
sudo docker compose exec -T db createdb -U rtsmartpro "$checkdb"
trap 'sudo docker compose exec -T db dropdb -U rtsmartpro "$checkdb"' EXIT
latest=$(find backups -maxdepth 1 -name '*.sql.gz' | sort | tail -1)
test -n "$latest"
gzip -dc "$latest" | sudo docker compose exec -T db psql -v ON_ERROR_STOP=1 -U rtsmartpro -d "$checkdb" >/tmp/rtsmartpro-restore.log
printf 'SELECT count(*) AS resident_count FROM residents;\n' | sudo docker compose exec -T db psql -U rtsmartpro -d "$checkdb"

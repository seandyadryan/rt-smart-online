#!/usr/bin/env bash
set -euo pipefail
cd /opt/rtsmartpro
sudo docker compose exec -T db psql -v ON_ERROR_STOP=1 -U rtsmartpro -d postgres <<'SQL'
SELECT 'CREATE DATABASE rtsmartpro_test' WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname='rtsmartpro_test')\gexec
SQL
sudo docker compose exec -T api sh -c 'DATABASE_URL="${DATABASE_URL%/*}/rtsmartpro_test" TWILIO_ACCOUNT_SID= TWILIO_AUTH_TOKEN= TWILIO_VERIFY_SERVICE_SID= pytest -q -p no:cacheprovider'

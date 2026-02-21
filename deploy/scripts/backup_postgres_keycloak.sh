#!/usr/bin/env bash
set -euo pipefail

# Security: ensure dumps are private to root.
umask 077

ENV_FILE="${ENV_FILE:-/opt/balmaya/.env}"
COMPOSE_FILE="${COMPOSE_FILE:-/opt/balmaya/docker-compose.prod.yml}"
BACKUP_ROOT="${BACKUP_ROOT:-/data/backups/postgres_keycloak}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $ENV_FILE" >&2
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

mkdir -p "$BACKUP_ROOT"
ARCHIVE="$BACKUP_ROOT/postgres_keycloak_${TIMESTAMP}.sql.gz"

docker compose -f "$COMPOSE_FILE" exec -T postgres_keycloak \
  pg_dump -U "$KC_DB_USER" -d "$KC_DB_NAME" | gzip -9 > "$ARCHIVE"

find "$BACKUP_ROOT" -type f -name 'postgres_keycloak_*.sql.gz' -mtime +"$RETENTION_DAYS" -delete

echo "Created $ARCHIVE"

#!/usr/bin/env bash
set -euo pipefail

# Installs daily backups for both PostgreSQL databases.
# Run as root on the VM.

install -o root -g root -m 0750 /opt/balmaya/scripts/backup_postgres_backend.sh /usr/local/bin/backup_postgres_backend.sh
install -o root -g root -m 0750 /opt/balmaya/scripts/backup_postgres_keycloak.sh /usr/local/bin/backup_postgres_keycloak.sh

cat >/etc/cron.d/balmaya-db-backups <<'CRON'
SHELL=/bin/bash
PATH=/usr/sbin:/usr/bin:/sbin:/bin:/usr/local/bin

# Backend DB at 01:20 UTC
20 1 * * * root RETENTION_DAYS=14 /usr/local/bin/backup_postgres_backend.sh >> /var/log/balmaya-backups.log 2>&1

# Keycloak DB at 01:50 UTC
50 1 * * * root RETENTION_DAYS=14 /usr/local/bin/backup_postgres_keycloak.sh >> /var/log/balmaya-backups.log 2>&1
CRON

chmod 0644 /etc/cron.d/balmaya-db-backups
systemctl restart cron

echo "Cron configured at /etc/cron.d/balmaya-db-backups"

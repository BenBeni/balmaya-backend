# Production Infrastructure (Hetzner + Terraform + Docker Compose + GHCR + GitHub Actions)

This directory layout is production-focused and intentionally strict:

- `infra/terraform`: Hetzner infrastructure (VM, firewall, SSH key, optional volume, cloud-init hardening).
- `deploy/docker-compose.prod.yml`: Production containers and networks.
- `deploy/scripts`: Backup scripts and cron installer for both PostgreSQL databases.
- `.github/workflows/deploy-prod.yml`: Build/push image to GHCR and deploy app stack over SSH.
- `.github/workflows/terraform.yml`: Terraform plan/apply with schedule-based drift detection.

## Security Decisions (important)

- VM ingress is restricted to `22`, `80`, `443` only.
- UFW deny-by-default is enforced by cloud-init.
- SSH password authentication is disabled; key-only SSH.
- Fail2ban and unattended-upgrades are enabled at bootstrap.
- No DB ports are published publicly.
- App container is non-root and read-only filesystem.
- Secrets are injected through `.env` generated from GitHub Secrets at deploy time.
- Terraform `apply` is gated by the protected GitHub Environment `terraform-prod`.

## Required GitHub Secrets (Terraform Workflow)

- `HCLOUD_TOKEN`
- `TF_VAR_SSH_PUBLIC_KEY`
- `TF_STATE_BUCKET`
- `TF_STATE_KEY` (e.g. `balmaya/prod/terraform.tfstate`)
- `TF_STATE_REGION` (e.g. `eu-central`)
- `TF_STATE_ENDPOINT` (S3 endpoint, e.g. Hetzner Object Storage)
- `TF_STATE_ACCESS_KEY_ID`
- `TF_STATE_SECRET_ACCESS_KEY`

## Recommended GitHub Variables (Terraform Workflow)

- `TF_VAR_SSH_KEY_NAME`
- `TF_VAR_SERVER_NAME`
- `TF_VAR_SERVER_TYPE` (e.g. `cx31`)
- `TF_VAR_LOCATION` (e.g. `nbg1`)
- `TF_VAR_IMAGE` (e.g. `ubuntu-22.04`)
- `TF_VAR_ENABLE_DATA_VOLUME` (`true`/`false`)
- `TF_VAR_DATA_VOLUME_SIZE_GB` (e.g. `40`)

## Required GitHub Secrets (App Deploy Workflow)

- `HETZNER_HOST`
- `HETZNER_SSH_USER`
- `HETZNER_SSH_PRIVATE_KEY`
- `BACKEND_DB_PASSWORD`
- `KC_DB_PASSWORD`
- `KEYCLOAK_ADMIN_PASSWORD`
- `KEYCLOAK_CLIENTAPP_CLIENT_SECRET`
- `KEYCLOAK_CLIENT_SECRET`
- `GHCR_USERNAME` (PAT owner or bot username)
- `GHCR_TOKEN` (PAT with `read:packages` scope)
- `APP_HOST`
- `KEYCLOAK_HOSTNAME`

## Terraform Usage

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
# edit terraform.tfvars
terraform init
terraform plan
terraform apply
```

## Backup/Retention Policy

- Backend DB backup script: `deploy/scripts/backup_postgres_backend.sh`
- Keycloak DB backup script: `deploy/scripts/backup_postgres_keycloak.sh`
- Cron installer: `deploy/scripts/setup_backups_cron.sh`
- Default retention: 14 days (`RETENTION_DAYS=14`), old dumps auto-deleted.
- Backup storage path defaults to `/data/backups/...`.

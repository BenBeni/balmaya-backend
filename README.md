# balmaya (Spring Boot)

Sender-only fintech backend:
- Spring Boot 3 / Java 17
- Keycloak (JWT)
- PostgreSQL + Flyway
- Redis + Redisson distributed locks
- Immutable row versioning (logical_id + version + is_current)

## Run locally (Docker)

```bash
docker compose up --build
```

Services:
- Keycloak: http://localhost:8080
- API: http://localhost:8081
- Postgres: localhost:5432
- Redis: localhost:6379

## Keycloak setup (manual)
1. Login to Keycloak admin console: user `admin` / pass `admin`
2. Create realm: `balmaya`
3. Create realm roles: `user`, `admin`, `assistant`
4. Create a client for your frontend(s) as needed.
5. For API testing: obtain an access token from Keycloak and call endpoints with:
   `Authorization: Bearer <token>`

## API
- `GET/POST /beneficiaries`
- `PUT /beneficiaries/{logicalId}`
- `GET/POST /schedules`
- `PUT /schedules/{logicalId}`
- `GET/POST /payments`
- Admin (read-only): `GET /admin/read/**` (requires role admin or assistant)

## Notes
- Payment collection and payout execution integrations are stubbed (entities exist).
- Schedule runner advances `next_run_at` via version bump to respect immutable versioning.


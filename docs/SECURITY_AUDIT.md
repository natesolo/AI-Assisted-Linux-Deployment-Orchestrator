# Security Audit (MVP)

Date: 2026-02-21

## Findings and actions

1. SSH host key verification disabled by default
- Risk: MITM risk during SSH connection setup.
- Action taken: default is now strict host key verification (`AIDLO_SSH_STRICT_HOST_KEY_CHECKING=true`).

2. Password-based SSH auth accepted without policy guard
- Risk: weaker operational security than key-based auth.
- Action taken: password auth is now disabled by default (`AIDLO_SSH_ALLOW_PASSWORD_AUTH=false`).

3. No API authentication on orchestration endpoints
- Risk: anyone with network access could trigger deployments.
- Action taken: added API key interceptor for `/api/**` and `/actuator/**`.
  - Enabled by default with `X-API-Key` header.
  - Controlled by `aidlo.security.api-key.*`.

4. Hardcoded database credentials in default config
- Risk: secret leakage and insecure defaults.
- Action taken: removed default DB credential literals. Default profile now requires env vars (`DB_URL`, `DB_USER`, `DB_PASSWORD`).

5. Missing `.gitignore`
- Risk: accidental commit of build artifacts and local env files.
- Action taken: added `.gitignore` and `.env.example`.

## Remaining risk (known)
- Host passwords are still stored in the database field when password auth is enabled. For production, prefer key-based auth and keep password auth disabled.

## Recommended next hardening steps
- Encrypt host password field at rest with application-managed key material (KMS-backed in production).
- Add role-based authorization (not only API key authentication).
- Add rate limiting and request audit signatures for sensitive endpoints.

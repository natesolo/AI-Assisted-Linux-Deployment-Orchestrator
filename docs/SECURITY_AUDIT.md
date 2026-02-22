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

6. No rate limiting on deployment submissions
- Risk: endpoint flooding can overwhelm SSH targets and orchestrator resources.
- Action taken: added rate limiting interceptor on `POST /api/v1/deployments` (defaults: 10 requests / 60 seconds per API key or client IP).
  - Operational note: limiter is in-memory per-process. For horizontal scale, move limits to shared storage or an API gateway.

7. Narrow command blocklist
- Risk: blocklists miss destructive variants and allow bypasses.
- Action taken: replaced blocklist behavior with strict allowlist validation for generated deployment commands.

8. Docker Compose credentials and network exposure defaults
- Risk: hardcoded local DB password and broad port binding can leak into non-local usage.
- Action taken:
  - `docker-compose.yml` now reads credentials from env vars (`POSTGRES_*`).
  - PostgreSQL port binding is loopback-only (`127.0.0.1:5432:5432`).

9. SSH output could contain secrets
- Risk: command output persisted to DB could include credentials/tokens.
- Action taken: added output redaction before run-step output is stored.

10. Unvalidated SSH key path
- Risk: user-supplied key path may point outside expected key directories.
- Action taken: added SSH key path allowlist policy (`aidlo.ssh.key-path-policy.*`) and validation at host registration and execution time.

11. Free-text actor fields
- Risk: unbounded/unsanitized actor identifiers can pollute logs and DB.
- Action taken: `requestedBy`/`approvedBy` now enforce bounded safe pattern validation and trimming.

12. Actuator health information disclosure
- Risk: verbose health responses can expose internal component details.
- Action taken: configured non-local profiles to hide health details/components (`management.endpoint.health.show-details=never`).

## Remaining risk (known)
- Host passwords are still stored in the database field when password auth is enabled. For production, prefer key-based auth and keep password auth disabled.
- Prompt injection remains a threat for any future real LLM integration. Keep strict allowlist validation in place and treat LLM output as untrusted input.
- Log redaction is heuristic and pattern-based. Treat it as defense-in-depth; avoid scripts that print secrets.

## Recommended next hardening steps
- Encrypt host password field at rest with application-managed key material (KMS-backed in production).
- Add role-based authorization (not only API key authentication).
- Add signed request audit trails and privileged approval workflows for high-risk plans.

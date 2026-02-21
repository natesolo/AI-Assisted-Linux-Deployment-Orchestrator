# Architecture Specification

## End-to-end flow
1. `POST /api/v1/deployments` receives natural-language request.
2. `LlmPlanner` converts request to structured `DeploymentPlan` (with optional rollback command per step).
3. `ApprovalService` verifies non-dry-run approval exists.
4. `CommandPolicyService` validates each generated command.
5. `DeploymentOrchestratorService` executes each step with retry + timeout policy.
6. On host-level failure, orchestrator executes rollback commands in reverse order for completed steps.
7. `SshjSshExecutor` runs commands on remote Linux hosts over SSH.
8. Step outputs/statuses and audit events are persisted in PostgreSQL.
9. Prometheus metrics are exposed on `/actuator/prometheus`.

## Package design
- `/Users/nathan/AI-Assisted Linux Deployment Orchestrator (Java)/src/main/java/com/nathan/aidlo/cmdb`
  - CMDB entities and repositories
  - Host registration API with SSH connection/auth details
- `/Users/nathan/AI-Assisted Linux Deployment Orchestrator (Java)/src/main/java/com/nathan/aidlo/orchestration`
  - Deployment submission API
  - Orchestration state machine
  - Execution policy properties
- `/Users/nathan/AI-Assisted Linux Deployment Orchestrator (Java)/src/main/java/com/nathan/aidlo/llm`
  - Planner abstraction + provider implementations
- `/Users/nathan/AI-Assisted Linux Deployment Orchestrator (Java)/src/main/java/com/nathan/aidlo/ssh`
  - Remote command execution abstraction and SSHJ implementation
- `/Users/nathan/AI-Assisted Linux Deployment Orchestrator (Java)/src/main/java/com/nathan/aidlo/policy`
  - Approval and command safety checks
- `/Users/nathan/AI-Assisted Linux Deployment Orchestrator (Java)/src/main/java/com/nathan/aidlo/audit`
  - Append-only audit events

## Database schema intent
- `hosts`: source-of-truth host metadata and SSH auth configuration
- `deployment_runs`: each deployment request lifecycle
- `run_steps`: step-by-step execution per host (including rollback steps)
- `run_artifacts`: script/log/artifact checksums and locations
- `drift_records`: desired-vs-actual divergence snapshots
- `audit_events`: immutable event history for governance

## Safety model
- Dry-run supported on every run.
- Non-dry-run requires `approvedBy`.
- Policy blocks known dangerous shell patterns.
- On failure, rollback strategy executes declared rollback commands in reverse order.
- All run and step actions generate audit events.

## Metrics
- `aidlo.run.completed.total{status=*}`
- `aidlo.run.duration`
- `aidlo.step.duration{step=*,host=*}`
- `aidlo.step.failed.total`
- `aidlo.rollback.executed.total`
- `aidlo.rollback.failed.total`

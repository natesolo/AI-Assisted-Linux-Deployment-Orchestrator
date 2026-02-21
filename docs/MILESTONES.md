# Delivery Milestones

## Milestone 1 (Week 1): Production-shaped MVP (Completed in this scaffold)
- Spring Boot API + persistence + Flyway schema
- Host registration and deployment submission APIs
- Planner abstraction and baseline plan generation
- Dry-run execution flow, run/step status tracking, audit logging
- Safety controls: approval requirement + command block policy

Definition of done:
- User can register hosts, submit dry-run and approved runs, and inspect step-by-step run output.

## Milestone 2 (Week 2): Real remote execution + reliability
- Implement real SSH adapter with key auth and connection pooling
- Add step retry policies, timeout controls, and rollback hooks
- Capture script artifacts with SHA256 checksums
- Add Prometheus deployment metrics and run latency histograms

Definition of done:
- Run executes on actual remote hosts with retry/timeout handling and measurable SLO data.

## Milestone 3 (Week 3): LLM integration + drift and governance
- Replace heuristic planner with LLM provider abstraction (OpenAI/other)
- Add structured planner output validation (JSON schema)
- Implement drift detection job comparing desired vs actual host state
- Add approval workflow endpoint and immutable event export for audits

Definition of done:
- End-to-end AI-assisted execution is controlled, auditable, and drift-aware.

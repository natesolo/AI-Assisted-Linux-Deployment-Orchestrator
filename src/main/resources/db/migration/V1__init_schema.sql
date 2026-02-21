CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE hosts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hostname VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    ssh_user VARCHAR(255) NOT NULL,
    os_family VARCHAR(100) NOT NULL,
    environment VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE deployment_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_text TEXT NOT NULL,
    desired_state_json TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    dry_run BOOLEAN NOT NULL,
    requested_by VARCHAR(255) NOT NULL,
    approved_by VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);

CREATE TABLE run_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL REFERENCES deployment_runs(id),
    host_id UUID NOT NULL REFERENCES hosts(id),
    step_order INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    command_text TEXT NOT NULL,
    idempotency_key VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    output TEXT,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);

CREATE TABLE run_artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL REFERENCES deployment_runs(id),
    step_id UUID REFERENCES run_steps(id),
    type VARCHAR(100) NOT NULL,
    uri TEXT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE drift_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    host_id UUID NOT NULL REFERENCES hosts(id),
    severity VARCHAR(50) NOT NULL,
    expected_state TEXT NOT NULL,
    actual_state TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID REFERENCES deployment_runs(id),
    actor VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_run_steps_run_id ON run_steps(run_id);
CREATE INDEX idx_run_steps_host_id ON run_steps(host_id);
CREATE INDEX idx_audit_events_run_id ON audit_events(run_id);
CREATE INDEX idx_drift_records_host_id ON drift_records(host_id);

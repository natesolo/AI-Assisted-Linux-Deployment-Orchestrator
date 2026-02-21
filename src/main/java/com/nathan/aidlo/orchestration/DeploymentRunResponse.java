package com.nathan.aidlo.orchestration;

import com.nathan.aidlo.cmdb.RunStatus;

import java.util.UUID;

public record DeploymentRunResponse(UUID runId, RunStatus status, int totalSteps, int failedSteps) {
}

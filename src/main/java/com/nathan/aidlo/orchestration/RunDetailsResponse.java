package com.nathan.aidlo.orchestration;

import com.nathan.aidlo.cmdb.RunStatus;

import java.util.List;
import java.util.UUID;

public record RunDetailsResponse(UUID runId, RunStatus status, boolean dryRun, String requestText, List<RunStepView> steps) {
}

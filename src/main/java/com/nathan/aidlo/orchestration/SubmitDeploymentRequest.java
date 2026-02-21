package com.nathan.aidlo.orchestration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record SubmitDeploymentRequest(
        @NotBlank String requestText,
        @NotEmpty List<UUID> hostIds,
        boolean dryRun,
        @NotBlank String requestedBy,
        String approvedBy
) {
}

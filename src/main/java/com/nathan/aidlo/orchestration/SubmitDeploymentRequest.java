package com.nathan.aidlo.orchestration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SubmitDeploymentRequest(
        @NotBlank
        @Size(max = 200)
        @Pattern(regexp = "^[a-zA-Z0-9\\s+,_\\-./]+$", message = "requestText contains unsupported characters")
        String requestText,
        @NotEmpty List<UUID> hostIds,
        boolean dryRun,
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._@-]+$", message = "requestedBy contains unsupported characters")
        String requestedBy,
        @Size(max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._@-]+$", message = "approvedBy contains unsupported characters")
        String approvedBy
) {
}

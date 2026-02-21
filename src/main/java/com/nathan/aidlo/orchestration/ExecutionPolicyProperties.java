package com.nathan.aidlo.orchestration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aidlo.execution")
public record ExecutionPolicyProperties(
        int maxRetries,
        int rollbackRetries,
        long retryBackoffMillis,
        long commandTimeoutSeconds
) {
}

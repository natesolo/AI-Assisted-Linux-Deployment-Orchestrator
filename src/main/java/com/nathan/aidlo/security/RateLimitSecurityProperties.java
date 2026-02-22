package com.nathan.aidlo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aidlo.security.rate-limit")
public record RateLimitSecurityProperties(boolean enabled, int maxRequests, int windowSeconds) {
}

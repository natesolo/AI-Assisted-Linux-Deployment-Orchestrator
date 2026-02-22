package com.nathan.aidlo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aidlo.security.api-key")
public record ApiKeySecurityProperties(boolean enabled, String headerName, String value) {
}

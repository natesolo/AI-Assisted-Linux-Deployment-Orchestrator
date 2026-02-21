package com.nathan.aidlo.ssh;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aidlo.ssh")
public record SshRuntimeProperties(boolean strictHostKeyChecking) {
}

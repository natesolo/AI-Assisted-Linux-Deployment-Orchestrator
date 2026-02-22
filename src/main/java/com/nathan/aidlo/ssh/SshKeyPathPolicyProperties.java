package com.nathan.aidlo.ssh;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "aidlo.ssh.key-path-policy")
public record SshKeyPathPolicyProperties(boolean enabled, List<String> allowedRoots) {
}

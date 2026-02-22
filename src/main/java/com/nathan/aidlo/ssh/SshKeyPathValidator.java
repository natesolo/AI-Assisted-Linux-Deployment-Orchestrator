package com.nathan.aidlo.ssh;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class SshKeyPathValidator {

    private final SshKeyPathPolicyProperties policyProperties;

    public SshKeyPathValidator(SshKeyPathPolicyProperties policyProperties) {
        this.policyProperties = policyProperties;
    }

    public void validate(String sshKeyPath) {
        if (sshKeyPath == null || sshKeyPath.isBlank() || !policyProperties.enabled()) {
            return;
        }

        String expanded = sshKeyPath.startsWith("~")
                ? sshKeyPath.replaceFirst("^~", System.getProperty("user.home"))
                : sshKeyPath;

        Path normalized = Paths.get(expanded).normalize().toAbsolutePath();
        if (!normalized.isAbsolute()) {
            throw new IllegalArgumentException("sshKeyPath must be an absolute path");
        }

        List<Path> allowedRoots = resolveAllowedRoots();
        boolean allowed = allowedRoots.stream().anyMatch(normalized::startsWith);
        if (!allowed) {
            throw new IllegalArgumentException("sshKeyPath must be under an allowed root");
        }
    }

    private List<Path> resolveAllowedRoots() {
        List<Path> roots = new ArrayList<>();
        List<String> configuredRoots = policyProperties.allowedRoots();
        if (configuredRoots == null) {
            return roots;
        }
        for (String rawRoot : configuredRoots) {
            if (rawRoot == null || rawRoot.isBlank()) {
                continue;
            }
            String expanded = rawRoot.startsWith("~")
                    ? rawRoot.replaceFirst("^~", System.getProperty("user.home"))
                    : rawRoot;
            roots.add(Paths.get(expanded).normalize().toAbsolutePath());
        }
        return roots;
    }
}

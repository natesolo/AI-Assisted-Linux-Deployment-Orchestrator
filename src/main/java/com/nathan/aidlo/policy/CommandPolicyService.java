package com.nathan.aidlo.policy;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class CommandPolicyService {

    private static final List<Pattern> ALLOWED_COMMANDS = List.of(
            Pattern.compile("^sudo dnf -y update \\|\\| sudo apt-get update -y$"),
            Pattern.compile("^sudo dnf -y install docker \\|\\| sudo apt-get install -y docker\\.io$"),
            Pattern.compile("^sudo systemctl enable --now docker$"),
            Pattern.compile("^sudo dnf -y remove docker \\|\\| sudo apt-get remove -y docker\\.io$"),
            Pattern.compile("^sudo systemctl disable --now docker$"),
            Pattern.compile("^sudo dnf -y install node_exporter \\|\\| sudo apt-get install -y prometheus-node-exporter$"),
            Pattern.compile("^sudo systemctl enable --now node_exporter \\|\\| sudo systemctl enable --now prometheus-node-exporter$"),
            Pattern.compile("^sudo dnf -y remove node_exporter \\|\\| sudo apt-get remove -y prometheus-node-exporter$"),
            Pattern.compile("^sudo systemctl disable --now node_exporter \\|\\| sudo systemctl disable --now prometheus-node-exporter$"),
            Pattern.compile("^cat /etc/os-release \\| grep -Ei 'rhel\\|centos\\|rocky\\|almalinux'$"),
            Pattern.compile("^sudo sysctl -w net\\.ipv4\\.ip_forward=1$"),
            Pattern.compile("^sudo sysctl -w net\\.ipv4\\.ip_forward=0$")
    );

    public void validate(String commandText) {
        String normalized = commandText == null ? "" : commandText.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Command blocked by policy: empty command");
        }

        boolean match = ALLOWED_COMMANDS.stream().anyMatch(pattern -> pattern.matcher(normalized).matches());
        if (!match) {
            throw new IllegalArgumentException("Command blocked by allowlist policy: " + normalized);
        }
    }
}

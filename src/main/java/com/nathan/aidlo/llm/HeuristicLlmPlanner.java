package com.nathan.aidlo.llm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HeuristicLlmPlanner implements LlmPlanner {

    @Override
    public DeploymentPlan generatePlan(String requestText) {
        validateRequestText(requestText);
        String lowered = requestText.toLowerCase();
        List<PlanStep> steps = new ArrayList<>();
        int index = 1;

        steps.add(new PlanStep(index++, "Update package index", "sudo dnf -y update || sudo apt-get update -y", null, "pkg-update"));

        if (lowered.contains("docker")) {
            steps.add(new PlanStep(index++, "Install Docker", "sudo dnf -y install docker || sudo apt-get install -y docker.io", "sudo dnf -y remove docker || sudo apt-get remove -y docker.io", "docker-install"));
            steps.add(new PlanStep(index++, "Enable Docker", "sudo systemctl enable --now docker", "sudo systemctl disable --now docker", "docker-enable"));
        }

        if (lowered.contains("monitoring") || lowered.contains("prometheus")) {
            steps.add(new PlanStep(index++, "Install Node Exporter", "sudo dnf -y install node_exporter || sudo apt-get install -y prometheus-node-exporter", "sudo dnf -y remove node_exporter || sudo apt-get remove -y prometheus-node-exporter", "monitoring-node-exporter"));
            steps.add(new PlanStep(index++, "Enable Node Exporter", "sudo systemctl enable --now node_exporter || sudo systemctl enable --now prometheus-node-exporter", "sudo systemctl disable --now node_exporter || sudo systemctl disable --now prometheus-node-exporter", "monitoring-enable"));
        }

        if (lowered.contains("rhel")) {
            steps.add(new PlanStep(index++, "Validate RHEL family", "cat /etc/os-release | grep -Ei 'rhel|centos|rocky|almalinux'", null, "validate-rhel"));
        }

        if (steps.size() == 1) {
            steps.add(new PlanStep(index, "Run baseline hardening", "sudo sysctl -w net.ipv4.ip_forward=1", "sudo sysctl -w net.ipv4.ip_forward=0", "baseline-hardening"));
        }

        String desiredStateJson = "{\"request\":\"" + requestText.replace("\"", "'") + "\",\"stepCount\":" + steps.size() + "}";
        return new DeploymentPlan(desiredStateJson, steps);
    }

    private void validateRequestText(String requestText) {
        if (requestText == null || requestText.isBlank()) {
            throw new IllegalArgumentException("requestText is required");
        }

        String lowered = requestText.toLowerCase();
        String[] forbiddenTokens = {";", "&&", "||", "|", "`", "$(", ">", "<"};
        for (String token : forbiddenTokens) {
            if (lowered.contains(token)) {
                throw new IllegalArgumentException("requestText contains forbidden token: " + token);
            }
        }
    }
}

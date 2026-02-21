package com.nathan.aidlo.llm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HeuristicLlmPlannerTest {

    @Test
    void shouldGenerateDockerAndMonitoringSteps() {
        HeuristicLlmPlanner planner = new HeuristicLlmPlanner();
        DeploymentPlan plan = planner.generatePlan("Install RHEL + Docker + monitoring");

        assertTrue(plan.steps().stream().anyMatch(step -> step.name().contains("Docker")));
        assertTrue(plan.steps().stream().anyMatch(step -> step.name().contains("Node Exporter")));
    }
}

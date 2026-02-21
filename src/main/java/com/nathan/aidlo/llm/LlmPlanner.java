package com.nathan.aidlo.llm;

public interface LlmPlanner {
    DeploymentPlan generatePlan(String requestText);
}

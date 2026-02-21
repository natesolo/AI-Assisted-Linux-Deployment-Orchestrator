package com.nathan.aidlo.llm;

import java.util.List;

public record DeploymentPlan(String desiredStateJson, List<PlanStep> steps) {
}

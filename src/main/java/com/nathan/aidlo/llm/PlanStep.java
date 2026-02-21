package com.nathan.aidlo.llm;

public record PlanStep(int order, String name, String command, String rollbackCommand, String idempotencyKey) {
}

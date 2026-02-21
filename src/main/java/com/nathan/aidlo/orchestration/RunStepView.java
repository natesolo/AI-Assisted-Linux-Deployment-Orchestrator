package com.nathan.aidlo.orchestration;

import com.nathan.aidlo.cmdb.StepStatus;

public record RunStepView(String host, int order, String name, String command, StepStatus status, String output) {
}

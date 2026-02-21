package com.nathan.aidlo.policy;

import org.springframework.stereotype.Service;

@Service
public class ApprovalService {

    public void verifyExecutionAllowed(boolean dryRun, String approvedBy) {
        if (!dryRun && (approvedBy == null || approvedBy.isBlank())) {
            throw new IllegalStateException("Non-dry-run execution requires approvedBy");
        }
    }
}

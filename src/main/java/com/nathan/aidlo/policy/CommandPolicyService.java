package com.nathan.aidlo.policy;

import org.springframework.stereotype.Service;

@Service
public class CommandPolicyService {

    public void validate(String commandText) {
        String lowered = commandText.toLowerCase();
        if (lowered.contains(" rm -rf /") || lowered.startsWith("rm -rf /") || lowered.contains(":(){") || lowered.contains("mkfs")) {
            throw new IllegalArgumentException("Command blocked by policy: " + commandText);
        }
    }
}

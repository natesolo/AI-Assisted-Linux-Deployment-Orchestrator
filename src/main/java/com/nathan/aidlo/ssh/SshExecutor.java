package com.nathan.aidlo.ssh;

import com.nathan.aidlo.cmdb.Host;

import java.time.Duration;

public interface SshExecutor {
    ExecutionResult execute(Host host, String commandText, boolean dryRun, Duration timeout);
}

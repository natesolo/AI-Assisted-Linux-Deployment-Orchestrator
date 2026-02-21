package com.nathan.aidlo.cmdb;

import java.util.UUID;

public record HostView(
        UUID id,
        String hostname,
        String address,
        String sshUser,
        int sshPort,
        String osFamily,
        String environment,
        String sshKeyPath,
        boolean passwordConfigured
) {
}

package com.nathan.aidlo.cmdb;

import jakarta.validation.constraints.NotBlank;

public record CreateHostRequest(
        @NotBlank String hostname,
        @NotBlank String address,
        @NotBlank String sshUser,
        Integer sshPort,
        @NotBlank String osFamily,
        @NotBlank String environment,
        String sshKeyPath,
        String sshPassword
) {
}

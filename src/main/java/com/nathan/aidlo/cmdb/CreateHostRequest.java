package com.nathan.aidlo.cmdb;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateHostRequest(
        @NotBlank
        @Size(max = 120)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "hostname contains unsupported characters")
        String hostname,
        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[a-zA-Z0-9:._-]+$", message = "address contains unsupported characters")
        String address,
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "sshUser contains unsupported characters")
        String sshUser,
        Integer sshPort,
        @NotBlank
        @Size(max = 40)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "osFamily contains unsupported characters")
        String osFamily,
        @NotBlank
        @Size(max = 40)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "environment contains unsupported characters")
        String environment,
        @Size(max = 512)
        @Pattern(regexp = "^[^\\u0000]*$", message = "sshKeyPath contains unsupported characters")
        String sshKeyPath,
        @Size(max = 256)
        String sshPassword
) {
}

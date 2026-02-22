package com.nathan.aidlo.ssh;

import com.nathan.aidlo.cmdb.Host;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class SshjSshExecutor implements SshExecutor {

    private final SshRuntimeProperties sshRuntimeProperties;
    private final SshKeyPathValidator sshKeyPathValidator;

    public SshjSshExecutor(SshRuntimeProperties sshRuntimeProperties, SshKeyPathValidator sshKeyPathValidator) {
        this.sshRuntimeProperties = sshRuntimeProperties;
        this.sshKeyPathValidator = sshKeyPathValidator;
    }

    @Override
    public ExecutionResult execute(Host host, String commandText, boolean dryRun, Duration timeout) {
        if (dryRun) {
            return new ExecutionResult(true, "DRY_RUN " + host.getHostname() + " :: " + commandText);
        }

        try (SSHClient client = new SSHClient()) {
            if (!sshRuntimeProperties.strictHostKeyChecking()) {
                client.addHostKeyVerifier(new PromiscuousVerifier());
            }

            client.connect(host.getAddress(), host.getSshPort());
            authenticate(client, host);

            try (Session session = client.startSession()) {
                Session.Command command = session.exec(commandText);
                command.join(timeout.toSeconds(), TimeUnit.SECONDS);

                String stdout = new String(command.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String stderr = new String(command.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                Integer exitCode = command.getExitStatus();
                if (exitCode == null) {
                    command.close();
                    return new ExecutionResult(false, "Command timed out after " + timeout.toSeconds() + "s");
                }
                boolean success = exitCode == 0;

                return new ExecutionResult(success, "exit=" + (exitCode == null ? -1 : exitCode) + " stdout=" + stdout + " stderr=" + stderr);
            }
        } catch (Exception ex) {
            return new ExecutionResult(false, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private void authenticate(SSHClient client, Host host) throws Exception {
        if (host.getSshPassword() != null && !host.getSshPassword().isBlank()) {
            if (!sshRuntimeProperties.allowPasswordAuth()) {
                throw new IllegalArgumentException("Password authentication is disabled by policy. Configure SSH keys or set aidlo.ssh.allow-password-auth=true.");
            }
            client.authPassword(host.getSshUser(), host.getSshPassword());
            return;
        }

        if (host.getSshKeyPath() != null && !host.getSshKeyPath().isBlank()) {
            sshKeyPathValidator.validate(host.getSshKeyPath());
            client.authPublickey(host.getSshUser(), host.getSshKeyPath());
            return;
        }

        throw new IllegalArgumentException("Host must provide sshPassword or sshKeyPath for authentication");
    }
}

package com.nathan.aidlo.cmdb;

import jakarta.validation.Valid;
import com.nathan.aidlo.ssh.SshKeyPathValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hosts")
public class HostController {

    private final HostRepository hostRepository;
    private final SshKeyPathValidator sshKeyPathValidator;

    public HostController(HostRepository hostRepository, SshKeyPathValidator sshKeyPathValidator) {
        this.hostRepository = hostRepository;
        this.sshKeyPathValidator = sshKeyPathValidator;
    }

    @PostMapping
    public HostView create(@Valid @RequestBody CreateHostRequest request) {
        if ((request.sshKeyPath() == null || request.sshKeyPath().isBlank())
                && (request.sshPassword() == null || request.sshPassword().isBlank())) {
            throw new IllegalArgumentException("Either sshKeyPath or sshPassword must be provided");
        }
        sshKeyPathValidator.validate(request.sshKeyPath());

        Host host = new Host();
        host.setHostname(request.hostname());
        host.setAddress(request.address());
        host.setSshUser(request.sshUser());
        host.setSshPort(request.sshPort() == null ? 22 : request.sshPort());
        host.setOsFamily(request.osFamily());
        host.setEnvironment(request.environment());
        host.setSshKeyPath(request.sshKeyPath());
        host.setSshPassword(request.sshPassword());

        Host saved = hostRepository.save(host);
        return toView(saved);
    }

    @GetMapping
    public List<HostView> list() {
        return hostRepository.findAll().stream().map(this::toView).toList();
    }

    private HostView toView(Host host) {
        return new HostView(
                host.getId(),
                host.getHostname(),
                host.getAddress(),
                host.getSshUser(),
                host.getSshPort(),
                host.getOsFamily(),
                host.getEnvironment(),
                host.getSshKeyPath(),
                host.getSshPassword() != null && !host.getSshPassword().isBlank()
        );
    }
}

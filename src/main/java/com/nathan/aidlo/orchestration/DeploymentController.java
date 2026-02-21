package com.nathan.aidlo.orchestration;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deployments")
public class DeploymentController {

    private final DeploymentOrchestratorService orchestratorService;

    public DeploymentController(DeploymentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping
    public DeploymentRunResponse submit(@Valid @RequestBody SubmitDeploymentRequest request) {
        return orchestratorService.submit(request);
    }

    @GetMapping("/{runId}")
    public RunDetailsResponse details(@PathVariable UUID runId) {
        return orchestratorService.runDetails(runId);
    }
}

package com.nathan.aidlo.orchestration;

import com.nathan.aidlo.audit.AuditEvent;
import com.nathan.aidlo.audit.AuditEventRepository;
import com.nathan.aidlo.cmdb.DeploymentRun;
import com.nathan.aidlo.cmdb.DeploymentRunRepository;
import com.nathan.aidlo.cmdb.Host;
import com.nathan.aidlo.cmdb.HostRepository;
import com.nathan.aidlo.cmdb.RunStatus;
import com.nathan.aidlo.cmdb.RunStep;
import com.nathan.aidlo.cmdb.RunStepRepository;
import com.nathan.aidlo.cmdb.StepStatus;
import com.nathan.aidlo.llm.DeploymentPlan;
import com.nathan.aidlo.llm.LlmPlanner;
import com.nathan.aidlo.llm.PlanStep;
import com.nathan.aidlo.policy.ApprovalService;
import com.nathan.aidlo.policy.CommandPolicyService;
import com.nathan.aidlo.security.OutputRedactionService;
import com.nathan.aidlo.ssh.ExecutionResult;
import com.nathan.aidlo.ssh.SshExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DeploymentOrchestratorService {

    private final HostRepository hostRepository;
    private final DeploymentRunRepository runRepository;
    private final RunStepRepository runStepRepository;
    private final AuditEventRepository auditEventRepository;
    private final LlmPlanner llmPlanner;
    private final CommandPolicyService commandPolicyService;
    private final ApprovalService approvalService;
    private final SshExecutor sshExecutor;
    private final ExecutionPolicyProperties executionPolicy;
    private final MeterRegistry meterRegistry;
    private final OutputRedactionService outputRedactionService;

    public DeploymentOrchestratorService(
            HostRepository hostRepository,
            DeploymentRunRepository runRepository,
            RunStepRepository runStepRepository,
            AuditEventRepository auditEventRepository,
            LlmPlanner llmPlanner,
            CommandPolicyService commandPolicyService,
            ApprovalService approvalService,
            SshExecutor sshExecutor,
            ExecutionPolicyProperties executionPolicy,
            MeterRegistry meterRegistry,
            OutputRedactionService outputRedactionService
    ) {
        this.hostRepository = hostRepository;
        this.runRepository = runRepository;
        this.runStepRepository = runStepRepository;
        this.auditEventRepository = auditEventRepository;
        this.llmPlanner = llmPlanner;
        this.commandPolicyService = commandPolicyService;
        this.approvalService = approvalService;
        this.sshExecutor = sshExecutor;
        this.executionPolicy = executionPolicy;
        this.meterRegistry = meterRegistry;
        this.outputRedactionService = outputRedactionService;
    }

    @Transactional
    public DeploymentRunResponse submit(SubmitDeploymentRequest request) {
        Timer.Sample runSample = Timer.start(meterRegistry);

        approvalService.verifyExecutionAllowed(request.dryRun(), request.approvedBy());

        List<Host> hosts = hostRepository.findAllById(request.hostIds());
        if (hosts.size() != request.hostIds().size()) {
            throw new IllegalArgumentException("One or more hostIds were not found");
        }

        DeploymentPlan plan = llmPlanner.generatePlan(request.requestText());

        DeploymentRun run = new DeploymentRun();
        run.setRequestText(request.requestText());
        run.setDesiredStateJson(plan.desiredStateJson());
        run.setStatus(RunStatus.PLANNED);
        run.setDryRun(request.dryRun());
        String requestedBy = request.requestedBy().trim();
        String approvedBy = request.approvedBy() == null ? null : request.approvedBy().trim();
        run.setRequestedBy(requestedBy);
        run.setApprovedBy(approvedBy);
        runRepository.save(run);

        writeAudit(run, requestedBy, "RUN_CREATED", plan.desiredStateJson());

        run.setStatus(RunStatus.RUNNING);
        run.setStartedAt(Instant.now());

        int failedSteps = 0;
        int totalSteps = 0;

        for (Host host : hosts) {
            List<PlanStep> completedHostSteps = new ArrayList<>();
            boolean hostFailed = false;

            for (PlanStep planStep : plan.steps()) {
                totalSteps++;
                commandPolicyService.validate(planStep.command());

                RunStep step = new RunStep();
                step.setRun(run);
                step.setHost(host);
                step.setStepOrder(planStep.order());
                step.setName(planStep.name());
                step.setCommandText(planStep.command());
                step.setIdempotencyKey(planStep.idempotencyKey());
                step.setStatus(StepStatus.RUNNING);
                step.setStartedAt(Instant.now());
                runStepRepository.save(step);

                Timer.Sample stepSample = Timer.start(meterRegistry);
                AttemptResult result = executeWithRetry(host, planStep.command(), request.dryRun(), executionPolicy.maxRetries());
                stepSample.stop(Timer.builder("aidlo.step.duration")
                        .tag("step", planStep.name())
                        .tag("host", host.getHostname())
                        .register(meterRegistry));

                step.setOutput(outputRedactionService.redact(result.output()));
                step.setCompletedAt(Instant.now());
                if (result.success()) {
                    step.setStatus(request.dryRun() ? StepStatus.SKIPPED : StepStatus.COMPLETED);
                    completedHostSteps.add(planStep);
                } else {
                    step.setStatus(StepStatus.FAILED);
                    failedSteps++;
                    hostFailed = true;
                    meterRegistry.counter("aidlo.step.failed.total").increment();
                }
                runStepRepository.save(step);

                writeAudit(run, requestedBy, "STEP_EXECUTED",
                        "{\"host\":\"" + host.getHostname() + "\",\"step\":\"" + planStep.name() + "\",\"status\":\""
                                + step.getStatus() + "\",\"attempts\":" + result.attempts() + "}");

                if (hostFailed) {
                    if (!request.dryRun()) {
                        performRollback(run, host, completedHostSteps, requestedBy);
                    }
                    break;
                }
            }
        }

        run.setCompletedAt(Instant.now());
        run.setStatus(failedSteps > 0 ? RunStatus.FAILED : RunStatus.COMPLETED);
        runRepository.save(run);

        if (run.getStatus() == RunStatus.COMPLETED) {
            meterRegistry.counter("aidlo.run.completed.total", "status", "success").increment();
        } else {
            meterRegistry.counter("aidlo.run.completed.total", "status", "failed").increment();
        }
        runSample.stop(Timer.builder("aidlo.run.duration").register(meterRegistry));

        writeAudit(run, requestedBy, "RUN_COMPLETED",
                "{\"status\":\"" + run.getStatus() + "\",\"failedSteps\":" + failedSteps + "}");

        return new DeploymentRunResponse(run.getId(), run.getStatus(), totalSteps, failedSteps);
    }

    @Transactional(readOnly = true)
    public RunDetailsResponse runDetails(UUID runId) {
        DeploymentRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        List<RunStepView> views = new ArrayList<>();
        for (RunStep step : runStepRepository.findByRunIdOrderByStepOrderAsc(runId)) {
            views.add(new RunStepView(
                    step.getHost().getHostname(),
                    step.getStepOrder(),
                    step.getName(),
                    step.getCommandText(),
                    step.getStatus(),
                    step.getOutput()
            ));
        }

        return new RunDetailsResponse(run.getId(), run.getStatus(), run.isDryRun(), run.getRequestText(), views);
    }

    private AttemptResult executeWithRetry(Host host, String commandText, boolean dryRun, int maxRetries) {
        StringBuilder output = new StringBuilder();
        int attempts = Math.max(1, maxRetries + 1);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            ExecutionResult result = sshExecutor.execute(host, commandText, dryRun, Duration.ofSeconds(executionPolicy.commandTimeoutSeconds()));
            output.append("attempt=").append(attempt).append(" result=").append(outputRedactionService.redact(result.output()));
            if (attempt < attempts) {
                output.append("\n");
            }
            if (result.success()) {
                return new AttemptResult(true, output.toString(), attempt);
            }

            if (attempt < attempts && executionPolicy.retryBackoffMillis() > 0) {
                try {
                    Thread.sleep(executionPolicy.retryBackoffMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new AttemptResult(false, output + "\ninterrupted", attempt);
                }
            }
        }

        return new AttemptResult(false, output.toString(), attempts);
    }

    private void performRollback(DeploymentRun run, Host host, List<PlanStep> completedSteps, String actor) {
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            PlanStep completedStep = completedSteps.get(i);
            if (completedStep.rollbackCommand() == null || completedStep.rollbackCommand().isBlank()) {
                continue;
            }

            commandPolicyService.validate(completedStep.rollbackCommand());

            RunStep rollbackStep = new RunStep();
            rollbackStep.setRun(run);
            rollbackStep.setHost(host);
            rollbackStep.setStepOrder(10000 + completedStep.order());
            rollbackStep.setName("ROLLBACK: " + completedStep.name());
            rollbackStep.setCommandText(completedStep.rollbackCommand());
            rollbackStep.setIdempotencyKey("rollback-" + completedStep.idempotencyKey());
            rollbackStep.setStatus(StepStatus.RUNNING);
            rollbackStep.setStartedAt(Instant.now());
            runStepRepository.save(rollbackStep);

            AttemptResult rollbackResult = executeWithRetry(host, completedStep.rollbackCommand(), false, executionPolicy.rollbackRetries());
            rollbackStep.setOutput(outputRedactionService.redact(rollbackResult.output()));
            rollbackStep.setCompletedAt(Instant.now());
            rollbackStep.setStatus(rollbackResult.success() ? StepStatus.COMPLETED : StepStatus.FAILED);
            runStepRepository.save(rollbackStep);

            meterRegistry.counter("aidlo.rollback.executed.total").increment();
            if (!rollbackResult.success()) {
                meterRegistry.counter("aidlo.rollback.failed.total").increment();
            }

            writeAudit(run, actor, "ROLLBACK_EXECUTED",
                    "{\"host\":\"" + host.getHostname() + "\",\"step\":\"" + completedStep.name()
                            + "\",\"status\":\"" + rollbackStep.getStatus() + "\"}");
        }
    }

    private void writeAudit(DeploymentRun run, String actor, String eventType, String payload) {
        AuditEvent event = new AuditEvent();
        event.setRun(run);
        event.setActor(actor);
        event.setEventType(eventType);
        event.setPayload(payload);
        auditEventRepository.save(event);
    }

    private record AttemptResult(boolean success, String output, int attempts) {
    }
}

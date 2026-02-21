package com.nathan.aidlo.cmdb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "run_artifacts")
public class RunArtifact {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id")
    private DeploymentRun run;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private RunStep step;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private String sha256;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public DeploymentRun getRun() {
        return run;
    }

    public void setRun(DeploymentRun run) {
        this.run = run;
    }

    public RunStep getStep() {
        return step;
    }

    public void setStep(RunStep step) {
        this.step = step;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}

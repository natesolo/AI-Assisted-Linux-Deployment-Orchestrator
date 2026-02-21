package com.nathan.aidlo.cmdb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drift_records")
public class DriftRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "host_id")
    private Host host;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriftSeverity severity;

    @Column(nullable = false, columnDefinition = "text")
    private String expectedState;

    @Column(nullable = false, columnDefinition = "text")
    private String actualState;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant detectedAt;

    @PrePersist
    void onCreate() {
        detectedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public DriftSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(DriftSeverity severity) {
        this.severity = severity;
    }

    public String getExpectedState() {
        return expectedState;
    }

    public void setExpectedState(String expectedState) {
        this.expectedState = expectedState;
    }

    public String getActualState() {
        return actualState;
    }

    public void setActualState(String actualState) {
        this.actualState = actualState;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

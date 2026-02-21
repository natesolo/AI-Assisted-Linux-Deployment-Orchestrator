package com.nathan.aidlo.cmdb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RunStepRepository extends JpaRepository<RunStep, UUID> {
    List<RunStep> findByRunIdOrderByStepOrderAsc(UUID runId);
}

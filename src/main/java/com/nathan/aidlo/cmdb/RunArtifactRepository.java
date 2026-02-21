package com.nathan.aidlo.cmdb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RunArtifactRepository extends JpaRepository<RunArtifact, UUID> {
}

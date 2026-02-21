package com.nathan.aidlo.cmdb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HostRepository extends JpaRepository<Host, UUID> {
}

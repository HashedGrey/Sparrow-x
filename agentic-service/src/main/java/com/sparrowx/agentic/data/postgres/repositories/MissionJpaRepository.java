package com.sparrowx.agentic.data.postgres.repositories;

import com.sparrowx.agentic.data.postgres.entities.MissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionJpaRepository
        extends JpaRepository<MissionEntity, String> {

    Optional<MissionEntity> findByTenantIdAndMissionId(
            String tenantId,
            String missionId
    );

    Optional<MissionEntity> findByTenantIdAndRequestId(
            String tenantId,
            String requestId
    );
}
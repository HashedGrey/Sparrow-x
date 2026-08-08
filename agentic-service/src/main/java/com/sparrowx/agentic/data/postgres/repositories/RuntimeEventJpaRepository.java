package com.sparrowx.agentic.data.postgres.repositories;

import com.sparrowx.agentic.data.postgres.entities.RuntimeEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RuntimeEventJpaRepository
        extends JpaRepository<RuntimeEventEntity, Long> {

    Optional<RuntimeEventEntity> findByTenantIdAndMissionIdAndResumeToken(
            String tenantId,
            String missionId,
            String resumeToken
    );

    List<RuntimeEventEntity>
    findByTenantIdAndMissionIdAndIdGreaterThanOrderByIdAsc(
            String tenantId,
            String missionId,
            long id,
            Pageable pageable
    );

    Optional<RuntimeEventEntity>
    findFirstByTenantIdAndMissionIdOrderByIdDesc(
            String tenantId,
            String missionId
    );
}
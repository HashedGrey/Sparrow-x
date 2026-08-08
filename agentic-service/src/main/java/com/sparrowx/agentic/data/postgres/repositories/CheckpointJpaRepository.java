package com.sparrowx.agentic.data.postgres.repositories;

import com.sparrowx.agentic.data.postgres.entities.CheckpointEntity;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckpointJpaRepository
        extends JpaRepository<CheckpointEntity, String> {

    Optional<CheckpointEntity> findByTenantIdAndMissionIdAndCheckpointId(
            String tenantId,
            String missionId,
            String checkpointId
    );

    Optional<CheckpointEntity>
    findFirstByTenantIdAndMissionIdAndCheckpointTypeOrderByCreatedAtDescCheckpointIdDesc(
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType checkpointType
    );
}
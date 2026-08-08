package com.sparrowx.agentic.runtime.checkpoint;

import java.util.Optional;

/**
 * Tenant-scoped external snapshot persistence port.
 * Temporal history remains the execution-recovery authority.
 */
public interface CheckpointStore {

    CheckpointRef save(CheckpointSnapshot snapshot);

    Optional<CheckpointSnapshot> findById(
            String tenantId,
            String missionId,
            String checkpointId
    );

    Optional<CheckpointSnapshot> findLatest(
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType checkpointType
    );
}
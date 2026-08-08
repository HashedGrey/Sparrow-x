package com.sparrowx.agentic.data.postgres.adapters;

import com.sparrowx.agentic.data.postgres.entities.CheckpointEntity;
import com.sparrowx.agentic.data.postgres.mappers.CheckpointEntityMapper;
import com.sparrowx.agentic.data.postgres.repositories.CheckpointJpaRepository;
import com.sparrowx.agentic.exceptions.CheckpointCorruptionException;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL adapter for explicit large-payload and final-result snapshots.
 *
 * This is not a Workflow recovery store. Temporal history remains the
 * execution authority.
 *
 * Transaction boundaries are owned by the BuildingBlocks CommandBus.
 */
@Component
public final class PostgresCheckpointStore
        implements CheckpointStore {

    private final CheckpointJpaRepository repository;
    private final CheckpointEntityMapper mapper;

    public PostgresCheckpointStore(
            CheckpointJpaRepository repository,
            CheckpointEntityMapper mapper
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null"
        );

        this.mapper = Objects.requireNonNull(
                mapper,
                "mapper must not be null"
        );
    }

    @Override
    public CheckpointRef save(CheckpointSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        CheckpointRef reference = Objects.requireNonNull(
                snapshot.reference(),
                "snapshot.reference must not be null"
        );

        Optional<CheckpointEntity> existing =
                repository.findByTenantIdAndMissionIdAndCheckpointId(
                        reference.tenantId(),
                        reference.missionId(),
                        reference.checkpointId()
                );

        if (existing.isPresent()) {
            return verifyIdempotent(snapshot, existing.get());
        }

        try {
            CheckpointEntity saved = repository.saveAndFlush(
                    mapper.toEntity(snapshot)
            );

            return mapper.toReference(saved);
        } catch (DataIntegrityViolationException exception) {
            CheckpointEntity concurrent =
                    repository.findByTenantIdAndMissionIdAndCheckpointId(
                                    reference.tenantId(),
                                    reference.missionId(),
                                    reference.checkpointId()
                            )
                            .orElseThrow(() ->
                                    new CheckpointCorruptionException(
                                            "Checkpoint write conflicted "
                                                    + "without a persisted row",
                                            exception
                                    )
                            );

            return verifyIdempotent(snapshot, concurrent);
        }
    }
    @Override
    public Optional<CheckpointSnapshot> findById(
            String tenantId,
            String missionId,
            String checkpointId
    ) {
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");
        checkpointId = requireText(
                checkpointId,
                "checkpointId"
        );

        return repository
                .findByTenantIdAndMissionIdAndCheckpointId(
                        tenantId,
                        missionId,
                        checkpointId
                )
                .map(mapper::toDomain);
    }

    private CheckpointRef verifyIdempotent(
            CheckpointSnapshot requested,
            CheckpointEntity existingEntity
    ) {
        CheckpointSnapshot existing = mapper.toDomain(existingEntity);

        if (!existing.equals(requested)) {
            throw new CheckpointCorruptionException(
                    "Checkpoint identifier already exists with "
                            + "different immutable content: "
                            + requested.reference().checkpointId()
            );
        }

        return mapper.toReference(existingEntity);
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value;
    }

    @Override
    public Optional<CheckpointSnapshot> findLatest(
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType checkpointType
    ) {
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");
        Objects.requireNonNull(
                checkpointType,
                "checkpointType must not be null"
        );

        return repository
                .findFirstByTenantIdAndMissionIdAndCheckpointTypeOrderByCreatedAtDescCheckpointIdDesc(
                        tenantId,
                        missionId,
                        checkpointType
                )
                .map(mapper::toDomain);
    }
}
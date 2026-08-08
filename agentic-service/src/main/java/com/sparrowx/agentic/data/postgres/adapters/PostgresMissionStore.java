package com.sparrowx.agentic.data.postgres.adapters;

import com.sparrowx.agentic.data.postgres.entities.MissionEntity;
import com.sparrowx.agentic.data.postgres.mappers.MissionEntityMapper;
import com.sparrowx.agentic.data.postgres.repositories.MissionJpaRepository;
import com.sparrowx.agentic.exceptions.MissionNotFoundException;
import com.sparrowx.agentic.exceptions.MissionValidationException;
import com.sparrowx.agentic.mission.model.Mission;
import com.sparrowx.agentic.mission.store.MissionStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL persistence for the business Mission aggregate and its public
 * lifecycle projection.
 *
 * Temporal execution history is not reconstructed from this table.
 *
 * Transaction boundaries are owned by the BuildingBlocks CommandBus.
 */
@Component
public final class PostgresMissionStore
        implements MissionStore {

    private final MissionJpaRepository repository;
    private final MissionEntityMapper mapper;

    public PostgresMissionStore(
            MissionJpaRepository repository,
            MissionEntityMapper mapper
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
    public Mission createOrGet(Mission mission) {
        Objects.requireNonNull(
                mission,
                "mission must not be null"
        );

        Optional<MissionEntity> existing =
                repository.findByTenantIdAndRequestId(
                        mission.tenantId(),
                        mission.context().requestId()
                );

        if (existing.isPresent()) {
            return verifyIdempotentSubmission(
                    mission,
                    existing.get()
            );
        }

        try {
            MissionEntity saved = repository.saveAndFlush(
                    mapper.toEntity(mission)
            );

            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            MissionEntity concurrent =
                    repository
                            .findByTenantIdAndRequestId(
                                    mission.tenantId(),
                                    mission.context().requestId()
                            )
                            .orElseThrow(() ->
                                    new MissionValidationException(
                                            "Mission submission conflicted "
                                                    + "without a persisted "
                                                    + "mission row",
                                            exception
                                    )
                            );

            return verifyIdempotentSubmission(
                    mission,
                    concurrent
            );
        }
    }

    @Override
    public Mission save(Mission mission) {
        Objects.requireNonNull(
                mission,
                "mission must not be null"
        );

        MissionEntity entity =
                repository
                        .findByTenantIdAndMissionId(
                                mission.tenantId(),
                                mission.missionId()
                        )
                        .orElseThrow(() ->
                                new MissionNotFoundException(
                                        "Mission not found: "
                                                + mission.missionId()
                                )
                        );

        mapper.updateEntity(entity, mission);

        try {
            return mapper.toDomain(
                    repository.saveAndFlush(entity)
            );
        } catch (OptimisticLockingFailureException exception) {
            throw new MissionValidationException(
                    "Mission was concurrently modified: "
                            + mission.missionId(),
                    exception
            );
        }
    }

    @Override
    public Optional<Mission> findById(
            String tenantId,
            String missionId
    ) {
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");

        return repository
                .findByTenantIdAndMissionId(
                        tenantId,
                        missionId
                )
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Mission> findByRequestId(
            String tenantId,
            String requestId
    ) {
        tenantId = requireText(tenantId, "tenantId");
        requestId = requireText(requestId, "requestId");

        return repository
                .findByTenantIdAndRequestId(
                        tenantId,
                        requestId
                )
                .map(mapper::toDomain);
    }

    private Mission verifyIdempotentSubmission(
            Mission requested,
            MissionEntity entity
    ) {
        Mission existing = mapper.toDomain(entity);

        if (!existing.missionId().equals(requested.missionId())
                || !existing.requestFingerprint().equals(
                requested.requestFingerprint()
        )) {
            throw new MissionValidationException(
                    "requestId is already associated with "
                            + "different mission input"
            );
        }

        return existing;
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

        return value.trim();
    }
}
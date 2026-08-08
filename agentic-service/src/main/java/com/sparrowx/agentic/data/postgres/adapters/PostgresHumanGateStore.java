package com.sparrowx.agentic.data.postgres.adapters;

import com.sparrowx.agentic.data.postgres.entities.HumanGateEntity;
import com.sparrowx.agentic.data.postgres.mappers.HumanGateEntityMapper;
import com.sparrowx.agentic.data.postgres.repositories.HumanGateJpaRepository;
import com.sparrowx.agentic.exceptions.HumanGateException;
import com.sparrowx.agentic.runtime.gate.HumanGate;
import com.sparrowx.agentic.runtime.gate.HumanGateStatus;
import com.sparrowx.agentic.runtime.store.HumanGateStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL persistence for auditable human-review gates.
 *
 * Temporal owns durable waiting and continuation. This adapter persists only
 * the business gate record and reviewer decision.
 *
 * Transaction boundaries are owned by the BuildingBlocks CommandBus.
 */
@Component
public final class PostgresHumanGateStore
        implements HumanGateStore {

    private final HumanGateJpaRepository repository;
    private final HumanGateEntityMapper mapper;

    public PostgresHumanGateStore(
            HumanGateJpaRepository repository,
            HumanGateEntityMapper mapper
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
    public HumanGate create(HumanGate gate) {
        Objects.requireNonNull(gate, "gate must not be null");

        Optional<HumanGateEntity> existing =
                repository.findByTenantIdAndMissionIdAndGateId(
                        gate.tenantId(),
                        gate.missionId(),
                        gate.gateId()
                );

        if (existing.isPresent()) {
            return requireIdempotentCreate(
                    gate,
                    existing.get()
            );
        }

        try {
            return mapper.toDomain(
                    repository.saveAndFlush(
                            mapper.toEntity(gate)
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            HumanGateEntity concurrent =
                    repository
                            .findByTenantIdAndMissionIdAndGateId(
                                    gate.tenantId(),
                                    gate.missionId(),
                                    gate.gateId()
                            )
                            .orElseThrow(() ->
                                    new HumanGateException(
                                            "Human gate write conflicted "
                                                    + "without a persisted row",
                                            exception
                                    )
                            );

            return requireIdempotentCreate(
                    gate,
                    concurrent
            );
        }
    }

    @Override
    public HumanGate save(HumanGate gate) {
        Objects.requireNonNull(gate, "gate must not be null");

        HumanGateEntity entity =
                repository
                        .findByTenantIdAndMissionIdAndGateId(
                                gate.tenantId(),
                                gate.missionId(),
                                gate.gateId()
                        )
                        .orElseThrow(() ->
                                new HumanGateException(
                                        "Human gate not found: "
                                                + gate.gateId()
                                )
                        );

        mapper.updateEntity(entity, gate);

        try {
            return mapper.toDomain(
                    repository.saveAndFlush(entity)
            );
        } catch (OptimisticLockingFailureException exception) {
            throw new HumanGateException(
                    "Human gate was concurrently modified: "
                            + gate.gateId(),
                    exception
            );
        }
    }

    @Override
    public Optional<HumanGate> findById(
            String tenantId,
            String missionId,
            String gateId
    ) {
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");
        gateId = requireText(gateId, "gateId");

        return repository
                .findByTenantIdAndMissionIdAndGateId(
                        tenantId,
                        missionId,
                        gateId
                )
                .map(mapper::toDomain);
    }

    @Override
    public Optional<HumanGate> findOpenByMission(
            String tenantId,
            String missionId
    ) {
        tenantId = requireText(tenantId, "tenantId");
        missionId = requireText(missionId, "missionId");

        return repository
                .findFirstByTenantIdAndMissionIdAndStatusOrderByCreatedAtDescGateIdDesc(
                        tenantId,
                        missionId,
                        HumanGateStatus.OPEN
                )
                .map(mapper::toDomain);
    }

    private HumanGate requireIdempotentCreate(
            HumanGate requested,
            HumanGateEntity entity
    ) {
        HumanGate existing = mapper.toDomain(entity);

        if (!existing.equals(requested)) {
            throw new HumanGateException(
                    "Human gate identifier already exists with "
                            + "different immutable content: "
                            + requested.gateId()
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
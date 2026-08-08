package com.sparrowx.agentic.data.postgres.mappers;

import com.sparrowx.agentic.data.postgres.entities.HumanGateEntity;
import com.sparrowx.agentic.runtime.gate.HumanGate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Maps the auditable human-review gate aggregate.
 */
@Component
public final class HumanGateEntityMapper {

    public HumanGateEntity toEntity(HumanGate gate) {
        Objects.requireNonNull(
                gate,
                "gate must not be null"
        );

        return new HumanGateEntity(
                gate.gateId(),
                gate.tenantId(),
                gate.missionId(),
                gate.title(),
                gate.reason(),
                new LinkedHashSet<>(gate.requiredReviewerRoles()),
                gate.reviewPayload(),
                gate.status(),
                gate.decidedByUserId(),
                gate.decisionNote(),
                gate.createdAt(),
                gate.expiresAt(),
                gate.decidedAt()
        );
    }

    public void updateEntity(
            HumanGateEntity entity,
            HumanGate gate
    ) {
        Objects.requireNonNull(
                entity,
                "entity must not be null"
        );
        Objects.requireNonNull(
                gate,
                "gate must not be null"
        );

        requireSameIdentity(entity, gate);

        entity.setTitle(gate.title());
        entity.setReason(gate.reason());
        entity.setRequiredReviewerRoles(new LinkedHashSet<>(gate.requiredReviewerRoles()));
        entity.setReviewPayload(gate.reviewPayload());
        entity.setStatus(gate.status());
        entity.setDecidedByUserId(
                gate.decidedByUserId()
        );
        entity.setDecisionNote(gate.decisionNote());
        entity.setExpiresAt(gate.expiresAt());
        entity.setDecidedAt(gate.decidedAt());
    }

    public HumanGate toDomain(
            HumanGateEntity entity
    ) {
        Objects.requireNonNull(
                entity,
                "entity must not be null"
        );

        return new HumanGate(
                entity.getGateId(),
                entity.getTenantId(),
                entity.getMissionId(),
                entity.getTitle(),
                entity.getReason(),
                List.copyOf(entity.getRequiredReviewerRoles()),
                entity.getReviewPayload(),
                entity.getStatus(),
                entity.getDecidedByUserId(),
                entity.getDecisionNote(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getDecidedAt()
        );
    }

    private static void requireSameIdentity(
            HumanGateEntity entity,
            HumanGate gate
    ) {
        if (!entity.getGateId().equals(gate.gateId())
                || !entity.getTenantId().equals(
                gate.tenantId()
        )
                || !entity.getMissionId().equals(
                gate.missionId()
        )) {
            throw new IllegalArgumentException(
                    "Human gate identity cannot be changed"
            );
        }

        if (!entity.getCreatedAt().equals(
                gate.createdAt()
        )) {
            throw new IllegalArgumentException(
                    "Human gate createdAt cannot be changed"
            );
        }
    }
}
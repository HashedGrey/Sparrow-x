package com.sparrowx.agentic.runtime.gate;

import com.sparrowx.agentic.runtime.store.HumanGateStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public final class ApprovalService {

    private final HumanGateStore humanGateStore;

    public ApprovalService(HumanGateStore humanGateStore) {
        this.humanGateStore = Objects.requireNonNull(
                humanGateStore,
                "humanGateStore must not be null"
        );
    }

    public HumanGate open(OpenRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        HumanGate candidate = HumanGate.open(
                request.gateId(),
                request.tenantId(),
                request.missionId(),
                request.title(),
                request.reason(),
                request.requiredReviewerRoles(),
                request.reviewPayload(),
                request.createdAt(),
                request.expiresAt()
        );

        HumanGate existingById = humanGateStore.findById(
                request.tenantId(),
                request.missionId(),
                request.gateId()
        ).orElse(null);

        if (existingById != null) {
            return requireSameGateDefinition(candidate, existingById);
        }

        HumanGate existingOpen = humanGateStore.findOpenByMission(
                request.tenantId(),
                request.missionId()
        ).orElse(null);

        if (existingOpen != null) {
            if (existingOpen.gateId().equals(request.gateId())) {
                return requireSameGateDefinition(candidate, existingOpen);
            }

            throw new IllegalStateException(
                    "HUMAN_GATE_ALREADY_OPEN: mission already has gate "
                            + existingOpen.gateId()
            );
        }

        HumanGate persisted = Objects.requireNonNull(
                humanGateStore.create(candidate),
                "humanGateStore.create must not return null"
        );

        return requireSameGateDefinition(candidate, persisted);
    }

    public HumanGate approve(DecisionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        HumanGate current = load(
                request.tenantId(),
                request.missionId(),
                request.gateId()
        );

        HumanGate updated = current.approve(
                request.reviewerUserId(),
                request.reviewerRoles(),
                request.note(),
                request.decidedAt()
        );

        return persistTransition(current, updated);
    }

    public HumanGate reject(DecisionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        HumanGate current = load(
                request.tenantId(),
                request.missionId(),
                request.gateId()
        );

        HumanGate updated = current.reject(
                request.reviewerUserId(),
                request.reviewerRoles(),
                request.note(),
                request.decidedAt()
        );

        return persistTransition(current, updated);
    }

    public HumanGate expire(ExpiryRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        HumanGate current = load(
                request.tenantId(),
                request.missionId(),
                request.gateId()
        );

        HumanGate updated = current.expire(request.expiredAt());
        return persistTransition(current, updated);
    }

    private HumanGate load(
            String tenantId,
            String missionId,
            String gateId
    ) {
        HumanGate gate = humanGateStore.findById(
                tenantId,
                missionId,
                gateId
        ).orElseThrow(() -> new IllegalArgumentException(
                "HUMAN_GATE_NOT_FOUND: " + gateId
        ));

        if (!gate.tenantId().equals(tenantId)
                || !gate.missionId().equals(missionId)
                || !gate.gateId().equals(gateId)) {
            throw new IllegalStateException(
                    "HUMAN_GATE_STORE_SCOPE_MISMATCH"
            );
        }

        return gate;
    }

    private HumanGate persistTransition(
            HumanGate current,
            HumanGate updated
    ) {
        if (current == updated || current.equals(updated)) {
            return current;
        }

        HumanGate persisted = Objects.requireNonNull(
                humanGateStore.save(updated),
                "humanGateStore.save must not return null"
        );

        if (!persisted.equals(updated)) {
            throw new IllegalStateException(
                    "HUMAN_GATE_PERSISTENCE_CONFLICT"
            );
        }

        return persisted;
    }

    private static HumanGate requireSameGateDefinition(
            HumanGate expected,
            HumanGate actual
    ) {
        boolean sameDefinition = expected.gateId().equals(actual.gateId())
                && expected.tenantId().equals(actual.tenantId())
                && expected.missionId().equals(actual.missionId())
                && expected.title().equals(actual.title())
                && expected.reason().equals(actual.reason())
                && expected.requiredReviewerRoles().equals(
                actual.requiredReviewerRoles()
        )
                && expected.reviewPayload().equals(actual.reviewPayload())
                && expected.createdAt().equals(actual.createdAt())
                && expected.expiresAt().equals(actual.expiresAt());

        if (!sameDefinition) {
            throw new IllegalStateException(
                    "HUMAN_GATE_IDEMPOTENCY_CONFLICT: "
                            + "gateId was reused with different content"
            );
        }

        return actual;
    }

    public record OpenRequest(
            String gateId,
            String tenantId,
            String missionId,
            String title,
            String reason,
            List<String> requiredReviewerRoles,
            Map<String, Object> reviewPayload,
            Instant createdAt,
            Instant expiresAt
    ) {
        public OpenRequest {
            gateId = requireText(gateId, "gateId");
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            title = requireText(title, "title");
            reason = requireText(reason, "reason");
            requiredReviewerRoles = requiredReviewerRoles == null
                    ? List.of()
                    : List.copyOf(requiredReviewerRoles);
            reviewPayload = reviewPayload == null
                    ? Map.of()
                    : Map.copyOf(reviewPayload);
            createdAt = Objects.requireNonNull(
                    createdAt,
                    "createdAt must not be null"
            );
            expiresAt = Objects.requireNonNull(
                    expiresAt,
                    "expiresAt must not be null"
            );
        }
    }

    public record DecisionRequest(
            String tenantId,
            String missionId,
            String gateId,
            String reviewerUserId,
            Set<String> reviewerRoles,
            String note,
            Instant decidedAt
    ) {
        public DecisionRequest {
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            gateId = requireText(gateId, "gateId");
            reviewerUserId = requireText(
                    reviewerUserId,
                    "reviewerUserId"
            );
            reviewerRoles = reviewerRoles == null
                    ? Set.of()
                    : Set.copyOf(reviewerRoles);
            note = note == null ? "" : note.trim();
            decidedAt = Objects.requireNonNull(
                    decidedAt,
                    "decidedAt must not be null"
            );
        }
    }

    public record ExpiryRequest(
            String tenantId,
            String missionId,
            String gateId,
            Instant expiredAt
    ) {
        public ExpiryRequest {
            tenantId = requireText(tenantId, "tenantId");
            missionId = requireText(missionId, "missionId");
            gateId = requireText(gateId, "gateId");
            expiredAt = Objects.requireNonNull(
                    expiredAt,
                    "expiredAt must not be null"
            );
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "HUMAN_GATE_INVALID_REQUEST: "
                            + field + " must not be blank"
            );
        }
        return value.trim();
    }
}
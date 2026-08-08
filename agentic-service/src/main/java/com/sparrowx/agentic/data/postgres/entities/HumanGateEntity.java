package com.sparrowx.agentic.data.postgres.entities;

import com.sparrowx.agentic.runtime.gate.HumanGateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "agentic_human_gates",
        indexes = {
                @Index(
                        name = "idx_agentic_human_gate_open",
                        columnList = "tenant_id, mission_id, status, created_at"
                ),
                @Index(
                        name = "idx_agentic_human_gate_expiry",
                        columnList = "status, expires_at"
                )
        }
)
public class HumanGateEntity {

    @Id
    @Column(
            name = "gate_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String gateId;

    @Column(
            name = "tenant_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String tenantId;

    @Column(
            name = "mission_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String missionId;

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "required_reviewer_roles",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Set<String> requiredReviewerRoles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "review_payload",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, Object> reviewPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private HumanGateStatus status;

    @Column(name = "decided_by_user_id", nullable = false, length = 128)
    private String decidedByUserId;

    @Column(name = "decision_note", nullable = false, columnDefinition = "text")
    private String decisionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    protected HumanGateEntity() {
    }

    public HumanGateEntity(
            String gateId,
            String tenantId,
            String missionId,
            String title,
            String reason,
            Set<String> requiredReviewerRoles,
            Map<String, Object> reviewPayload,
            HumanGateStatus status,
            String decidedByUserId,
            String decisionNote,
            Instant createdAt,
            Instant expiresAt,
            Instant decidedAt
    ) {
        this.gateId = gateId;
        this.tenantId = tenantId;
        this.missionId = missionId;
        this.title = title;
        this.reason = reason;
        this.requiredReviewerRoles = immutableSet(requiredReviewerRoles);
        this.reviewPayload = immutableMap(reviewPayload);
        this.status = status;
        this.decidedByUserId = decidedByUserId;
        this.decisionNote = decisionNote;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.decidedAt = decidedAt;
    }

    public Set<String> getRequiredReviewerRoles() {
        return immutableSet(requiredReviewerRoles);
    }

    public Map<String, Object> getReviewPayload() {
        return immutableMap(reviewPayload);
    }

    public void setRequiredReviewerRoles(Set<String> requiredReviewerRoles) {
        this.requiredReviewerRoles = immutableSet(requiredReviewerRoles);
    }

    public void setReviewPayload(Map<String, Object> reviewPayload) {
        this.reviewPayload = immutableMap(reviewPayload);
    }

    private static Set<String> immutableSet(Set<String> value) {
        return value == null ? Set.of() : Set.copyOf(value);
    }

    private static Map<String, Object> immutableMap(Map<String, Object> value) {
        return value == null ? Map.of() : Map.copyOf(value);
    }
}
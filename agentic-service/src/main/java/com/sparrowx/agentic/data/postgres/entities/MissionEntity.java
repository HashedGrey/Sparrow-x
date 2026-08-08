package com.sparrowx.agentic.data.postgres.entities;

import com.sparrowx.agentic.mission.model.MissionPath;
import com.sparrowx.agentic.mission.model.MissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(
        name = "agentic_missions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_agentic_mission_tenant_request",
                        columnNames = {
                                "tenant_id",
                                "request_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_agentic_mission_tenant_request",
                        columnList = "tenant_id, request_id"
                ),
                @Index(
                        name = "idx_agentic_mission_tenant_user",
                        columnList =
                                "tenant_id, user_id, submitted_at"
                ),
                @Index(
                        name = "idx_agentic_mission_status",
                        columnList =
                                "tenant_id, status, updated_at"
                )
        }
)
public class MissionEntity {

    @Id
    @Column(name = "mission_id", nullable = false, updatable = false, length = 128)
    private String missionId;

    @Column(
            name = "request_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String requestId;

    @Column(
            name = "tenant_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String tenantId;

    @Column(
            name = "user_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String userId;

    @Column(
            name = "username",
            nullable = false,
            updatable = false,
            length = 256
    )
    private String username;

    @Column(
            name = "project_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String projectId;

    @Column(
            name = "team_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String teamId;

    @Column(
            name = "trace_id",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String traceId;

    @Column(
            name = "caller_service",
            nullable = false,
            updatable = false,
            length = 256
    )
    private String callerService;

    @Column(
            name = "session_id",
            nullable = false,
            updatable = false,
            length = 256
    )
    private String sessionId;

    @Column(
            name = "conversation_id",
            nullable = false,
            updatable = false,
            length = 256
    )
    private String conversationId;

    @Column(
            name = "client_channel",
            nullable = false,
            updatable = false,
            length = 128
    )
    private String clientChannel;

    @Column(
            name = "query",
            nullable = false,
            columnDefinition = "text"
    )
    private String query;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 32
    )
    private MissionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "selected_path",
            nullable = false,
            length = 32
    )
    private MissionPath selectedPath;

    @Column(
            name = "submitted_at",
            nullable = false,
            updatable = false
    )
    private Instant submittedAt;

    @Column(name = "terminal_at")
    private Instant terminalAt;

    /**
     * Canonical mapper-owned aggregate metadata and serialized value fields.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "metadata",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, Object> metadata;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    protected MissionEntity() {
    }

    public MissionEntity(
            String missionId,
            String requestId,
            String tenantId,
            String userId,
            String username,
            String projectId,
            String teamId,
            String traceId,
            String callerService,
            String sessionId,
            String conversationId,
            String clientChannel,
            String query,
            MissionStatus status,
            MissionPath selectedPath,
            Instant submittedAt,
            Instant terminalAt,
            Map<String, Object> metadata,
            Instant updatedAt
    ) {
        this.missionId = missionId;
        this.requestId = requestId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.username = username;
        this.projectId = projectId;
        this.teamId = teamId;
        this.traceId = traceId;
        this.callerService = callerService;
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.clientChannel = clientChannel;
        this.query = query;
        this.status = status;
        this.selectedPath = selectedPath;
        this.submittedAt = submittedAt;
        this.terminalAt = terminalAt;
        this.metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
        this.updatedAt = updatedAt;
    }



    public Map<String, Object> getMetadata() {
        return metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }


    public void setMetadata(
            Map<String, Object> metadata
    ) {
        this.metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public void replaceProjection(
            MissionStatus status,
            MissionPath selectedPath,
            Instant terminalAt,
            Map<String, Object> metadata,
            Instant updatedAt
    ) {
        this.status = status;
        this.selectedPath = selectedPath;
        this.terminalAt = terminalAt;
        this.metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
        this.updatedAt = updatedAt;
    }

}
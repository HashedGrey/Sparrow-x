package com.sparrowx.agentic.data.postgres.entities;

import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
@Getter
@Entity
@Table(
        name = "agentic_checkpoints",
        indexes = {
                @Index(
                        name = "idx_agentic_checkpoint_mission_type",
                        columnList = "tenant_id, mission_id, checkpoint_type"
                ),
                @Index(
                        name = "idx_agentic_checkpoint_created",
                        columnList = "tenant_id, mission_id, created_at"
                )
        }
)
public class CheckpointEntity {

    @Id
    @Column(
            name = "checkpoint_id",
            nullable = false,
            updatable = false,
            length = 256
    )
    private String checkpointId;

    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

    @Column(name = "mission_id", nullable = false, length = 128)
    private String missionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkpoint_type", nullable = false, length = 64)
    private CheckpointRef.CheckpointType checkpointType;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    @Column(name = "content_type", nullable = false, length = 256)
    private String contentType;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "snapshot_payload", nullable = false, columnDefinition = "bytea")
    private byte[] snapshotPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CheckpointEntity() {
    }

    public CheckpointEntity(
            String tenantId,
            String missionId,
            String checkpointId,
            CheckpointRef.CheckpointType checkpointType,
            int schemaVersion,
            String contentType,
            String sha256,
            long sizeBytes,
            byte[] snapshotPayload,
            Map<String, String> metadata,
            Instant createdAt
    ) {
        this.tenantId = tenantId;
        this.missionId = missionId;
        this.checkpointId = checkpointId;
        this.checkpointType = checkpointType;
        this.schemaVersion = schemaVersion;
        this.contentType = contentType;
        this.sha256 = sha256;
        this.sizeBytes = sizeBytes;
        this.snapshotPayload = copy(snapshotPayload);
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        this.createdAt = createdAt;
    }

    public byte[] getSnapshotPayload() {
        return copy(snapshotPayload);
    }

    private static byte[] copy(byte[] value) {
        return value == null ? new byte[0] : Arrays.copyOf(value, value.length);
    }
}
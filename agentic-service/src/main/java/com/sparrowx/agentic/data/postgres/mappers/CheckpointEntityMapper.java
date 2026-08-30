package com.sparrowx.agentic.data.postgres.mappers;

import com.sparrowx.agentic.data.postgres.entities.CheckpointEntity;
import com.sparrowx.agentic.exceptions.CheckpointCorruptionException;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointRef;
import com.sparrowx.agentic.runtime.checkpoint.CheckpointSnapshot;
import com.sparrowx.agentic.util.Jsons;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public final class CheckpointEntityMapper {

    private final Jsons jsons;

    public CheckpointEntityMapper(Jsons jsons) {
        this.jsons = Objects.requireNonNull(
                jsons,
                "jsons must not be null"
        );
    }

    public CheckpointEntity toEntity(
            CheckpointSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "snapshot must not be null"
        );

        CheckpointRef reference = snapshot.reference();

        return new CheckpointEntity(
                reference.tenantId(),
                reference.missionId(),
                reference.checkpointId(),
                reference.checkpointType(),
                reference.schemaVersion(),
                reference.contentType(),
                reference.sha256(),
                reference.sizeBytes(),
                jsons.writeBytes(snapshot),
                reference.metadata(),
                reference.createdAt()
        );
    }

    public CheckpointSnapshot toDomain(
            CheckpointEntity entity
    ) {
        Objects.requireNonNull(
                entity,
                "entity must not be null"
        );

        CheckpointSnapshot snapshot;

        try {
            snapshot = jsons.read(
                    entity.getSnapshotPayload(),
                    CheckpointSnapshot.class
            );
        } catch (RuntimeException exception) {
            throw new CheckpointCorruptionException(
                    "Unable to deserialize checkpoint "
                            + entity.getCheckpointId(),
                    exception
            );
        }

        CheckpointRef storedReference = toReference(entity);

        if (!storedReference.equals(snapshot.reference())) {
            throw new CheckpointCorruptionException(
                    "Checkpoint envelope does not match indexed "
                            + "checkpoint columns: "
                            + entity.getCheckpointId()
            );
        }

        return snapshot;
    }

    public CheckpointRef toReference(CheckpointEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return new CheckpointRef(
                entity.getCheckpointId(),
                entity.getTenantId(),
                entity.getMissionId(),
                entity.getCheckpointType(),
                entity.getSchemaVersion(),
                entity.getContentType(),
                entity.getSha256(),
                entity.getSizeBytes(),
                entity.getCreatedAt(),
                entity.getMetadata()
        );
    }
}
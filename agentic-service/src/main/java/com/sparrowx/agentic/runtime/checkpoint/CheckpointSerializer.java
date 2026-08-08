package com.sparrowx.agentic.runtime.checkpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.agentic.exceptions.CheckpointCorruptionException;
import com.sparrowx.agentic.util.Hashing;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Versioned checkpoint serialization using the canonical JSON mapper.
 */
@Component
public final class CheckpointSerializer {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final ObjectMapper objectMapper;

    public CheckpointSerializer(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public CheckpointSnapshot serialize(
            String checkpointId,
            String tenantId,
            String missionId,
            CheckpointRef.CheckpointType checkpointType,
            int schemaVersion,
            Instant createdAt,
            Map<String, String> metadata,
            Object value
    ) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(value);
            CheckpointRef reference = new CheckpointRef(
                    checkpointId,
                    tenantId,
                    missionId,
                    checkpointType,
                    schemaVersion,
                    JSON_CONTENT_TYPE,
                    Hashing.sha256Hex(payload),
                    payload.length,
                    createdAt,
                    metadata
            );
            return new CheckpointSnapshot(reference, payload);
        } catch (JsonProcessingException exception) {
            throw new CheckpointCorruptionException(
                    "Unable to serialize checkpoint " + checkpointId,
                    exception
            );
        }
    }

    public <T> T deserialize(
            CheckpointSnapshot snapshot,
            Class<T> targetType
    ) {
        validate(snapshot);
        try {
            return objectMapper.readValue(
                    snapshot.payload(),
                    targetType
            );
        } catch (IOException exception) {
            throw corrupted(snapshot, exception);
        }
    }

    public <T> T deserialize(
            CheckpointSnapshot snapshot,
            TypeReference<T> targetType
    ) {
        validate(snapshot);
        try {
            return objectMapper.readValue(
                    snapshot.payload(),
                    targetType
            );
        } catch (IOException exception) {
            throw corrupted(snapshot, exception);
        }
    }

    private static void validate(CheckpointSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        CheckpointRef reference = snapshot.reference();
        byte[] payload = snapshot.payload();

        if (reference.sizeBytes() != payload.length) {
            throw new CheckpointCorruptionException(
                    "Checkpoint size mismatch: " + reference.checkpointId()
            );
        }

        if (!reference.sha256().equals(Hashing.sha256Hex(payload))) {
            throw new CheckpointCorruptionException(
                    "Checkpoint hash mismatch: " + reference.checkpointId()
            );
        }
    }

    private static CheckpointCorruptionException corrupted(
            CheckpointSnapshot snapshot,
            IOException cause
    ) {
        return new CheckpointCorruptionException(
                "Unable to deserialize checkpoint "
                        + snapshot.reference().checkpointId(),
                cause
        );
    }
}
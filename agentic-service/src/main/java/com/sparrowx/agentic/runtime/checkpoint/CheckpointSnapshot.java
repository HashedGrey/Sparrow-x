package com.sparrowx.agentic.runtime.checkpoint;

import java.util.Objects;

/**
 * Frozen serialized payload and its validated durable reference.
 */
public record CheckpointSnapshot(
        CheckpointRef reference,
        byte[] payload
) {

    public CheckpointSnapshot {
        reference = Objects.requireNonNull(reference, "reference");
        payload = payload == null ? new byte[0] : payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }
}
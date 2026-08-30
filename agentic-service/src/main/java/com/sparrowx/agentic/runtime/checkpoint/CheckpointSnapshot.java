package com.sparrowx.agentic.runtime.checkpoint;

import java.util.Arrays;
import java.util.Objects;

/**
 * Frozen serialized payload and its validated durable reference.
 */
public record CheckpointSnapshot(
        CheckpointRef reference,
        byte[] payload
) {

    public CheckpointSnapshot {
        reference = Objects.requireNonNull(
                reference,
                "reference"
        );

        payload = payload == null
                ? new byte[0]
                : payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CheckpointSnapshot other)) {
            return false;
        }

        return reference.equals(other.reference)
                && Arrays.equals(payload, other.payload);
    }

    @Override
    public int hashCode() {
        return 31 * reference.hashCode()
                + Arrays.hashCode(payload);
    }
}
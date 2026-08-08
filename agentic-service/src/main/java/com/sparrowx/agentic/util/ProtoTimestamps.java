package com.sparrowx.agentic.util;

import com.google.protobuf.Timestamp;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.Objects;

/**
 * Java/protobuf timestamp conversion.
 */
public final class ProtoTimestamps {

    private static final long MIN_PROTO_SECONDS = -62_135_596_800L;
    private static final long MAX_PROTO_SECONDS = 253_402_300_799L;
    private static final int MAX_NANOS = 999_999_999;

    private ProtoTimestamps() {
    }

    public static Timestamp toProto(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");

        validateSeconds(instant.getEpochSecond());

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static Instant fromProto(Timestamp timestamp) {
        Objects.requireNonNull(timestamp, "timestamp must not be null");

        validateSeconds(timestamp.getSeconds());
        validateNanos(timestamp.getNanos());

        try {
            return Instant.ofEpochSecond(
                    timestamp.getSeconds(),
                    timestamp.getNanos()
            );
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    "Invalid protobuf timestamp",
                    exception
            );
        }
    }

    public static Timestamp toProtoOrDefault(Instant instant) {
        return instant == null
                ? Timestamp.getDefaultInstance()
                : toProto(instant);
    }

    public static Instant fromProtoOrNull(Timestamp timestamp) {
        return timestamp == null
                ? null
                : fromProto(timestamp);
    }

    public static boolean isValid(Timestamp timestamp) {
        if (timestamp == null) {
            return false;
        }

        return timestamp.getSeconds() >= MIN_PROTO_SECONDS
                && timestamp.getSeconds() <= MAX_PROTO_SECONDS
                && timestamp.getNanos() >= 0
                && timestamp.getNanos() <= MAX_NANOS;
    }

    private static void validateSeconds(long seconds) {
        if (seconds < MIN_PROTO_SECONDS || seconds > MAX_PROTO_SECONDS) {
            throw new IllegalArgumentException(
                    "Timestamp seconds are outside protobuf range: "
                            + seconds
            );
        }
    }

    private static void validateNanos(int nanos) {
        if (nanos < 0 || nanos > MAX_NANOS) {
            throw new IllegalArgumentException(
                    "Timestamp nanos must be between 0 and "
                            + MAX_NANOS
                            + ": "
                            + nanos
            );
        }
    }
}
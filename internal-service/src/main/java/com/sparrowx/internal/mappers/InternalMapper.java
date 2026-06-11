package com.sparrowx.internal.mappers;

import com.google.protobuf.Timestamp;
import com.sparrowx.internal.grpc.RequestContext;

import java.time.Instant;

public final class InternalMapper {

    private InternalMapper() {
    }

    public static String tenantId(RequestContext context) {
        return context == null ? "" : context.getTenantId();
    }

    public static String actorId(RequestContext context) {
        return context == null ? "" : context.getActorId();
    }

    public static String requestId(RequestContext context) {
        return context == null ? "" : context.getRequestId();
    }

    public static String value(Object valueObject) {
        if (valueObject == null) {
            return "";
        }

        try {
            var method = valueObject.getClass().getMethod("value");
            var value = method.invoke(valueObject);
            return value == null ? "" : value.toString();
        } catch (Exception ignored) {
            return valueObject.toString();
        }
    }

    public static Timestamp toTimestamp(Object timestampLike) {
        if (timestampLike == null) {
            return null;
        }

        var instant = toInstant(timestampLike);

        if (instant == null) {
            return null;
        }

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private static Instant toInstant(Object timestampLike) {
        if (timestampLike instanceof Instant instant) {
            return instant;
        }

        try {
            var method = timestampLike.getClass().getMethod("value");
            var value = method.invoke(timestampLike);

            if (value instanceof Instant instant) {
                return instant;
            }

            if (value != null) {
                return Instant.parse(value.toString());
            }

            return null;
        } catch (Exception ignored) {
            try {
                return Instant.parse(timestampLike.toString());
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }
}
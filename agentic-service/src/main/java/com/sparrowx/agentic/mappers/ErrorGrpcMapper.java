package com.sparrowx.agentic.mappers;

import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.sparrowx.agentic.grpc.GrpcExceptionHandler;
import com.sparrowx.agentic.mission.model.MissionFailure;
import com.sparrowx.agentic.proto.MissionError;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public final class ErrorGrpcMapper {

    private final GrpcExceptionHandler exceptionHandler;

    public ErrorGrpcMapper(
            GrpcExceptionHandler exceptionHandler
    ) {
        this.exceptionHandler = Objects.requireNonNull(
                exceptionHandler,
                "exceptionHandler must not be null"
        );
    }

    public MissionError toProto(MissionFailure failure) {
        Objects.requireNonNull(
                failure,
                "failure must not be null"
        );

        Map<String, Object> details =
                new LinkedHashMap<>(failure.details());

        if (failure.reason() != null) {
            details.putIfAbsent(
                    "reason",
                    failure.reason().name()
            );
        }

        return MissionError.newBuilder()
                .setCode(failure.code())
                .setMessage(failure.message())
                .setRetryable(failure.retryable())
                .setFailedStageId(failure.failedStageId())
                .setFailedStepId(failure.failedStepId())
                .setFailedComponentId(
                        failure.failedComponentId()
                )
                .setDetails(toStruct(details))
                .build();
    }

    public MissionError toProto(Throwable throwable) {
        Throwable resolved = throwable == null
                ? new IllegalStateException(
                "Unknown Agentic service failure"
        )
                : throwable;

        return MissionError.newBuilder()
                .setCode(
                        resolved.getClass().getSimpleName()
                )
                .setMessage(
                        resolved.getMessage() == null
                                ? "Agentic service operation failed"
                                : resolved.getMessage()
                )
                .setRetryable(false)
                .build();
    }

    public StatusRuntimeException toException(
            Throwable throwable
    ) {
        return exceptionHandler.toStatusRuntimeException(
                throwable
        );
    }

    private static Struct toStruct(Map<String, ?> source) {
        Struct.Builder builder = Struct.newBuilder();

        if (source == null) {
            return builder.build();
        }

        source.forEach((key, value) -> {
            if (key != null) {
                builder.putFields(key, toValue(value));
            }
        });

        return builder.build();
    }

    private static Value toValue(Object value) {
        Value.Builder builder = Value.newBuilder();

        if (value == null) {
            return builder
                    .setNullValue(NullValue.NULL_VALUE)
                    .build();
        }

        if (value instanceof Boolean booleanValue) {
            return builder
                    .setBoolValue(booleanValue)
                    .build();
        }

        if (value instanceof Number numberValue) {
            return builder
                    .setNumberValue(numberValue.doubleValue())
                    .build();
        }

        if (value instanceof Map<?, ?> mapValue) {
            Struct.Builder struct = Struct.newBuilder();

            mapValue.forEach((key, nestedValue) -> {
                if (key != null) {
                    struct.putFields(
                            String.valueOf(key),
                            toValue(nestedValue)
                    );
                }
            });

            return builder.setStructValue(struct).build();
        }

        if (value instanceof Iterable<?> iterable) {
            ListValue.Builder list = ListValue.newBuilder();

            iterable.forEach(item ->
                    list.addValues(toValue(item))
            );

            return builder.setListValue(list).build();
        }

        if (value.getClass().isArray()) {
            ListValue.Builder list = ListValue.newBuilder();
            int length = Array.getLength(value);

            for (int index = 0; index < length; index++) {
                list.addValues(
                        toValue(Array.get(value, index))
                );
            }

            return builder.setListValue(list).build();
        }

        return builder
                .setStringValue(
                        value instanceof Enum<?> enumValue
                                ? enumValue.name()
                                : String.valueOf(value)
                )
                .build();
    }
}
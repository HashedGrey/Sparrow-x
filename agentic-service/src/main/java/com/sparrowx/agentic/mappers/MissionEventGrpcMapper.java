package com.sparrowx.agentic.mappers;

import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.sparrowx.agentic.features.streammissionprogress.MissionProgressEventView;
import com.sparrowx.agentic.mission.model.ComponentInvocation;
import com.sparrowx.agentic.proto.MissionProgressEvent;
import com.sparrowx.agentic.util.ProtoTimestamps;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;

@Component
public final class MissionEventGrpcMapper {

    public MissionProgressEvent toProto(
            MissionProgressEventView view
    ) {
        Objects.requireNonNull(view, "view must not be null");

        MissionProgressEvent.Builder builder =
                MissionProgressEvent.newBuilder()
                        .setMissionId(view.missionId())
                        .setStatus(toProtoStatus(view.status()))
                        .setStageId(view.stageId())
                        .setStageName(view.stageName())
                        .setStepId(view.stepId())
                        .setStepName(view.stepName())
                        .setStepStatus(
                                toProtoStepStatus(view.stepStatus())
                        )
                        .setMessage(view.message())
                        .setProgressPercent(
                                view.progressPercent()
                        )
                        .setResumeToken(view.resumeToken())
                        .setEmittedAt(
                                ProtoTimestamps.toProto(
                                        view.emittedAt()
                                )
                        )
                        .putAllMetadata(view.metadata());

        if (view.currentComponent() != null) {
            builder.setCurrentComponent(
                    toProto(view.currentComponent())
            );
        }

        return builder.build();
    }

    public com.sparrowx.agentic.proto.ComponentInvocation
    toProto(ComponentInvocation invocation) {
        Objects.requireNonNull(
                invocation,
                "invocation must not be null"
        );

        var builder =
                com.sparrowx.agentic.proto.ComponentInvocation
                        .newBuilder()
                        .setComponentId(invocation.componentId())
                        .setComponentKind(
                                toProtoComponentKind(
                                        invocation.componentKind()
                                )
                        )
                        .setComponentName(
                                invocation.componentName()
                        )
                        .setInputSummary(
                                toStruct(invocation.inputSummary())
                        )
                        .setOutputSummary(
                                toStruct(invocation.outputSummary())
                        );

        if (invocation.startedAt() != null) {
            builder.setStartedAt(
                    ProtoTimestamps.toProto(
                            invocation.startedAt()
                    )
            );
        }

        if (invocation.completedAt() != null) {
            builder.setCompletedAt(
                    ProtoTimestamps.toProto(
                            invocation.completedAt()
                    )
            );
        }

        return builder.build();
    }

    private static com.sparrowx.agentic.proto.MissionStatus
    toProtoStatus(Object status) {
        return switch (enumName(status)) {
            case "SUBMITTED" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_SUBMITTED;
            case "RUNNING" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_RUNNING;
            case "WAITING_APPROVAL" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_WAITING_APPROVAL;
            case "FAILED_RETRYABLE" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_FAILED_RETRYABLE;
            case "FAILED_TERMINAL" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_FAILED_TERMINAL;
            case "COMPLETED" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_COMPLETED;
            case "CANCELLED" ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_CANCELLED;
            default ->
                    com.sparrowx.agentic.proto.MissionStatus
                            .MISSION_STATUS_UNSPECIFIED;
        };
    }

    private static com.sparrowx.agentic.proto.StepStatus
    toProtoStepStatus(Object status) {
        return switch (enumName(status)) {
            case "QUEUED" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_QUEUED;
            case "RUNNING" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_RUNNING;
            case "PAUSED" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_PAUSED;
            case "SUCCEEDED" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_SUCCEEDED;
            case "FAILED_RETRYABLE" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_FAILED_RETRYABLE;
            case "FAILED_TERMINAL" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_FAILED_TERMINAL;
            case "CANCELLED" ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_CANCELLED;
            default ->
                    com.sparrowx.agentic.proto.StepStatus
                            .STEP_STATUS_UNSPECIFIED;
        };
    }

    private static com.sparrowx.agentic.proto.ComponentKind
    toProtoComponentKind(Object kind) {
        return switch (enumName(kind)) {
            case "CONTEXT_PREPARATION" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_CONTEXT_PREPARATION;
            case "INTENT_INTERPRETATION" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_INTENT_INTERPRETATION;
            case "PLANNING" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_PLANNING;
            case "DOCUMENT_EVIDENCE" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_DOCUMENT_EVIDENCE;
            case "INTERNAL_CONTEXT" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_INTERNAL_CONTEXT;
            case "SYNTHESIS" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_SYNTHESIS;
            case "GOVERNANCE" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_GOVERNANCE;
            case "HUMAN_GATE" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_HUMAN_GATE;
            case "CUSTOM" ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_CUSTOM;
            default ->
                    com.sparrowx.agentic.proto.ComponentKind
                            .COMPONENT_KIND_UNSPECIFIED;
        };
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

    private static String enumName(Object value) {
        if (value == null) {
            return "UNSPECIFIED";
        }

        return value instanceof Enum<?> enumValue
                ? enumValue.name()
                : value.toString();
    }
}
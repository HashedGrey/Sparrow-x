package com.sparrowx.agentic.data.postgres.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.agentic.data.postgres.entities.RuntimeEventEntity;
import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public final class RuntimeEventEntityMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public RuntimeEventEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public RuntimeEventEntity toEntity(
            String tenantId,
            MissionProgressEvent event
    ) {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(event, "event");

        if (event.resumeToken().isBlank()) {
            throw new IllegalArgumentException(
                    "A persisted progress event requires a resume token"
            );
        }

        return new RuntimeEventEntity(
                tenantId,
                event.missionId(),
                event.resumeToken(),
                event.status(),
                objectMapper.convertValue(event, MAP_TYPE),
                event.emittedAt()
        );
    }

    public MissionProgressEvent toDomain(RuntimeEventEntity entity) {
        Objects.requireNonNull(entity, "entity");

        try {
            MissionProgressEvent event = objectMapper.convertValue(
                    entity.getEventPayload(),
                    MissionProgressEvent.class
            );

            validateProjection(entity, event);
            return event;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid persisted runtime event: "
                            + entity.getResumeToken(),
                    exception
            );
        }
    }

    private static void validateProjection(
            RuntimeEventEntity entity,
            MissionProgressEvent event
    ) {
        if (!entity.getMissionId().equals(event.missionId())
                || !entity.getResumeToken().equals(event.resumeToken())
                || entity.getMissionStatus() != event.status()) {
            throw new IllegalStateException(
                    "Runtime event payload does not match its projection: "
                            + entity.getResumeToken()
            );
        }
    }
}
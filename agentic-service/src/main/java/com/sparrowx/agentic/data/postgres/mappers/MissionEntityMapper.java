package com.sparrowx.agentic.data.postgres.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowx.agentic.data.postgres.entities.MissionEntity;
import com.sparrowx.agentic.mission.model.Mission;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public final class MissionEntityMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public MissionEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public MissionEntity toEntity(Mission mission) {
        Objects.requireNonNull(mission, "mission must not be null");

        MissionContext context = mission.context();

        return new MissionEntity(
                mission.missionId(),
                context.requestId(),
                mission.tenantId(),
                context.userId(),
                context.username(),
                context.projectId(),
                context.teamId(),
                context.traceId(),
                context.callerService(),
                context.sessionId(),
                context.conversationId(),
                context.clientChannel(),
                mission.query(),
                mission.status(),
                mission.selectedPath(),
                mission.submittedAt(),
                mission.completedAt(),
                serializeMission(mission),
                mission.updatedAt()
        );
    }

    public void updateEntity(
            MissionEntity entity,
            Mission mission
    ) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(mission, "mission must not be null");

        Mission existing = toDomain(entity);

        if (!entity.getMissionId().equals(mission.missionId())
                || !entity.getTenantId().equals(mission.tenantId())
                || !entity.getRequestId().equals(
                mission.context().requestId()
        )
                || !existing.requestFingerprint().equals(
                mission.requestFingerprint()
        )) {
            throw new IllegalArgumentException(
                    "Cannot change persisted mission identity"
            );
        }

        entity.replaceProjection(
                mission.status(),
                mission.selectedPath(),
                mission.completedAt(),
                serializeMission(mission),
                mission.updatedAt()
        );
    }

    public Mission toDomain(MissionEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        try {
            Mission mission = objectMapper.convertValue(
                    entity.getMetadata(),
                    Mission.class
            );

            validateProjection(entity, mission);
            return mission;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid persisted mission payload: "
                            + entity.getMissionId(),
                    exception
            );
        }
    }

    private Map<String, Object> serializeMission(Mission mission) {
        return objectMapper.convertValue(mission, MAP_TYPE);
    }

    private static void validateProjection(
            MissionEntity entity,
            Mission mission
    ) {
        if (!entity.getMissionId().equals(mission.missionId())
                || !entity.getTenantId().equals(mission.tenantId())
                || !entity.getRequestId().equals(
                mission.context().requestId()
        )
                || entity.getStatus() != mission.status()
                || entity.getSelectedPath() != mission.selectedPath()
                || !entity.getSubmittedAt().equals(
                mission.submittedAt()
        )
                || !Objects.equals(
                entity.getTerminalAt(),
                mission.completedAt()
        )) {
            throw new IllegalStateException(
                    "Mission payload does not match persisted projection: "
                            + entity.getMissionId()
            );
        }
    }
}
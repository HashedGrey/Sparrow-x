package com.sparrowx.agentic.mission;

import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.runtime.store.RuntimeEventStore;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MissionEventPublisher {

    private final RuntimeEventStore eventStore;

    public MissionEventPublisher(RuntimeEventStore eventStore) {
        this.eventStore = Objects.requireNonNull(eventStore, "eventStore");
    }

    public MissionProgressEvent publish(
            String tenantId,
            MissionProgressEvent event
    ) {
        Objects.requireNonNull(event, "event");

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        if (event.missionId().isBlank()) {
            throw new IllegalArgumentException("missionId must not be blank");
        }
        if (event.resumeToken().isBlank()) {
            throw new IllegalArgumentException("resumeToken must not be blank");
        }
        if (event.progressPercent() < 0.0
                || event.progressPercent() > 100.0) {
            throw new IllegalArgumentException(
                    "progressPercent must be between 0 and 100"
            );
        }

        return eventStore.append(tenantId, event);
    }
}
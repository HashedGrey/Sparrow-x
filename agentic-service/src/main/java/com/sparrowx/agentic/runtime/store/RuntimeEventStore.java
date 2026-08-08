package com.sparrowx.agentic.runtime.store;

import com.sparrowx.agentic.mission.model.MissionProgressEvent;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Tenant-scoped persistence and tailing port for public progress events.
 */
public interface RuntimeEventStore {

    /**
     * Idempotently appends by stable resume token.
     */
    MissionProgressEvent append(
            String tenantId,
            MissionProgressEvent event
    );

    /**
     * Reads strictly after the supplied token. An empty token starts at the
     * earliest retained event.
     */
    List<MissionProgressEvent> readAfter(
            String tenantId,
            String missionId,
            String resumeToken,
            int limit
    );

    /**
     * Creates a durable cursor that backfills events committed between replay
     * completion and subscription creation.
     */
    EventSubscription subscribeAfter(
            String tenantId,
            String missionId,
            String resumeToken
    );

    interface EventSubscription extends AutoCloseable {

        Optional<MissionProgressEvent> next(Duration waitTimeout);

        String resumeToken();

        @Override
        void close();
    }
}
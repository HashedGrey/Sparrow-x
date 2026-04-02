package buildingblocks.core.events;

import java.time.Instant;
import java.util.UUID;

public interface IntegrationEvent {
    UUID getEventId();
    Instant getOccurredAt();
    String getEventType();
}
package buildingblocks.infrastructure.messaging.inbox;

import java.util.UUID;

public interface InboxDeduplication {
    boolean alreadyProcessed(UUID messageId);
    void markProcessed(UUID messageId, String eventType);
}
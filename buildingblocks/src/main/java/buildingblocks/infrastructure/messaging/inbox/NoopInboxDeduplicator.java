package buildingblocks.infrastructure.messaging.inbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnMissingBean(InboxDeduplication.class)
public class NoopInboxDeduplicator implements InboxDeduplication {

    @Override
    public boolean alreadyProcessed(UUID messageId) {
        return false;
    }

    @Override
    public void markProcessed(UUID messageId, String eventType) {
        // inbox disabled
    }
}
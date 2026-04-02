package buildingblocks.infrastructure.messaging.inbox;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class InboxDeduplicator {

    private final InboxRepository repository;

    public InboxDeduplicator(InboxRepository repository) {
        this.repository = repository;
    }

    public boolean alreadyProcessed(UUID messageId) {
        return repository.existsById(messageId);
    }

    @Transactional
    public void markProcessed(UUID messageId, String eventType) {

        try {
            repository.save(new InboxMessage(messageId, eventType));
        } catch (DataIntegrityViolationException ex) {
            // duplicate = already processed
        }
    }
}
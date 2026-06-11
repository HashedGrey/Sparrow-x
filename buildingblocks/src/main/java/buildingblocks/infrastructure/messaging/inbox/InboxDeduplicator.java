package buildingblocks.infrastructure.messaging.inbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@ConditionalOnBean(InboxRepository.class)
public class InboxDeduplicator implements InboxDeduplication {

    private final InboxRepository repository;

    public InboxDeduplicator(InboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean alreadyProcessed(UUID messageId) {
        return repository.existsById(messageId);
    }

    @Override
    @Transactional
    public void markProcessed(UUID messageId, String eventType) {
        try {
            repository.save(new InboxMessage(messageId, eventType));
        } catch (DataIntegrityViolationException ex) {
            // duplicate = already processed
        }
    }
}
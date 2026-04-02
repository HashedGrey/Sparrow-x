package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.infrastructure.persistence.outbox.model.OutboxMessage;
import buildingblocks.infrastructure.persistence.outbox.model.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository
        extends JpaRepository<OutboxMessage, UUID> {

    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(
            MessageStatus status,
            Pageable pageable
    );

    List<OutboxMessage> findByStatusAndNextRetryAtBeforeOrderByCreatedAtAsc(
            MessageStatus status,
            Instant time,
            Pageable pageable
    );

    boolean existsByIdAndStatus(
            UUID id,
            MessageStatus status
    );
}
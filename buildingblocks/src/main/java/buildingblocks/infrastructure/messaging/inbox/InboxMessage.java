package buildingblocks.infrastructure.messaging.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inbox_messages",
        indexes = {
                @Index(name = "idx_inbox_processed_at", columnList = "processedAt")
        }
)
public class InboxMessage {

    @Id
    private UUID id;

    private String eventType;

    private LocalDateTime processedAt;

    protected InboxMessage() {}

    public InboxMessage(UUID id, String eventType) {
        this.id = id;
        this.eventType = eventType;
        this.processedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
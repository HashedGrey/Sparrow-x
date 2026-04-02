package buildingblocks.infrastructure.persistence.outbox.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_messages",
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
                @Index(name = "idx_outbox_retry", columnList = "status, next_retry_at")
        }
)
@Getter
public class OutboxMessage {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topic;

    // Kafka partition key (ensures ordering per aggregate)
    private String messageKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    // Optional metadata (traceId, correlationId, tenantId etc)
    @Column(columnDefinition = "TEXT")
    private String headers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    private int attempts;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Version
    private Long version;

    protected OutboxMessage() {}

    public OutboxMessage(
            UUID id,
            String aggregateType,
            String eventType,
            String payload,
            String topic,
            String messageKey,
            String headers
    ) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.topic = topic;
        this.messageKey = messageKey;
        this.headers = headers;
        this.status = MessageStatus.PENDING;
        this.createdAt = Instant.now();
        this.attempts = 0;
        this.nextRetryAt = Instant.now();
    }

    public void markProcessing() {
        this.status = MessageStatus.PROCESSING;
    }

    public void markProcessed() {
        this.status = MessageStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.nextRetryAt = null;
    }

    public void markFailed(String error) {
        this.status = MessageStatus.FAILED;
        this.lastError = error;
        this.attempts++;

        // simple exponential backoff
        long backoffSeconds = Math.min(60, (long) Math.pow(2, attempts));
        this.nextRetryAt = Instant.now().plusSeconds(backoffSeconds);
    }

    public void markDeadLetter(String error) {
        this.status = MessageStatus.DEAD_LETTER;
        this.lastError = error;
        this.nextRetryAt = null;
    }

    public boolean canRetry(int maxRetries) {
        return attempts < maxRetries;
    }
}
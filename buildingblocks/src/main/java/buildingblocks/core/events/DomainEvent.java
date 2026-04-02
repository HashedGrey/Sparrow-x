package buildingblocks.core.events;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID eventId;
    private final UUID aggregateId;
    private final Instant occurredAt;

    protected DomainEvent(UUID aggregateId) {

        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId must not be null");
        }

        this.eventId = UuidCreator.getTimeOrderedEpoch();
        this.aggregateId = aggregateId;
        this.occurredAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getEventType() {
        return getClass().getSimpleName();
    }
}
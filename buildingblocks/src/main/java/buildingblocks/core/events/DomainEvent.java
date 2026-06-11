package buildingblocks.core.events;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainEvent {

    private final UUID eventId;
    private final String aggregateId;
    private final Instant occurredAt;

    protected DomainEvent(String aggregateId) {

        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId must not be null");
        }

        this.eventId = UuidCreator.getTimeOrderedEpoch();
        this.aggregateId = aggregateId;
        this.occurredAt = Instant.now();
    }

    public String getEventType() {
        return getClass().getSimpleName();
    }
}
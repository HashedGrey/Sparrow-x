package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.core.events.DomainEvent;

public interface DomainEventOutbox {
    void write(DomainEvent event);
}
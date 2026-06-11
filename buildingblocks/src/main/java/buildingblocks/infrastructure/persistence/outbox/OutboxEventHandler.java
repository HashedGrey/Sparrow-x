package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.core.events.DomainEvent;
import buildingblocks.core.events.EventHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(DomainEventOutbox.class)
public class OutboxEventHandler implements EventHandler<DomainEvent> {

    private final DomainEventOutbox writer;

    public OutboxEventHandler(DomainEventOutbox writer) {
        this.writer = writer;
    }

    @Override
    public void handle(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("DomainEvent cannot be null");
        }

        writer.write(event);
    }
}
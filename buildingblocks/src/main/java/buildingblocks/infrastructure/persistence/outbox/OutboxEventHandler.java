package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.core.events.DomainEvent;
import buildingblocks.core.events.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventHandler implements EventHandler<DomainEvent> {

    private final OutboxWriter writer;

    public OutboxEventHandler(OutboxWriter writer) {
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
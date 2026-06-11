package buildingblocks.infrastructure.persistence.outbox;

import buildingblocks.core.events.DomainEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(DomainEventOutbox.class)
public class NoopDomainEventOutbox implements DomainEventOutbox {

    @Override
    public void write(DomainEvent event) {
        // outbox disabled
    }
}
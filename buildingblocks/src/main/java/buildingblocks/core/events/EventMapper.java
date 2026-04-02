package buildingblocks.core.events;

import java.util.Optional;

public interface EventMapper {

    IntegrationEvent mapToIntegrationEvent(DomainEvent event);

    //InternalCommand mapToInternalCommand(DomainEvent event);
    Optional<InternalCommand<?>> mapToInternalCommand(DomainEvent event);
}
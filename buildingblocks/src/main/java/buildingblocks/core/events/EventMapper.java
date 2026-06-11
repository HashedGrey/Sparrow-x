package buildingblocks.core.events;

import java.util.Optional;

public interface EventMapper {

    Optional<IntegrationEvent> mapToIntegrationEvent(DomainEvent event);

    Optional<InternalCommand<?>> mapToInternalCommand(DomainEvent event);
}
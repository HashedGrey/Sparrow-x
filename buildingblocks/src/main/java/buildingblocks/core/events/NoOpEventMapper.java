package buildingblocks.core.events;

import java.util.Optional;

public final class NoOpEventMapper implements EventMapper {

    @Override
    public Optional<IntegrationEvent> mapToIntegrationEvent(DomainEvent event) {
        return Optional.empty();
    }

    @Override
    public Optional<InternalCommand<?>> mapToInternalCommand(DomainEvent event) {
        return Optional.empty();
    }
}
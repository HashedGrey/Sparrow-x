package com.sparrowx.document.mappers;

import buildingblocks.core.events.DomainEvent;
import buildingblocks.core.events.EventMapper;
import buildingblocks.core.events.IntegrationEvent;
import buildingblocks.core.events.InternalCommand;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public Optional<IntegrationEvent> mapToIntegrationEvent(DomainEvent event) {
        return Optional.empty();
    }

    @Override
    public Optional<InternalCommand<?>> mapToInternalCommand(DomainEvent event) {
        return Optional.empty();
    }
}
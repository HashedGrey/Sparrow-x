package com.sparrowx.profile.mappers;

import buildingblocks.contracts.profile.ProfileCreated;
import buildingblocks.core.event.DomainEvent;
import buildingblocks.core.event.EventMapper;
import buildingblocks.core.event.IntegrationEvent;
import buildingblocks.core.event.InternalCommand;
import com.sparrowx.profile.features.profile.createprofile.CreateProfileCassandraCommand;
import com.sparrowx.profile.features.profile.createprofile.ProfileCreatedDomainEvent;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public IntegrationEvent MapToIntegrationEvent(DomainEvent event) {

        return switch (event) {
            case ProfileCreatedDomainEvent e -> new ProfileCreated(
                    e.profileId().value(),
                    e.userName().getUserName(),
                    e.fullName().getFullName(),
                    e.email().getEmail(),
                    e.avatarUrl().getAvatarUrl(),
                    e.createdAt());
            default -> null;
        };
    }

    @Override
    public InternalCommand MapToInternalCommand(DomainEvent event) {
        return switch (event) {
            case ProfileCreatedDomainEvent e -> new CreateProfileCassandraCommand(
                    e.profileId(),
                    e.userName(),
                    e.fullName(),
                    e.email(),
                    e.avatarUrl().getAvatarUrl(),
                    e.createdAt());
            default -> null;
        };
    }
}

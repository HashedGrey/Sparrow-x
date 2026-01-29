package com.sparrowx.profile.features.profile.updateprofile;

import buildingblocks.core.event.DomainEvent;

import java.util.UUID;

public record ProfileUpdatedDomainEvent(
        UUID id,
        String name,
        String bio,
        String avatarUrl,
        boolean isDeleted
) implements DomainEvent {
}
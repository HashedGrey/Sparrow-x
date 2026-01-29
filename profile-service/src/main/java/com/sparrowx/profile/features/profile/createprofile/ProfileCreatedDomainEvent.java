package com.sparrowx.profile.features.profile.createprofile;

import buildingblocks.core.event.DomainEvent;
import com.sparrowx.profile.valueobjects.*;

import java.time.Instant;

public record ProfileCreatedDomainEvent(
        ProfileId profileId,
        UserName userName,
        FullName fullName,
        Email email,
        AvatarUrl avatarUrl,
        Instant createdAt
        ) implements DomainEvent {
}
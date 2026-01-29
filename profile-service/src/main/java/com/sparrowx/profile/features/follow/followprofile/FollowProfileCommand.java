package com.sparrowx.profile.features.follow.followprofile;

import buildingblocks.core.event.InternalCommand;
import buildingblocks.mediator.abstractions.commands.ICommand;
import buildingblocks.mediator.abstractions.requests.Unit;

import java.util.UUID;

public record FollowProfileCommand(
        UUID followerId,
        UUID followeeId
) implements ICommand<Unit>, InternalCommand { }

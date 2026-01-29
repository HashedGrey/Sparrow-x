package com.sparrowx.profile.features.profile.updateprofile;

import buildingblocks.core.event.InternalCommand;
import buildingblocks.mediator.abstractions.commands.ICommand;
import buildingblocks.mediator.abstractions.requests.Unit;
import com.sparrowx.profile.valueobjects.AvatarUrl;
import com.sparrowx.profile.valueobjects.Bio;
import com.sparrowx.profile.valueobjects.ProfileId;
import org.apache.catalina.User;


public record UpdateProfileCassandraCommand(
        ProfileId id,
        User user,
        Bio bio,
        AvatarUrl avatarUrl,
        boolean isDeleted
) implements ICommand<Unit>, InternalCommand {
}

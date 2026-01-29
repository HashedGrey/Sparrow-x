package com.sparrowx.profile.features.profile.createprofile;

import buildingblocks.core.event.InternalCommand;
import buildingblocks.mediator.abstractions.commands.ICommand;
import buildingblocks.mediator.abstractions.requests.Unit;
import com.sparrowx.profile.valueobjects.Email;
import com.sparrowx.profile.valueobjects.FullName;
import com.sparrowx.profile.valueobjects.ProfileId;
import com.sparrowx.profile.valueobjects.UserName;
import java.time.Instant;

public record CreateProfileCassandraCommand(

        ProfileId profileId,
        UserName userName,
        FullName fullName,
        Email email,
        String avatarUrl,
        Instant joinedAt

) implements ICommand<Unit>, InternalCommand {

}




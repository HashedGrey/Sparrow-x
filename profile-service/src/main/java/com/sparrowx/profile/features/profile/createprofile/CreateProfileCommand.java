package com.sparrowx.profile.features.profile.createprofile;

import buildingblocks.core.event.InternalCommand;
import buildingblocks.mediator.abstractions.commands.ICommand;
import com.sparrowx.profile.dtos.ProfileDto;


public record CreateProfileCommand(
        String userName,
        String fullName,
        String email
) implements ICommand<ProfileDto>, InternalCommand { }

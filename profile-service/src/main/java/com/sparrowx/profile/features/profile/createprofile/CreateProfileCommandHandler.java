package com.sparrowx.profile.features.profile.createprofile;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import com.sparrowx.profile.data.postgres.entities.ProfileEntity;
import com.sparrowx.profile.data.postgres.repositories.ProfileRepository;
import com.sparrowx.profile.dtos.ProfileDto;
import com.sparrowx.profile.exceptions.ProfileAlreadyExistsException;
import com.sparrowx.profile.mappers.ProfileMapper;
import com.sparrowx.profile.models.Profile;
import com.sparrowx.profile.valueobjects.*;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CreateProfileCommandHandler implements ICommandHandler<CreateProfileCommand, ProfileDto> {

    private final ProfileRepository profileRepository;
    Instant createdAt = Instant.now();
    AvatarUrl avatarUrl;

    public CreateProfileCommandHandler(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileDto handle(CreateProfileCommand command) {

        profileRepository.findByUserNameAndIsDeletedFalse(new UserName(command.userName()))
        .ifPresent(profile -> {throw new ProfileAlreadyExistsException();});


        Profile profile = Profile.create(
                new UserName(command.userName()),
                new FullName(command.fullName()),
                new Email(command.email()),
                avatarUrl,
                createdAt
        );

        ProfileEntity profileEntity = ProfileMapper.toProfileEntity(profile);

        ProfileEntity createdProfile = profileRepository.save(profileEntity);

        return ProfileMapper.toProfileDto(createdProfile);
    }
}

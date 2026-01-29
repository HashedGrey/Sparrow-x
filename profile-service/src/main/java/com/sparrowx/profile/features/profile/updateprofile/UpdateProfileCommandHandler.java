//package com.sparrowx.profile.features.profile.updateprofile;
//
//import buildingblocks.mediator.abstractions.commands.ICommandHandler;
//import com.sparrowx.profile.data.postgres.entities.ProfileEntity;
//import com.sparrowx.profile.data.postgres.repositories.ProfileRepository;
//import com.sparrowx.profile.dtos.ProfileDto;
//import com.sparrowx.profile.exceptions.ProfileNotFoundException;
//import com.sparrowx.profile.mappers.ProfileMapper;
//import com.sparrowx.profile.models.Profile;
//import com.sparrowx.profile.valueobjects.*;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UpdateProfileCommandHandler implements ICommandHandler<UpdateProfileCommand, ProfileDto> {
//
//    private final ProfileRepository profileRepository;
//
//    public UpdateProfileCommandHandler(ProfileRepository profileRepository) {
//        this.profileRepository = profileRepository;
//    }
//
//    @Override
//    public ProfileDto handle(UpdateProfileCommand command) {
//
//        ProfileEntity existingProfile = profileRepository.findByIdAndIsDeletedFalse(command.id())
//                .orElseThrow(() -> new ProfileNotFoundException("Profile" + command.id() + " not found"));
//
//        Profile profile = ProfileMapper.toProfileAggregate(existingProfile);
//
//        profile.update(
//                new UserId(existingProfile.getId()),
//                new Name(command.name()),
//                //new AvatarUrl(command.avatarUrl()),
//                command.status(),
//                command.isDeleted()
//        );
//
//        // Map back to entity
//        ProfileEntity updatedEntity = ProfileMapper.toProfileEntity(profile);
//
//        // Save to Postgres
//        ProfileEntity savedEntity = profileRepository.save(updatedEntity);
//
//        // Return DTO
//        return ProfileMapper.toProfileDto(savedEntity);
//    }
//}

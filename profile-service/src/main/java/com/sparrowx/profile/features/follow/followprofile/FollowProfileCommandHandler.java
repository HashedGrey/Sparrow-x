//package com.sparrowx.profile.features.follow.followprofile;
//
//import buildingblocks.mediator.abstractions.commands.ICommandHandler;
//import buildingblocks.mediator.abstractions.requests.Unit;
//import com.sparrowx.profile.data.postgres.entities.ProfileEntity;
//import com.sparrowx.profile.data.postgres.repositories.ProfileRepository;
//import com.sparrowx.profile.events.ProfileFollowedDomainEvent;
//import com.sparrowx.profile.exceptions.ProfileNotFoundException;
//import com.sparrowx.profile.mappers.ProfileMapper;
//import com.sparrowx.profile.models.Profile;
//import com.sparrowx.profile.valueobjects.ProfileId;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//
//@Service
//public class FollowProfileCommandHandler implements ICommandHandler<FollowProfileCommand, Unit> {
//
//    private final ProfileRepository profileRepository;
//    private final ApplicationEventPublisher eventPublisher;
//
//    public FollowProfileCommandHandler(ProfileRepository profileRepository,
//                                       ApplicationEventPublisher eventPublisher) {
//        this.profileRepository = profileRepository;
//        this.eventPublisher = eventPublisher;
//    }
//
//    @Override
//    public Unit handle(FollowProfileCommand command) {
//        // 1. Fetch follower
//        ProfileEntity followerEntity = profileRepository.findByIdAndIsDeletedFalse(command.followerId())
//                .orElseThrow(()-> new ProfileNotFoundException(command.followerId().toString()));
//
//        // 2. Fetch followee
//        ProfileEntity followeeEntity = profileRepository.findByIdAndIsDeletedFalse(command.followeeId())
//                .orElseThrow(()-> new ProfileNotFoundException(command.followerId().toString()));
//
//        // 3. Rehydrate aggregates
//        Profile follower = ProfileMapper.toProfileAggregate(followerEntity);
//        Profile followee = ProfileMapper.toProfileAggregate(followeeEntity);
//
//        // 4. Apply domain logic
//        follower.follow(new ProfileId(followee.getId()));
//
//        // 5. Map back to persistence
//        ProfileEntity updatedFollower = ProfileMapper.toProfileEntity(follower);
//        profileRepository.save(updatedFollower);
//
//        // 6. Emit domain event
//        eventPublisher.publishEvent(new ProfileFollowedDomainEvent(
//                follower.getId(),
//                followee.getId()
//        ));
//
//        return Unit.VALUE;
//    }
//}

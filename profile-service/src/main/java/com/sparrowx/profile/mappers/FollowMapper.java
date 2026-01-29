//package com.sparrowx.profile.mappers;
//
//import com.sparrowx.profile.dtos.FollowDto;
//import com.sparrowx.profile.entity.FollowEntity;
//import com.sparrowx.profile.models.Follow;
//import com.sparrowx.profile.valueobjects.FollowId;
//import com.sparrowx.profile.valueobjects.ProfileId;
//import org.springframework.stereotype.Component;
//
//@Component
//public class FollowMapper {
//
//    public FollowDto toDto(Follow follow) {
//        return new FollowDto(
//                follow.getId().getValue(),
//                follow.getFollowerId().getValue(),
//                follow.getFolloweeId().getValue(),
//                follow.getFollowedAt() != null ? follow.getFollowedAt().getTime() : 0
//        );
//    }
//
////    public Follow toDomain(FollowEntity entity) {
////        return new Follow(
////                new FollowId(entity.getId()),
////                new ProfileId(entity.getFollowerId()),
////                new ProfileId(entity.getFolloweeId()),
////                entity.getFollowedAt()
////        );
////    }
//
//    public FollowEntity toEntity(Follow follow) {
//        FollowEntity entity = new FollowEntity();
//        entity.setId(follow.getId().getValue());
//        entity.setFollowerId(follow.getFollowerId().getValue());
//        entity.setFolloweeId(follow.getFolloweeId().getValue());
//        entity.setFollowedAt(follow.getFollowedAt());
//        return entity;
//    }
//}

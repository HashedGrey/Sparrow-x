package com.sparrowx.profile.models;

import buildingblocks.core.model.AggregateRoot;
import com.sparrowx.profile.valueobjects.FollowId;
import com.sparrowx.profile.valueobjects.ProfileId;
import com.sparrowx.profile.features.createfollow.FollowCreatedDomainEvent;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter(AccessLevel.PRIVATE)
public class Follow extends AggregateRoot {

    ProfileId followerId;
    ProfileId followeeId;
    LocalDateTime followedAt;

    public Follow(FollowId id,
                  ProfileId followerId,
                  ProfileId followeeId,
                  LocalDateTime followedAt,
                  Instant createdAt,
                  Long createdBy,
                  Instant lastModified,
                  Long lastModifiedBy,
                  Long version,
                  boolean isDeleted) {
        this.id = id;
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.followedAt = followedAt;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.lastModified = lastModified;
        this.lastModifiedBy = lastModifiedBy;
        this.version = version;
        this.isDeleted = isDeleted;
    }

    public Follow(FollowId id,
                  ProfileId followerId,
                  ProfileId followeeId,
                  LocalDateTime followedAt) {
        this.id = id;
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.followedAt = followedAt;
    }

    public static Follow create(FollowId id,
                                ProfileId followerId,
                                ProfileId followeeId,
                                LocalDateTime followedAt) {
        var follow = new Follow(id, followerId, followeeId, followedAt);

        follow.addDomainEvent(new FollowCreatedDomainEvent(
                follow.id.getValue(),
                follow.followerId.getValue(),
                follow.followeeId.getValue(),
                follow.followedAt
        ));

        return follow;
    }
}

package com.sparrowx.profile.models;

import buildingblocks.core.model.AggregateRoot;
import com.sparrowx.profile.valueobjects.*;
import com.sparrowx.profile.features.profile.createprofile.ProfileCreatedDomainEvent;
import lombok.*;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter(AccessLevel.PRIVATE)
public class Profile extends AggregateRoot {
    UserName userName;
    FullName fullName;
    Email email;
    ProfileId profileId;
    static Instant createdAt;
    AvatarUrl avatarUrl;

    public Profile(ProfileId profileId, UserName userName,
                   FullName fullName, Email email,
                   AvatarUrl avatarUrl, Instant createdAt) {
        this.profileId = profileId;
        this.userName = userName;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public static Profile create(UserName userName,
                                 FullName fullName,
                                 Email email,
                                 AvatarUrl avatarUrl,
                                 Instant createdAt) {
        ProfileId profileId = ProfileId.newId();

        var profile = new Profile(profileId, userName, fullName, email, avatarUrl, createdAt);

        profile.addDomainEvent(new ProfileCreatedDomainEvent(
                profileId,
                userName,
                fullName,
                email,
                avatarUrl,
                createdAt
        ));

        return profile;
    }



    public void update( UserName userName, AvatarUrl avatarUrl, boolean isDeleted) {
        this.userName = userName;
        this.isDeleted = isDeleted;
    }


}

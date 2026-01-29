package com.sparrowx.profile.mappers;

import com.sparrowx.profile.data.cassandra.tables.ProfileTable;
import com.sparrowx.profile.dtos.ProfileDto;
import com.sparrowx.profile.data.postgres.entities.ProfileEntity;
import com.sparrowx.profile.features.profile.createprofile.CreateProfileCassandraCommand;
import com.sparrowx.profile.features.profile.createprofile.CreateProfileCommand;
import com.sparrowx.profile.features.profile.createprofile.CreateProfileRequestDto;
import com.sparrowx.profile.features.profile.updateprofile.UpdateProfileCassandraCommand;
import com.sparrowx.profile.models.Profile;
import com.sparrowx.profile.valueobjects.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProfileMapper {

    public static ProfileEntity toProfileEntity(Profile profile) {
        return new ProfileEntity(
                profile.getUserName(),
                profile.getFullName(),
                profile.getEmail(),
                profile.getAva
        );
    }



    public static ProfileDto toProfileDto(ProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProfileDto(
                entity.getId(),
                entity.getUserName() != null ? entity.getUserName() : null,
                entity.getEmail() != null ? entity.getEmail().getValue() : null,
                entity.getName(),
                entity.getAvatarUrl()
        );
    }

    public static ProfileTable toProfileTable(CreateProfileCassandraCommand command) {
        return ProfileTable.builder()
                .id(command.profileId().value())
                .userName(command.userName().getUserName())
                .fullName(command.fullName().getFullName())
                .email(command.email().getEmail())
                .avatarUrl(command.avatarUrl())
                .build();
    }

    public static ProfileTable toProfileTable(UUID id, UpdateProfileCassandraCommand command) {
        ProfileTable table = new ProfileTable();
        table.setId(id);
        table.setName(command.name());
        table.setAvatarUrl(command.avatarUrl());
        table.setStatus(command.status());
        table.setDeleted(command.isDeleted());
        return table;
    }

    public static CreateProfileCommand toCreateProfileCommand(CreateProfileRequestDto requestDto) {
        return new CreateProfileCommand(
                requestDto.userName(),
                requestDto.fullName(),
                requestDto.email()
        );
    }


}

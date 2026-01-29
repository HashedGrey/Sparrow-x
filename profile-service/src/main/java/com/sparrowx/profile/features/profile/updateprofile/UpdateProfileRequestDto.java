package com.sparrowx.profile.features.profile.updateprofile;

public record UpdateProfileRequestDto(
        String name,
        String bio,
        String avatarUrl,
        boolean isDeleted
) {
}


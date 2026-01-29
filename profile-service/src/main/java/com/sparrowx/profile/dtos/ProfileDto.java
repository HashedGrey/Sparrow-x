package com.sparrowx.profile.dtos;

import com.sparrowx.profile.valueobjects.AvatarUrl;
import com.sparrowx.profile.valueobjects.Email;
import com.sparrowx.profile.valueobjects.UserName;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
        UUID id,
        String userName,
        String fullName,
        String email
) { }

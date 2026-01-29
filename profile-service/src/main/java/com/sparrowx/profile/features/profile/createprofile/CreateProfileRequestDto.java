package com.sparrowx.profile.features.profile.createprofile;

import com.sparrowx.profile.valueobjects.Email;
import com.sparrowx.profile.valueobjects.FullName;
import com.sparrowx.profile.valueobjects.UserName;

public record CreateProfileRequestDto(
        String userName,
        String fullName,
        String email
) { }


package com.sparrowx.profile.features.profile;

import buildingblocks.mediator.abstractions.IMediator;
import com.sparrowx.profile.dtos.ProfileDto;
import com.sparrowx.profile.features.profile.createprofile.CreateProfileCommand;
import com.sparrowx.profile.features.profile.createprofile.CreateProfileRequestDto;
import com.sparrowx.profile.mappers.ProfileMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/profile")
@Tag(name = "profile")
public class ProfileController {

    private final IMediator mediator;

    public ProfileController(IMediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<ProfileDto> createProfile(@RequestBody CreateProfileRequestDto requestDto) {
        CreateProfileCommand command = ProfileMapper.toCreateProfileCommand(requestDto);
        var result = mediator.send(command);
        return ResponseEntity.ok().body(result);
    }
}


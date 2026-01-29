package com.sparrowx.profile.valueobjects;

import buildingblocks.utils.validation.ValidationUtils;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@EqualsAndHashCode
@NoArgsConstructor // Required by JPA
@Getter
public class Bio {
    private String bio;

    public Bio(String value) {
        ValidationUtils.notBeNullOrEmpty(value);

        this.bio = value;
    }
}





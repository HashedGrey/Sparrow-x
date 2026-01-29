package com.sparrowx.profile.valueobjects;

import buildingblocks.utils.validation.ValidationUtils;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@Getter
public class UserName {
    private String userName;

    public UserName(String value) {
        ValidationUtils.notBeNullOrEmpty(value);
        this.userName = value;
    }
}





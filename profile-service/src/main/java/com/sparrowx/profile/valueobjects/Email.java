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
public class Email {
    private String email;

    public Email(String value) {
        ValidationUtils.notBeNullOrEmpty(value);
        ValidationUtils.notBeInvalidEmail(value.toString(), "Email");

        this.email = value;
    }
}




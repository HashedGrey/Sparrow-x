package com.sparrowx.profile.data.postgres.entities;

import buildingblocks.core.model.BaseEntity;
import com.sparrowx.profile.valueobjects.AvatarUrl;
import com.sparrowx.profile.valueobjects.Email;
import com.sparrowx.profile.valueobjects.FullName;
import com.sparrowx.profile.valueobjects.UserName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProfileEntity extends BaseEntity {

    @Embedded
    private Email email;

    @Embedded
    private UserName userName;

    @Column(name = "full_name", nullable = false)
    private FullName fullName;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

    public ProfileEntity(UserName userName, FullName fullName, Email email,  String avatarUrl) {
        this.email = email;
        this.userName = userName;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    public void updateAvatarUrl(String newUrl) {
        this.avatarUrl = newUrl;
    }

}

package com.sparrowx.profile.data.cassandra.tables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Table("profiles")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileTable {

    @PrimaryKey
    private UUID id;

    private String userName;

    private String fullName;

    private String email;

    private String avatarUrl;

    private LocalDate joinDate;

}

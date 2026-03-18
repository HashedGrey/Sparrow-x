package com.sparrowx.tweet.data.cassandra.tables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("tweets_by_id")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetTable {

    @PrimaryKey
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("content")
    private String content;

    @Column("created_at")
    private Instant createdAt;

}
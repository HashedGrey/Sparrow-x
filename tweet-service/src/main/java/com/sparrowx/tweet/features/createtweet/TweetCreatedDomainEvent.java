package com.sparrowx.tweet.features.createtweet;

import buildingblocks.core.events.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class TweetCreatedDomainEvent extends DomainEvent {

    private final UUID tweetId;
    private final UUID userId;
    private final String content;
    private final Instant createdAt;

    public TweetCreatedDomainEvent(
            UUID tweetId,
            UUID userId,
            String content,
            Instant createdAt
    ) {
        super(tweetId);   // aggregateId required by DomainEvent

        this.tweetId = tweetId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

}
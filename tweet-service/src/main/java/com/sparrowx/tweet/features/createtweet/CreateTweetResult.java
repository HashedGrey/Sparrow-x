package com.sparrowx.tweet.features.createtweet;

import lombok.Getter;

import java.time.Instant;

@Getter
public class CreateTweetResult {

    private final String tweetId;
    private final String userId;
    private final String content;
    private final Instant createdAt;

    public CreateTweetResult(
            String tweetId,
            String userId,
            String content,
            Instant createdAt
    ) {
        this.tweetId = tweetId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

}
package com.sparrowx.tweet.valueobjects;

import java.util.UUID;

public record TweetId(UUID value) {

    public TweetId {
        if (value == null ) {
            throw new IllegalArgumentException("TweetId cannot be null or blank");
        }
    }

}
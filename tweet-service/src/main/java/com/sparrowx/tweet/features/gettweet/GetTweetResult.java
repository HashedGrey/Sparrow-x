package com.sparrowx.tweet.features.gettweet;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetTweetResult(
        UUID tweetId,
        UUID userId,
        String content,
        LocalDateTime createdAt
) {
}
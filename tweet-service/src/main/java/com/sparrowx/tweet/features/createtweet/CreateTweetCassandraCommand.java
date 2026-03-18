package com.sparrowx.tweet.features.createtweet;

import buildingblocks.core.events.InternalCommand;

import com.sparrowx.tweet.valueobjects.CreatedAt;
import com.sparrowx.tweet.valueobjects.TweetContent;
import com.sparrowx.tweet.valueobjects.TweetId;
import com.sparrowx.tweet.valueobjects.UserId;

public class CreateTweetCassandraCommand extends InternalCommand<Void> {

    private final TweetId tweetId;
    private final UserId userId;
    private final TweetContent content;
    private final CreatedAt createdAt;

    public CreateTweetCassandraCommand(
            TweetId tweetId,
            UserId userId,
            TweetContent content,
            CreatedAt createdAt
    ) {
        super();
        this.tweetId = tweetId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public TweetId tweetId() {
        return tweetId;
    }

    public UserId userId() {
        return userId;
    }

    public TweetContent content() {
        return content;
    }

    public CreatedAt createdAt() {
        return createdAt;
    }
}
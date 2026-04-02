package com.sparrowx.tweet.models;

import com.sparrowx.tweet.features.createtweet.TweetCreatedDomainEvent;
import com.sparrowx.tweet.valueobjects.CreatedAt;
import com.sparrowx.tweet.valueobjects.TweetContent;
import com.sparrowx.tweet.valueobjects.TweetId;
import com.sparrowx.tweet.valueobjects.UserId;

import buildingblocks.domain.model.AggregateRoot;
import lombok.Getter;

@Getter
public class Tweet extends AggregateRoot<TweetId> {

    private final UserId userId;
    private final TweetContent content;
    private final CreatedAt createdAt;

    private Tweet(
            TweetId tweetId,
            UserId userId,
            TweetContent content,
            CreatedAt createdAt
    ) {
        this.id = tweetId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Tweet create(
            TweetId tweetId,
            UserId userId,
            TweetContent content,
            CreatedAt createdAt
    ) {

        Tweet tweet = new Tweet(
                tweetId,
                userId,
                content,
                createdAt
        );


        tweet.addDomainEvent(
                new TweetCreatedDomainEvent(
                        tweetId.value(),
                        userId.getValue(),
                        content.getValue(),
                        createdAt.getValue()
                )
        );

        return tweet;
    }

}
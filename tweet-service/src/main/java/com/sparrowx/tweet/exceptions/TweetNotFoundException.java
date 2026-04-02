package com.sparrowx.tweet.exceptions;

import buildingblocks.shared.exceptions.NotFoundException;

import java.util.UUID;

public class TweetNotFoundException extends NotFoundException {

    public TweetNotFoundException(UUID tweetId) {
        super("Tweet not found: " + tweetId);
    }
}
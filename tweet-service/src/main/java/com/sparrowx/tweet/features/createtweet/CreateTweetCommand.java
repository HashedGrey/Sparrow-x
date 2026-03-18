package com.sparrowx.tweet.features.createtweet;

import buildingblocks.core.commands.Command;
import com.sparrowx.tweet.valueobjects.TweetContent;
import com.sparrowx.tweet.valueobjects.UserId;

import lombok.Getter;

@Getter
public class CreateTweetCommand implements Command<CreateTweetResult> {

    private final UserId userId;
    private final TweetContent content;

    public CreateTweetCommand(UserId userId, TweetContent content) {
        this.userId = userId;
        this.content = content;
    }

}
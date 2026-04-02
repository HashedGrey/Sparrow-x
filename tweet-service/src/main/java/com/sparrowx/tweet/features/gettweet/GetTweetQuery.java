package com.sparrowx.tweet.features.gettweet;

import buildingblocks.core.queries.Query;
import com.sparrowx.tweet.valueobjects.TweetId;

public record GetTweetQuery(TweetId tweetId) implements Query<GetTweetResult> {
}

package com.sparrowx.tweet.valueobjects;

public record TweetContent(String value) {

    public TweetContent {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tweet content cannot be empty");
        }

        if (value.length() > 280) {
            throw new IllegalArgumentException("Tweet content exceeds 280 characters");
        }
    }

    public String getValue() {
        return value;
    }
}
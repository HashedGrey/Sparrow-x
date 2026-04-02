package com.sparrowx.tweet.exceptions;

import buildingblocks.shared.exceptions.ValidationException;

public class InvalidTweetContentException extends ValidationException {

    public InvalidTweetContentException(String message) {
        super(message);
    }
}
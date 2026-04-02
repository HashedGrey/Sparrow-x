package com.sparrowx.tweet.exceptions;

import buildingblocks.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class TweetServiceException extends AppException {

    public TweetServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, TweetErrorCodes.TWEET_SERVICE_ERROR);
    }

    public TweetServiceException(String message, Exception cause) {
        super(message, cause, TweetErrorCodes.TWEET_SERVICE_ERROR);
    }
}
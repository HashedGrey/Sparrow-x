package com.sparrowx.tweet.features.createtweet;

import buildingblocks.shared.exceptions.ValidationException;

public class CreateTweetCommandValidator {

    public static void validate(CreateTweetCommand command) {

        if (command == null) {
            throw new ValidationException("CreateTweetCommand cannot be null");
        }

        if (command.getUserId() == null) {
            throw new ValidationException("UserId is required");
        }

        if (command.getContent() == null) {
            throw new ValidationException("Tweet content is required");
        }
    }
}
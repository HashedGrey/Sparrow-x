package com.sparrowx.tweet.features.createtweet;

import buildingblocks.core.commands.CommandHandler;

import com.sparrowx.tweet.data.cassandra.repositories.TweetCassandraRepository;
import com.sparrowx.tweet.data.cassandra.tables.TweetTable;
import com.sparrowx.tweet.mappers.TweetMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateTweetCassandraCommandHandler
        implements CommandHandler<CreateTweetCassandraCommand, Void> {

    private static final Logger log =
            LoggerFactory.getLogger(CreateTweetCassandraCommandHandler.class);

    private final TweetCassandraRepository tweetCassandraRepository;

    public CreateTweetCassandraCommandHandler(
            TweetCassandraRepository tweetCassandraRepository
    ) {
        this.tweetCassandraRepository = tweetCassandraRepository;
    }

    @Override
    public Void handle(CreateTweetCassandraCommand command) {

        if (command == null) {
            log.warn("Received null CreateTweetCassandraCommand");
            return null;
        }
        if (tweetCassandraRepository.existsById(command.tweetId().value())) {
            return null; // idempotent skip
        }

        log.debug(
                "Projecting tweet to Cassandra tweetId={}",
                command.tweetId().value()
        );

        TweetTable table = TweetMapper.toTweetTable(command);

        tweetCassandraRepository.save(table);

        log.debug(
                "Tweet projected to Cassandra tweetId={}",
                command.tweetId().value()
        );

        return null;
    }
}
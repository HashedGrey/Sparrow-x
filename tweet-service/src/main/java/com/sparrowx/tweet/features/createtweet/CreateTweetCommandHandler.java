package com.sparrowx.tweet.features.createtweet;

import com.sparrowx.tweet.data.postgres.entities.TweetEntity;
import com.sparrowx.tweet.data.postgres.repositories.TweetRepository;
import com.sparrowx.tweet.mappers.TweetMapper;
import com.sparrowx.tweet.models.Tweet;
import com.sparrowx.tweet.valueobjects.CreatedAt;
import com.sparrowx.tweet.valueobjects.TweetId;

import buildingblocks.core.commands.CommandHandler;
import buildingblocks.infrastructure.persistence.UnitOfWork;
import buildingblocks.shared.utils.IdGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateTweetCommandHandler
        implements CommandHandler<CreateTweetCommand, CreateTweetResult> {

    private static final Logger log =
            LoggerFactory.getLogger(CreateTweetCommandHandler.class);

    private final TweetRepository tweetRepository;
    private final UnitOfWork unitOfWork;

    public CreateTweetCommandHandler(
            TweetRepository tweetRepository,
            UnitOfWork unitOfWork
    ) {
        this.tweetRepository = tweetRepository;
        this.unitOfWork = unitOfWork;
    }


    @Override
    public CreateTweetResult handle(CreateTweetCommand command) {

        return unitOfWork.execute(() -> {

            TweetId tweetId = new TweetId(IdGenerator.generate());
            CreatedAt createdAt = CreatedAt.now();

            Tweet tweet = Tweet.create(
                    tweetId,
                    command.getUserId(),
                    command.getContent(),
                    createdAt
            );

            unitOfWork.registerAggregate(tweet);

            TweetEntity entity = TweetMapper.toTweetEntity(tweet);
            tweetRepository.save(entity);

            return new CreateTweetResult(
                    tweetId.value().toString(),
                    command.getUserId().getValue().toString(),
                    command.getContent().getValue(),
                    createdAt.getValue()
            );
        });
    }
}


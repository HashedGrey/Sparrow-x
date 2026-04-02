package com.sparrowx.tweet.features.gettweet;

import buildingblocks.infrastructure.observability.LoggerFactoryUtil;
import buildingblocks.core.queries.QueryHandler;
import com.sparrowx.tweet.data.cassandra.repositories.TweetCassandraRepository;
import com.sparrowx.tweet.data.cassandra.tables.TweetTable;
import com.sparrowx.tweet.exceptions.TweetNotFoundException;
import com.sparrowx.tweet.mappers.TweetMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GetTweetQueryHandler
        implements QueryHandler<GetTweetQuery, GetTweetResult> {

    private final TweetCassandraRepository tweetCassandraRepository;

    public GetTweetQueryHandler(
            TweetCassandraRepository tweetCassandraRepository
    ) {
        this.tweetCassandraRepository = tweetCassandraRepository;
    }

    @Override
    public GetTweetResult handle(GetTweetQuery query) {

        if (query == null) {
            LoggerFactoryUtil.warn(getClass(), "query.handler.invalid GetTweetQuery null");
            throw new IllegalArgumentException("GetTweetQuery must not be null");
        }

        long handlerStart = System.nanoTime();
        UUID tweetId = query.tweetId().value();

        LoggerFactoryUtil.info(
                getClass(),
                "query.handler.start GetTweetQueryHandler tweetId={}",
                tweetId
        );

        try {
            long cassandraStart = System.nanoTime();

            LoggerFactoryUtil.info(
                    getClass(),
                    "query.repository.read TweetCassandraRepository.findById tweetId={}",
                    tweetId
            );

            TweetTable table = tweetCassandraRepository.findById(tweetId)
                    .orElseThrow(() -> {
                        LoggerFactoryUtil.warn(
                                getClass(),
                                "query.repository.not_found TweetCassandraRepository.findById tweetId={}",
                                tweetId
                        );
                        return new TweetNotFoundException(tweetId);
                    });

            long cassandraDurationMs = (System.nanoTime() - cassandraStart) / 1_000_000;

            LoggerFactoryUtil.info(
                    getClass(),
                    "query.repository.complete TweetCassandraRepository.findById in {} ms tweetId={}",
                    cassandraDurationMs,
                    tweetId
            );

            GetTweetResult result = TweetMapper.toGetTweetResult(table);

            long handlerDurationMs = (System.nanoTime() - handlerStart) / 1_000_000;

            LoggerFactoryUtil.info(
                    getClass(),
                    "query.handler.complete GetTweetQueryHandler in {} ms tweetId={}",
                    handlerDurationMs,
                    tweetId
            );

            return result;

        } catch (Exception ex) {
            long handlerDurationMs = (System.nanoTime() - handlerStart) / 1_000_000;

            LoggerFactoryUtil.error(
                    getClass(),
                    "query.handler.failed GetTweetQueryHandler after {} ms tweetId={}",
                    handlerDurationMs,
                    tweetId
            );

            throw ex;
        }
    }
}
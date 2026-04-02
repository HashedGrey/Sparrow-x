package com.sparrowx.tweet.grpc;

import com.sparrowx.tweet.exceptions.TweetServiceException;
import com.sparrowx.tweet.mappers.TweetMapper;
import buildingblocks.core.commands.CommandBus;
import buildingblocks.core.queries.QueryBus;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class TweetServiceGrpcImpl extends TweetServiceGrpc.TweetServiceImplBase {

    private static final Logger log =
            LoggerFactory.getLogger(TweetServiceGrpcImpl.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public TweetServiceGrpcImpl(
            CommandBus commandBus,
            QueryBus queryBus
    ) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    /*
    ============================================================
    Create Tweet
    ============================================================
    */

    @Override
    public void createTweet(
            CreateTweetRequest request,
            StreamObserver<CreateTweetResponse> responseObserver
    ) {

        try {

            log.debug("CreateTweet request userId={}", request.getUserId());

            var command = TweetMapper.toCreateTweetCommand(request);

            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    TweetMapper.toCreateTweetResponse(result)
            );

            responseObserver.onCompleted();

        } catch (TweetServiceException ex) {

            log.error("CreateTweet failed with TweetServiceException", ex);
            throw ex;

        } catch (Exception ex) {

            log.error("CreateTweet failed", ex);
            throw new TweetServiceException("Failed to create tweet", ex);
        }
    }

    /*
    ============================================================
    Get Tweet
    ============================================================
    */

    @Override
    public void getTweet(
            GetTweetRequest request,
            StreamObserver<GetTweetResponse> responseObserver
    ) {

        try {

            var query = TweetMapper.toGetTweetQuery(request);

            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    TweetMapper.toGetTweetResponse(result)
            );

            responseObserver.onCompleted();

        } catch (TweetServiceException ex) {

            log.error("GetTweet failed with TweetServiceException", ex);
            throw ex;

        } catch (Exception ex) {

            log.error("GetTweet failed", ex);
            throw new TweetServiceException("Failed to get tweet", ex);
        }
    }
    /*
    ============================================================
    Get Batch Tweets
    ============================================================
    */

//    @Override
//    public void getBatchTweets(
//            GetBatchTweetsRequest request,
//            StreamObserver<GetBatchTweetsResponse> responseObserver
//    ) {
//
//        try {
//
//            var query = TweetMapper.toGetBatchTweetsQuery(request);
//
//            var result = queryBus.dispatch(query);
//
//            responseObserver.onNext(
//                    TweetMapper.toBatchResponse(result)
//            );
//
//            responseObserver.onCompleted();
//
//        } catch (Exception ex) {
//
//            log.error("GetBatchTweets failed", ex);
//            responseObserver.onError(ex);
//        }
//    }
}
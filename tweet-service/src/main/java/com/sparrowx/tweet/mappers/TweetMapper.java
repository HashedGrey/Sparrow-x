package com.sparrowx.tweet.mappers;

import com.google.protobuf.Timestamp;

import com.sparrowx.tweet.data.cassandra.tables.TweetTable;
import com.sparrowx.tweet.data.postgres.entities.TweetEntity;

import com.sparrowx.tweet.exceptions.InvalidTweetContentException;
import com.sparrowx.tweet.exceptions.TweetServiceException;
import com.sparrowx.tweet.features.createtweet.CreateTweetCommand;
import com.sparrowx.tweet.features.createtweet.CreateTweetCassandraCommand;

import com.sparrowx.tweet.features.createtweet.CreateTweetResult;
import com.sparrowx.tweet.features.gettweet.GetTweetQuery;
import com.sparrowx.tweet.features.gettweet.GetTweetResult;
import com.sparrowx.tweet.features.gettweet.getbatchtweets.GetBatchTweetsQuery;
import com.sparrowx.tweet.models.Tweet;
import com.sparrowx.tweet.valueobjects.TweetContent;
import com.sparrowx.tweet.valueobjects.TweetId;
import com.sparrowx.tweet.valueobjects.UserId;

import com.sparrowx.tweet.grpc.*;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TweetMapper {

    /* =========================================================
       gRPC -> Command
       ========================================================= */

    public static CreateTweetCommand toCreateTweetCommand(CreateTweetRequest request) {

        return new CreateTweetCommand(
                toUserId(request.getUserId()),
                toTweetContent(request.getContent())
        );
    }

    /* =========================================================
       gRPC -> Query
       ========================================================= */

    public static GetTweetQuery toGetTweetQuery(GetTweetRequest request) {
        String tweetId = request.getTweetId();

        if (tweetId == null || tweetId.isBlank()) {
            throw new TweetServiceException("tweetId must not be blank");
        }

        try {
            UUID uuid = UUID.fromString(tweetId);
            return new GetTweetQuery(new TweetId(uuid));
        } catch (IllegalArgumentException ex) {
            throw new TweetServiceException("Invalid tweetId format", ex);
        }
    }

//    public static GetBatchTweetsQuery
//    toGetBatchTweetsQuery(GetBatchTweetsRequest request) {
//
//        return new GetBatchTweetsQuery(
//                request.getTweetIdsList()
//        );
//    }

    /* =========================================================
       DTO -> gRPC Response
       ========================================================= */

    public static CreateTweetResponse toCreateTweetResponse(CreateTweetResult result) {

        return CreateTweetResponse.newBuilder()
                .setTweetId(result.getTweetId())
                .setUserId(result.getUserId())
                .setContent(result.getContent())
                .setCreatedAt(toProtoTimestamp(result.getCreatedAt()
                ))
                .build();
    }

    public static GetTweetResponse toGetTweetResponse(GetTweetResult result) {

        return GetTweetResponse.newBuilder()
                .setTweetId(result.tweetId().toString())
                .setUserId(result.userId().toString())
                .setContent(result.content())
                .setCreatedAt(
                        toProtoTimestamp(result.createdAt().toInstant(ZoneOffset.UTC))
                )
                .build();
    }

    public static GetTweetResult toGetTweetResult(TweetTable table) {

        if (table == null) {
            return null;
        }

        return new GetTweetResult(
                table.getId(),
                table.getUserId(),
                table.getContent(),
                LocalDateTime.ofInstant(table.getCreatedAt(), ZoneOffset.UTC)
        );
    }


//    public static GetBatchTweetsResponse toBatchResponse(BatchTweetsResponseDto dto) {
//
//        return GetBatchTweetsResponse.newBuilder()
//                .addAllTweets(
//                        dto.getTweets()
//                                .stream()
//                                .map(t -> Tweet.newBuilder()
//                                        .setTweetId(t.getTweetId())
//                                        .setUserId(t.getUserId())
//                                        .setContent(t.getContent())
//                                        .setCreatedAt(toProtoTimestamp(t.getCreatedAt()))
//                                        .build())
//                                .collect(Collectors.toList())
//                )
//                .build();
//    }

    /* =========================================================
       Domain -> Postgres Entity (Write Model)
       ========================================================= */

    public static TweetEntity toTweetEntity(Tweet tweet) {

        return new TweetEntity(
                tweet.getId().value(),
                tweet.getUserId(),
                tweet.getContent()
        );
    }



//    public static BatchTweetsResponseDto toBatchTweetsResponseDto(List<TweetTable> tables) {
//
//        List<GetTweetResponseDto> tweets = tables.stream()
//                .map(TweetMapper::toGetTweetResponseDto)
//                .toList();
//
//        return new BatchTweetsResponseDto(tweets);
//    }

    /* =========================================================
       Internal Command -> Cassandra Table
       ========================================================= */

    public static TweetTable toTweetTable(CreateTweetCassandraCommand command) {

        return TweetTable.builder()
                .id(command.tweetId().value())
                .userId(command.userId().getValue())
                .content(command.content().getValue())
                .createdAt(command.createdAt().getValue())
                .build();
    }

    /* =========================================================
       Utility
       ========================================================= */

    public static Timestamp toProtoTimestamp(Instant instant) {

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static UUID toUUID(String id) {
        return UUID.fromString(id);
    }

    public static UserId toUserId(String id) {
        return new UserId(UUID.fromString(id));
    }

    public static TweetContent toTweetContent(String content) {
        return new TweetContent(content);
    }
}
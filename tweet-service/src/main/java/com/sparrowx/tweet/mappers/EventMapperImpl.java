package com.sparrowx.tweet.mappers;

import buildingblocks.contracts.TweetCreated;
import buildingblocks.core.events.DomainEvent;
import buildingblocks.core.events.EventMapper;
import buildingblocks.core.events.IntegrationEvent;
import buildingblocks.core.events.InternalCommand;

import com.sparrowx.tweet.features.createtweet.CreateTweetCassandraCommand;
import com.sparrowx.tweet.features.createtweet.TweetCreatedDomainEvent;
import com.sparrowx.tweet.valueobjects.CreatedAt;
import com.sparrowx.tweet.valueobjects.TweetContent;
import com.sparrowx.tweet.valueobjects.TweetId;
import com.sparrowx.tweet.valueobjects.UserId;

import org.springframework.stereotype.Component;

import java.util.Optional;import java.util.UUID;

@Component
public class EventMapperImpl implements EventMapper {

    /*
    ============================================================
    Domain Event → Integration Event
    Used when publishing events to Kafka or external services
    ============================================================
    */

    @Override
    public IntegrationEvent mapToIntegrationEvent(DomainEvent event) {

        if (event instanceof TweetCreatedDomainEvent e) {

            return new TweetCreated(
                    e.getAggregateId(),
                    e.getUserId(),
                    e.getContent()
            );
        }

        return null;
    }

    /*
    ============================================================
    Domain Event → Internal Command
    Used for projections / internal workflows
    ============================================================
    */

    @Override
    public Optional<InternalCommand<?>> mapToInternalCommand(DomainEvent event) {

        if (event instanceof TweetCreatedDomainEvent e) {

            return Optional.of(
                    new CreateTweetCassandraCommand(
                            new TweetId(e.getTweetId()),
                            new UserId(e.getUserId()),
                            new TweetContent(e.getContent()),
                            new CreatedAt(e.getCreatedAt())
                    )
            );
        }

        return Optional.empty();
    }
}
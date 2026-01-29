package com.sparrowx.profile.listeners;


import buildingblocks.contracts.profile.FollowCreated;
import buildingblocks.rabbitmq.MessageHandler;
import buildingblocks.utils.jsonconverter.JsonConverterUtils;
import org.slf4j.Logger;
import org.slf4j.event.KeyValuePair;
import org.springframework.stereotype.Component;

@Component
public class FollowCreatedListener implements MessageHandler<FollowCreated> {

    private final Logger logger;

    public FollowCreatedListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onMessage(FollowCreated event) {
        logger.info(
                "Processing new follow event: {}",
                new KeyValuePair("follow_created_event", JsonConverterUtils.serializeObject(event))
        );


    }
}

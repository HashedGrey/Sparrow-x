package com.sparrowx.profile.listeners;


import buildingblocks.contracts.profile.ProfileUpdated;
import buildingblocks.rabbitmq.MessageHandler;
import buildingblocks.utils.jsonconverter.JsonConverterUtils;
import org.slf4j.Logger;
import org.slf4j.event.KeyValuePair;
import org.springframework.stereotype.Component;

@Component
public class ProfileUpdatedListener implements MessageHandler<ProfileUpdated> {

    private final Logger logger;

    public ProfileUpdatedListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onMessage(ProfileUpdated event) {
        logger.info(
                "Processing profile update event: {}",
                new KeyValuePair("profile_updated_event", JsonConverterUtils.serializeObject(event))
        );

    }
}

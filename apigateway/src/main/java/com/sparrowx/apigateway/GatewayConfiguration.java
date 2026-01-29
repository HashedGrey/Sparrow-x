package com.sparrowx.apigateway;

import buildingblocks.core.queries.QueryBusConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({
        QueryBusConfiguration.class
})
public class GatewayConfiguration {
}

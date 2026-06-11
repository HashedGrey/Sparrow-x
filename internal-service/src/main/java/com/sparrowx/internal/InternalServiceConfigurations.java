package com.sparrowx.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "sparrowx.internal")
public class InternalServiceConfigurations {

    private Graph graph = new Graph();
    private Defaults defaults = new Defaults();

    @Setter
    @Getter
    public static class Graph {
        private int maxDepth = 5;
        private int maxLimit = 250;
        private int defaultDepth = 1;
        private int defaultLimit = 50;
    }

    @Setter
    @Getter
    public static class Defaults {
        private String serviceName = "internal-service";
    }
}
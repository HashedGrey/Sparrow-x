package com.sparrowx.agentic.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures canonical JSON and Temporal payload conversion.
 */
@Configuration(proxyBeanMethods = false)
public final class SerializationConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .enable(
                        MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
                )
                .enable(
                        SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
                )
                .disable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                )
                .serializationInclusion(
                        JsonInclude.Include.NON_NULL
                )
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataConverter temporalDataConverter(
            ObjectMapper objectMapper
    ) {
        return DefaultDataConverter
                .newDefaultInstance()
                .withPayloadConverterOverrides(
                        new JacksonJsonPayloadConverter(objectMapper)
                );
    }
}
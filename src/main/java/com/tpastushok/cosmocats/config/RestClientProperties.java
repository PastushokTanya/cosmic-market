package com.tpastushok.cosmocats.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.integration.rest-client")
public class RestClientProperties {
    private int connectTimeoutMillis;
    private int readTimeoutMillis;
    private int retries;
    private int timeoutOnFailureMillis;
}

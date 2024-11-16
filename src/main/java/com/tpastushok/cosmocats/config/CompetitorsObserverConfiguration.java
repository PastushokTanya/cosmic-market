package com.tpastushok.cosmocats.config;

import com.tpastushok.cosmocats.service.implementation.CompetitorObserverServiceImpl;
import com.tpastushok.cosmocats.service.inerfaces.CompetitorObserverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CompetitorsObserverConfiguration {

    @Value("${application.competitors-price-observer.url-wildcard}")
    private String competitorsPriceObserverUrlWildcard;

    @Bean
    public CompetitorObserverService competitorsObserverService(WebClient competitorsPriceObserverClient) {
        return new CompetitorObserverServiceImpl(competitorsPriceObserverClient, competitorsPriceObserverUrlWildcard);
    }
}

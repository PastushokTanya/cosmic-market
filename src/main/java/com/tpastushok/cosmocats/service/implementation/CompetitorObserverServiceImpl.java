package com.tpastushok.cosmocats.service.implementation;

import com.tpastushok.cosmocats.dto.competitors.observer.CompetitorsObserverResponseDto;
import com.tpastushok.cosmocats.service.exception.ThirdPartyServiceException;
import com.tpastushok.cosmocats.service.inerfaces.CompetitorObserverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Slf4j
public class CompetitorObserverServiceImpl implements CompetitorObserverService {

    private final WebClient competitorsPriceObserverClient;
    private final String competitorsPriceObserverUrlWildcard;

    public CompetitorObserverServiceImpl(
            WebClient competitorsPriceObserverClient,
            String competitorsPriceObserverUrlWildcard
    ) {
        this.competitorsPriceObserverClient = competitorsPriceObserverClient;
        this.competitorsPriceObserverUrlWildcard = competitorsPriceObserverUrlWildcard;
    }

    @Override
    public CompetitorsObserverResponseDto observeOtherStorePrices(UUID productId) {
        // Build the URL dynamically by replacing the UUID placeholder in the wildcard URL
        String url = competitorsPriceObserverUrlWildcard.replace("{productId}", productId.toString());

        try {
            log.info("Sending request to observe prices for productId: {}", productId);

            CompetitorsObserverResponseDto response = competitorsPriceObserverClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(CompetitorsObserverResponseDto.class)
                    .onErrorMap(WebClientResponseException.class, ex -> {
                        log.error("Error response from third-party service for productId: {} - Status: {} - Body: {}",
                                productId, ex.getStatusCode(), ex.getResponseBodyAsString());
                        return new ThirdPartyServiceException("Error retrieving data from third-party service", ex);
                    })
                    .onErrorMap(Exception.class, ex -> {
                        log.error("Unexpected error occurred while observing prices for productId: {}", productId, ex);
                        return new ThirdPartyServiceException("Unexpected error occurred while observing other store prices", ex);
                    })
                    .block();

            log.info("Successfully retrieved prices for productId: {}", productId);
            return response;

        } catch (ThirdPartyServiceException ex) {
            log.error("Failed to observe prices for productId: {}", productId, ex);
            throw ex;
        }
    }
}

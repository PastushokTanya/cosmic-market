package com.tpastushok.cosmocats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@EnableRetry
public class RestClientConfiguration {

    private final RestClientProperties props;

    public RestClientConfiguration(RestClientProperties props) {
        this.props = props;
    }

    @Bean
    public WebClient competitorsPriceObserverClient() {
        // Configure WebClient with connect and read timeouts
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(props.getReadTimeoutMillis()))
                .option(
                        io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        props.getConnectTimeoutMillis()
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter()) // Retry filter applied
                .build();
    }

    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.just(clientRequest)
                    .retryWhen(
                            Retry.backoff(
                                            props.getRetries(),
                                            Duration.ofMillis(props.getTimeoutOnFailureMillis())
                                    )
                                    .maxBackoff(Duration.ofMillis(props.getTimeoutOnFailureMillis()))
                    );
        });
    }
}

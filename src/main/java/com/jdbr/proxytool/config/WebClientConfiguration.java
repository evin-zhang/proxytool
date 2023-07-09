package com.jdbr.proxytool.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Slf4j
@Configuration
public class WebClientConfiguration {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter(logRequest());
    }
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // Log the request details if needed
            log.debug("Request:{}" , clientRequest.method() + "," + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}

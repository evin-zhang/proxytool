package com.jdbr.proxytool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.extra.processor.TopicProcessor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import com.jdbr.proxytool.config.ProxyConfig;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;


@Component
public class ProxyGatewayServiceImpl {
    private static final int CACHE_EXPIRATION_MINUTES = 10;
    private final Cache<String, Object> cache;
    private final TopicProcessor<String> cacheInvalidationProcessor;
    private final ProxyConfig proxyConfig;
    private final WebClient webClient;

    @Autowired
    public ProxyGatewayServiceImpl(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(CACHE_EXPIRATION_MINUTES))
                .build();
        this.cacheInvalidationProcessor = TopicProcessor.<String>builder().name("cache-invalidation-processor").build();
        this.webClient = WebClient.builder()
                .filter(logRequest())
                .build();
    }
    public Mono<ResponseEntity<Object>> proxyRequest(String apiPath, HttpMethod httpMethod, String requestBody, String cacheHeaderValue) {
        String requestKey = generateRequestKey(apiPath, httpMethod, requestBody);
        boolean shouldCache = shouldCacheResponse(cacheHeaderValue);

        // Check cache
        if (shouldCache && cache.getIfPresent(requestKey) != null) {
            return Mono.just(ResponseEntity.ok().body(cache.getIfPresent(requestKey)));
        }

        // Forward request to target system and get response
        Mono<ResponseEntity<Object>> responseMono = forwardRequestToTargetSystem(apiPath, httpMethod, requestBody);

        // Cache response if necessary
        if (shouldCache) {
            responseMono = responseMono.doOnSuccess(response -> cache.put(requestKey, response.getBody()));
        }

        return responseMono;
    }

    private String generateRequestKey(String apiPath, HttpMethod httpMethod, String requestBody) {
        String key;
        if (httpMethod == HttpMethod.GET) {
            key = apiPath.hashCode() + "";
        } else {
            String payloadHash = Integer.toString(apiPath.hashCode()) + "_" + Integer.toString(requestBody.hashCode());
            key = apiPath.hashCode() + "_" + payloadHash;
        }
        return key;
    }

  
    private boolean shouldCacheResponse(String cacheHeaderValue) {
        return !"N".equalsIgnoreCase(cacheHeaderValue);
    }
    public Mono<ResponseEntity<Object>> forwardRequestToTargetSystem(String apiPath, HttpMethod httpMethod, String requestBody) {
        String targetSystemName = extractTargetSystemName(apiPath);
        String targetUrl = getTargetUrl(targetSystemName) + apiPath;

        return webClient.method(httpMethod)
                .uri(targetUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .toEntity(Object.class);
    }

    private String extractTargetSystemName(String apiPath) {
        String[] pathSegments = apiPath.split("/");
        if (pathSegments.length >= 2) {
            return pathSegments[1];
        }
        return "";
    }

    private String getTargetUrl(String targetSystemName) {
        return proxyConfig.getRoutes().get(targetSystemName) ==null?"":proxyConfig.getRoutes().get(targetSystemName) ;
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // Log the request details if needed
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}

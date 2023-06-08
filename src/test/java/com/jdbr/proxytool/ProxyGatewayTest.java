package com.jdbr.proxytool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProxyGatewayTest {

    @Mock
    private ProxyConfig proxyConfig;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private ProxyGateway proxyGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void proxyRequest_WithCachingEnabled_ReturnsCachedResponse() {
        // Arrange
        String apiPath = "/customer/query-customer-name";
        HttpMethod httpMethod = HttpMethod.GET;
        String requestBody = "";
        String cacheHeaderValue = "Y";
        String requestKey = apiPath.hashCode() + "";
        Object cachedResponse = new Object();

        // Mock cache
        Cache<String, Object> cache = mock(Cache.class);
        when(cache.getIfPresent(requestKey)).thenReturn(cachedResponse);
        proxyGateway.setCache(cache);

        // Act
        Mono<ResponseEntity<Object>> responseMono = proxyGateway.proxyRequest(apiPath, httpMethod, requestBody, cacheHeaderValue);

        // Assert
        ResponseEntity<Object> response = responseMono.block();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedResponse, response.getBody());
        verify(cache, times(1)).getIfPresent(requestKey);
        verify(webClient, never()).method(any(HttpMethod.class));
    }

    @Test
    void proxyRequest_WithCachingDisabled_ForwardsRequestToTargetSystem() {
        // Arrange
        String apiPath = "/customer/query-customer-name";
        HttpMethod httpMethod = HttpMethod.GET;
        String requestBody = "";
        String cacheHeaderValue = "N";
        String requestKey = apiPath.hashCode() + "";
        String targetSystemName = "customer";
        String targetUrl = "https://example.com/customer/query-customer-name";

        // Mock cache
        Cache<String, Object> cache = mock(Cache.class);
        when(cache.getIfPresent(requestKey)).thenReturn(null);
        proxyGateway.setCache(cache);

        // Mock proxyConfig
        Map<String, String> routes = new HashMap<>();
        routes.put(targetSystemName, targetUrl);
        when(proxyConfig.getRoutes()).thenReturn(routes);

        // Mock webClient
        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(webClient.method(httpMethod)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(targetUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(BodyInserters.fromValue(requestBody))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(Object.class)).thenReturn(Mono.just(ResponseEntity.ok().body("Response")));

        // Act
        Mono<ResponseEntity<Object>> responseMono = proxyGateway.proxyRequest(apiPath, httpMethod, requestBody, cacheHeaderValue);

        // Assert
        ResponseEntity<Object> response = responseMono.block();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Response", response.getBody());
        verify(cache, times(1)).getIfPresent(requestKey);
        verify(webClient, times(1)).method(httpMethod);
        verify(webClient, times(1)).uri(targetUrl);
        verify(requestHeadersSpec, times(1)).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodyUriSpec, times(1)).body(BodyInserters.fromValue(requestBody));
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).toEntity(Object.class);
    }
}


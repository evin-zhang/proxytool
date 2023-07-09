package com.jdbr.proxytool.services;
import com.jdbr.proxytool.config.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

import com.jdbr.proxytool.config.ProxyConfig;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Component
@Slf4j
public class ProxyGatewayServiceImpl implements ProxyGatewayService {
    private static final int CACHE_EXPIRATION_MINUTES = 10;

    private static final String USE_PROXY = "Y";
    private final Cache<String, String> cache;
    private final ProxyConfig proxyConfig;
    private final WebClient webClient;

    @Autowired
    public ProxyGatewayServiceImpl(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(CACHE_EXPIRATION_MINUTES))
                .build();
        this.webClient = createWebClient();
    }

    private WebClient createWebClient() {
        return WebClient.builder()
                .filter(logRequest())
                .build();
    }

    public Mono<ResponseEntity<String>> proxyRequest(String apiPath, HttpHeaders requestHeaders, MultiValueMap<String, String> params, String targetSystemName, HttpMethod httpMethod, String requestBody, String cacheHeaderValue) {
        String requestKey = generateRequestKey(apiPath, httpMethod, requestBody);
        boolean shouldCache = shouldCacheResponse(cacheHeaderValue);

        // Check cache
        if (shouldCache && cache.getIfPresent(requestKey) != null) {
            return Mono.just(ResponseEntity.ok().body(cache.getIfPresent(requestKey)));
        }

        // Forward request to target system and get response
        Mono<ResponseEntity<String>> responseMono = forwardRequestToTargetSystem(apiPath, targetSystemName, httpMethod, requestBody, requestHeaders, params);

        // Cache response if necessary
        if (shouldCache) {
            responseMono = responseMono.doOnSuccess(response -> cache.put(requestKey, response.getBody()));
        }

        return responseMono;
    }

    private String generateRequestKey(String apiPath, HttpMethod httpMethod, String requestBody) {
        String key;
        if (httpMethod == HttpMethod.POST) {
            String payloadHash = apiPath.hashCode() + "_" + requestBody.hashCode();
            key = apiPath.hashCode() + "_" + payloadHash;
        } else {
            key = String.valueOf(apiPath.hashCode());


        }
        return key;
    }


    private boolean shouldCacheResponse(String cacheHeaderValue) {
        return !"N".equalsIgnoreCase(cacheHeaderValue);
    }

    public Mono<ResponseEntity<String>> forwardRequestToTargetSystem(String apiPath, String targetSystemName, HttpMethod httpMethod, String requestBody, HttpHeaders httpHeaders, MultiValueMap<String, String> params) {

        Target target = getTargetUrl(targetSystemName);
        if (!Objects.isNull(target)) {
            String targetUrl = UriComponentsBuilder.fromUriString(target.getHost() + apiPath).queryParams(params).toUriString();
            String needProxy = target.getNeedProxy();
            if (USE_PROXY.equals(needProxy)) {
                final ReactorClientHttpConnector connector =
                        new ReactorClientHttpConnector(HttpClient.create()
                                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host(target.getProxyHost()).port(Integer.parseInt(target.getProxyPort()))));
                WebClient.RequestBodySpec requestBodySpec = WebClient.builder().filter(logRequest()).clientConnector(connector).build().method(httpMethod).uri(targetUrl).acceptCharset(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_XML).headers(headers -> headers.addAll(httpHeaders));
                if (HttpMethod.POST.equals(httpMethod)) {
                    return requestBodySpec.body(BodyInserters.fromValue(requestBody)).exchangeToMono(response -> response.toEntity(String.class));
                } else {
                    return requestBodySpec.exchangeToMono(response -> response.toEntity(String.class));
                }
            } else {
                WebClient.RequestBodySpec requestBodySpec = webClient.method(httpMethod).uri(targetUrl).acceptCharset(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_XML).headers(headers -> headers.addAll(httpHeaders));
                if (HttpMethod.POST.equals(httpMethod)) {
                    return requestBodySpec.body(BodyInserters.fromValue(requestBody)).exchangeToMono(response -> response.toEntity(String.class));
                } else {
                    return requestBodySpec.exchangeToMono(response -> response.toEntity(String.class));
                }
            }
        } else {
            return null;
        }

    }



    private Target getTargetUrl(String targetSystemName) {
        return proxyConfig.getRoutes().get(targetSystemName);
    }
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // Log the request details if needed
            log.debug("Request:{}" , clientRequest.method() + " " + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}

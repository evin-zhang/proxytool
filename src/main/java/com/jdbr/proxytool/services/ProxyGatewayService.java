package com.jdbr.proxytool.services;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

public interface ProxyGatewayService {
    Mono<ResponseEntity<String>> proxyRequest(String apiPath, HttpHeaders requestHeaders,
                                              MultiValueMap<String,String> params, String
                                              targetSystemName, HttpMethod httpMethod, String
                                              requestBody, String cacheHeaderValue);
}

package com.jdbr.proxytool.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public interface ProxyGatewayService {
    Mono<ResponseEntity<String>> proxyRequest(String apiPath, ServerRequest request,String
                                              targetSystemName, String
                                              requestBody);
}

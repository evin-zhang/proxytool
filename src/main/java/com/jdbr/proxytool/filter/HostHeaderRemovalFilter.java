package com.jdbr.proxytool.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class HostHeaderRemovalFilter  implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest modifiedRequest = request.mutate().headers(httpHeaders -> httpHeaders.remove("Host")).build();
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
}

package com.jdbr.proxytool.controller;
import com.jdbr.proxytool.services.ProxyGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
@Slf4j
@Configuration
public class RouterConfig {
    private final ProxyGatewayService proxyGatewayService;
    public RouterConfig(ProxyGatewayService proxyGatewayService){
        this.proxyGatewayService = proxyGatewayService;
    }

    private Mono<ServerResponse> handleProxyRequest(ServerRequest request) {
       final String targetSystemName = request.pathVariable("target-system-name");
       final String requestPath = request.path();
       final String apiPath = requestPath.substring(requestPath.indexOf(targetSystemName) + targetSystemName.length());
        return request.bodyToMono(String.class)
                .flatMap(requestBody ->
                        proxyGatewayService.proxyRequest(apiPath, request, targetSystemName, requestBody)
                )
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                .GET("/{target-system-name}/**", this::handleProxyRequest)
                .POST("/{target-system-name}/**", this::handleProxyRequest)
                .build();
    }
}

package com.jdbr.proxytool.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.jdbr.proxytool.services.ProxyGatewayServiceImpl;

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private final ProxyGatewayServiceImpl proxyGatewayServiceImpl;

    @Autowired
    public ProxyController(ProxyGatewayServiceImpl proxyGatewayServiceImpl) {
        this.proxyGatewayServiceImpl = proxyGatewayServiceImpl;
    }

    @GetMapping("/forward")
    public Mono<ResponseEntity<Object>> forwardGetRequest(
            @RequestHeader("x-proxy-tool-api-path") String apiPath,
            @RequestHeader(value = "x-proxy-tool-cache", defaultValue = "Y") String cacheHeaderValue) {
        return proxyGatewayServiceImpl.proxyRequest(apiPath, HttpMethod.GET, null, cacheHeaderValue);
    }

    @PostMapping("/forward")
    public Mono<ResponseEntity<Object>> forwardPostRequest(
            @RequestHeader("x-proxy-tool-api-path") String apiPath,
            @RequestHeader(value = "x-proxy-tool-cache", defaultValue = "Y") String cacheHeaderValue,
            @RequestBody(required = false) String requestBody) {
        return proxyGatewayServiceImpl.proxyRequest(apiPath, HttpMethod.POST, requestBody, cacheHeaderValue);
    }
}

package com.jdbr.proxytool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.jdbr.proxytool.ProxyGateway;

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private final ProxyGateway proxyGateway;

    @Autowired
    public ProxyController(ProxyGateway proxyGateway) {
        this.proxyGateway = proxyGateway;
    }

    @GetMapping("/forward")
    public Mono<ResponseEntity<Object>> forwardGetRequest(
            @RequestHeader("x-proxy-tool-api-path") String apiPath,
            @RequestHeader(value = "x-proxy-tool-cache", defaultValue = "Y") String cacheHeaderValue) {
        return proxyGateway.proxyRequest(apiPath, HttpMethod.GET, null, cacheHeaderValue);
    }

    @PostMapping("/forward")
    public Mono<ResponseEntity<Object>> forwardPostRequest(
            @RequestHeader("x-proxy-tool-api-path") String apiPath,
            @RequestHeader(value = "x-proxy-tool-cache", defaultValue = "Y") String cacheHeaderValue,
            @RequestBody(required = false) String requestBody) {
        return proxyGateway.proxyRequest(apiPath, HttpMethod.POST, requestBody, cacheHeaderValue);
    }
}

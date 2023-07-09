package com.jdbr.proxytool.controllers;

import com.jdbr.proxytool.services.ProxyGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/proxy")
@Slf4j
public class ProxyController {
    private final ProxyGatewayService proxyGatewayService;

    @Autowired
    public ProxyController(ProxyGatewayService proxyGatewayService) {
        this.proxyGatewayService = proxyGatewayService;
    }

    @RequestMapping(value = "/{target-system-name}/**",method={RequestMethod.GET,RequestMethod.POST})
    public Mono<ResponseEntity<String>> proxyRequest(
            @RequestBody(required=false) String requestBody,
            @RequestParam MultiValueMap<String,String> params,
            @PathVariable (name="target-system-name") String targetSystemName,
            HttpMethod httpMethod,
            ServerWebExchange exchange,
            @RequestHeader HttpHeaders requestHeaders,
            @RequestHeader(value = "x-proxy-tool-cache", defaultValue = "Y") String cacheHeaderValue) {
        String requestPath = exchange.getRequest().getPath().value();
        String apiPath = requestPath.substring(requestPath.indexOf(targetSystemName)+targetSystemName.length());
        log.debug(apiPath);
        return proxyGatewayService.proxyRequest(apiPath,requestHeaders,params,targetSystemName,httpMethod,requestBody,cacheHeaderValue);
    }

}

package com.jdbr.proxytool.controller;

import com.jdbr.proxytool.exception.ProxyGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ProxyGatewayException.class)
    public ResponseEntity<String> handleProxyException(ProxyGatewayException proxyGatewayException) {
        // Handle the exception
        log.error(proxyGatewayException.getMessage(), proxyGatewayException.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(proxyGatewayException.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        // Handle the exception
        log.error(exception.getMessage(), exception.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}

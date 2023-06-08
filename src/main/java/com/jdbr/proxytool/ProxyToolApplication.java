package com.jdbr.proxytool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProxyConfig.class)
public class ProxyToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyToolApplication.class, args);
    }
}

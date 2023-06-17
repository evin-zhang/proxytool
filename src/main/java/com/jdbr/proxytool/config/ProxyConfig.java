package com.jdbr.proxytool.config;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "proxy-tool")
public class ProxyConfig {
    private Map<String,Target> routes = new HashMap<>();
}

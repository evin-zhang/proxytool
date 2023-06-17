package com.jdbr.proxytool.config;

import lombok.Data;

@Data
public class Target
{
    private String host;
    private String needProxy;
    private String proxyHost;
    private String proxyPort;
}

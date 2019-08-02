package com.oxide.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ToString
@ConfigurationProperties(ZookeeperConfig.ZK)
public class ZookeeperConfig {

    public static final String ZK = "zkf";

    private boolean enabled;

    private String url;

    private int timeout = 30000;

    private String propertyPrefix;
}

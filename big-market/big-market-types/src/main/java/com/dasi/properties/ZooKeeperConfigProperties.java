package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zookeeper.sdk.config", ignoreInvalidFields = true)
public class ZooKeeperConfigProperties {

    private String connectString;

    private Integer baseSleepTimeMs;

    private Integer maxRetries;

    private Integer sessionTimeoutMs;

    private Integer connectionTimeoutMs;

    private String configPath;

}

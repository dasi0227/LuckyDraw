package com.dasi.config;

import com.dasi.properties.ZooKeeperConfigProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {

    @Bean
    public CuratorFramework curatorFramework(ZooKeeperConfigProperties properties) {

        ExponentialBackoffRetry backoffRetry = new ExponentialBackoffRetry(
                properties.getBaseSleepTimeMs(),
                properties.getMaxRetries());

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(properties.getConnectString())
                .sessionTimeoutMs(properties.getSessionTimeoutMs())
                .retryPolicy(backoffRetry)
                .connectionTimeoutMs(properties.getConnectionTimeoutMs())
                .build();
        curatorFramework.start();
        return curatorFramework;

    }

}

package com.dasi;

import cn.bugstack.middleware.db.router.config.DataSourceAutoConfig;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@SpringBootApplication
@Configurable
@EnableScheduling
@Import(DataSourceAutoConfig.class)
public class LuckyDrawApplication {

    @Resource
    private IRedisService redisService;

    @PostConstruct
    public void startUp() {
        redisService.deleteByPattern("*");
    }

    public static void main(String[] args){
        SpringApplication.run(LuckyDrawApplication.class);
    }

}

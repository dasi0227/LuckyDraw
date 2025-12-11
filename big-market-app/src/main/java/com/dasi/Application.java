package com.dasi;

import cn.bugstack.middleware.db.router.config.DataSourceAutoConfig;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
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
public class Application {

    @Resource
    private IRedisService redisService;

    @Resource
    private IActivityAssemble activityAssemble;

    @Resource
    private IStrategyAssemble strategyAssemble;

    @PostConstruct
    public void startUp() {
        Long activityId = 10001L;
        redisService.deleteByPattern("*");
        activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        strategyAssemble.assembleStrategyByActivityId(activityId);
    }

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}

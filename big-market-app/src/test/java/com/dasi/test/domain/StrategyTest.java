package com.dasi.test.domain;

import com.dasi.domain.strategy.service.armory.IStrategyArmory;
import com.dasi.domain.strategy.service.armory.IStrategyLottery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IStrategyLottery strategyLottery;

    @Test
    public void test_armory() {
        log.info("success: {}", strategyArmory.assembleStrategy(100001L));
    }

    @Test
    public void test_dispatch() {
        log.info("4000 策略配置抽奖：awardId = {}", strategyLottery.doLottery(100001L, "4000"));
        log.info("5000 策略配置抽奖：awardId = {}", strategyLottery.doLottery(100001L, "5000"));
        log.info("6000 策略配置抽奖：awardId = {}", strategyLottery.doLottery(100001L, "6000"));
    }

}

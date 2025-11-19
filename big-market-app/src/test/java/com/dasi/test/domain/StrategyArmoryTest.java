package com.dasi.test.domain;


import com.dasi.domain.strategy.service.armory.IStrategyArmory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyArmoryTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Test
    public void test_assembleLotteryStrategy() {
        strategyArmory.assembleLotteryStrategy(100002L);
    }

    @Test
    public void test_getRandomAwardId() {
        log.info("抽奖结果：奖品 ID = {}", strategyArmory.getRandomAwardId(100002L));
        log.info("抽奖结果：奖品 ID = {}", strategyArmory.getRandomAwardId(100002L));
        log.info("抽奖结果：奖品 ID = {}", strategyArmory.getRandomAwardId(100002L));
        log.info("抽奖结果：奖品 ID = {}", strategyArmory.getRandomAwardId(100002L));
        log.info("抽奖结果：奖品 ID = {}", strategyArmory.getRandomAwardId(100002L));
    }


}

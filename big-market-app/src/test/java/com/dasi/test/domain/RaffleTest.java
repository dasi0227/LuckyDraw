package com.dasi.test.domain;

import com.dasi.domain.strategy.model.entity.RaffleRequestEntity;
import com.dasi.domain.strategy.model.entity.RaffleResponseEntity;
import com.dasi.domain.strategy.service.armory.IStrategyArmory;
import com.dasi.domain.strategy.service.raffle.IRaffle;
import com.dasi.domain.strategy.service.rule.impl.RuleLockFilter;
import com.dasi.domain.strategy.service.rule.impl.RuleWeightFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleTest {

    @Resource
    private RuleWeightFilter ruleWeightFilter;

    @Resource
    private IRaffle raffle;

    @Resource
    private IStrategyArmory strategyArmory;
    @Autowired
    private RuleLockFilter ruleLockFilter;

    @Before
    public void setUp() {
        log.info("success: {}", strategyArmory.assembleLotteryStrategy(100001L));
        log.info("success: {}", strategyArmory.assembleLotteryStrategy(100002L));
        log.info("success: {}", strategyArmory.assembleLotteryStrategy(100003L));

        ReflectionTestUtils.setField(ruleWeightFilter, "userScore", 4500L);
        ReflectionTestUtils.setField(ruleLockFilter, "userRaffleCount", 0L);
    }

    @Test
    public void test_doRaffle() {
        RaffleRequestEntity request = RaffleRequestEntity.builder()
                .userId("user001")
                .strategyId(100001L)
                .build();

        log.info("请求参数：{}", request);
        RaffleResponseEntity response = raffle.doRaffle(request);
        log.info("响应结果：{}", response);
    }


}

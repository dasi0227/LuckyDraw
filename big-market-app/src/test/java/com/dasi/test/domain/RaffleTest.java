package com.dasi.test.domain;

import com.dasi.domain.strategy.model.dto.RaffleContext;
import com.dasi.domain.strategy.model.dto.RaffleResult;
import com.dasi.domain.strategy.service.assemble.IAssemble;
import com.dasi.domain.strategy.service.raffle.IRaffle;
import com.dasi.domain.strategy.service.rule.chain.impl.RuleWeightChain;
import com.dasi.domain.strategy.service.rule.tree.impl.RuleLockTree;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleTest {

    @Resource
    private IRaffle raffle;
    @Resource
    private IAssemble armory;
    @Resource
    private RuleWeightChain ruleWeightChain;
    @Resource
    private RuleLockTree ruleLockTree;
    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("big_market_*");
        armory.assembleStrategy(100006L);
        ReflectionTestUtils.setField(ruleWeightChain, "userScore", 5000L);
    }

    @Test
    public void testRaffle() throws InterruptedException {
        RaffleContext raffleContext = new RaffleContext();
        raffleContext.setUserId("dasi");
        raffleContext.setStrategyId(100006L);
        for (int i = 1; i <= 5; i++) {
            ReflectionTestUtils.setField(ruleLockTree, "userRaffleCount", (long) i);
            log.info("============================== 第 {} 次抽奖 ==============================", i);
            log.info("【抽奖请求】RaffleContext {}", raffleContext);
            RaffleResult raffleResult = raffle.doRaffle(raffleContext);
            log.info("【抽奖结果】RaffleResult = {}", raffleResult);
        }

        new CountDownLatch(1).await();
    }

}

package com.dasi.test.domain;

import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
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
public class StrategyTest {

    @Resource
    private IStrategyLottery raffle;
    @Resource
    private IStrategyAssemble armory;
    @Resource
    private RuleWeightChain ruleWeightChain;
    @Resource
    private RuleLockTree ruleLockTree;
    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("big_market:strategy:*");
    }

    @Test
    public void testStrategy() throws InterruptedException {
        Long strategyId = 1001L;

        // 装配
        armory.assembleStrategy(strategyId);

        // 抽奖
        ReflectionTestUtils.setField(ruleWeightChain, "userScore", 5000L);
        LotteryContext lotteryContext = new LotteryContext();
        lotteryContext.setUserId("wyw");
        lotteryContext.setStrategyId(strategyId);
        for (int i = 1; i <= 100; i++) {
            ReflectionTestUtils.setField(ruleLockTree, "userLotteryCount", (long) i);
            log.info("============================== 第 {} 次抽奖 ==============================", i);
            log.info("【抽奖请求】LotteryContext {}", lotteryContext);
            LotteryResult lotteryResult = raffle.doStrategyLottery(lotteryContext);
            log.info("【抽奖结果】LotteryResult = {}", lotteryResult);
        }

        new CountDownLatch(1).await();
    }

}

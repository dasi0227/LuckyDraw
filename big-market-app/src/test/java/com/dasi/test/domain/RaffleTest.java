package com.dasi.test.domain;

import com.dasi.domain.strategy.model.dto.RaffleRequestDTO;
import com.dasi.domain.strategy.model.dto.RaffleResponseDTO;
import com.dasi.domain.strategy.service.armory.IStrategyArmory;
import com.dasi.domain.strategy.service.raffle.IRaffle;
import com.dasi.domain.strategy.service.rule.chain.IRuleChain;
import com.dasi.domain.strategy.service.rule.chain.RuleChainFactory;
import com.dasi.domain.strategy.service.rule.chain.impl.RuleWeightChain;
import com.dasi.domain.strategy.service.rule.filter.impl.RuleLockFilter;
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
    private IRaffle raffle;

    @Resource
    private IStrategyArmory strategyArmory;
    @Autowired
    private RuleLockFilter ruleLockFilter;
    @Autowired
    private RuleWeightChain ruleWeightChain;
    @Autowired
    private RuleChainFactory ruleChainFactory;

    @Before
    public void setUp() {
        log.info("success: {}", strategyArmory.assembleStrategy(100001L));
        log.info("success: {}", strategyArmory.assembleStrategy(100002L));
        log.info("success: {}", strategyArmory.assembleStrategy(100003L));
        ReflectionTestUtils.setField(ruleWeightChain, "userScore", 5000L);
    }

    @Test
    public void testRuleBlackListChain() {
        IRuleChain ruleChain = ruleChainFactory.getFirstRuleChain(100001L);
        Integer awardId = ruleChain.logic("user001", 100001L);
        log.info("awardId: {}", awardId);
    }

    @Test
    public void testRuleWeightChain() {
        IRuleChain ruleChain = ruleChainFactory.getFirstRuleChain(100001L);
        Integer awardId = ruleChain.logic("dasi", 100001L);
        log.info("awardId: {}", awardId);
    }

    @Test
    public void test_doRaffle() {
        ReflectionTestUtils.setField(ruleLockFilter, "userRaffleCount", 0L);
        RaffleRequestDTO request = RaffleRequestDTO.builder()
                .userId("user001")
                .strategyId(100001L)
                .build();

        log.info("请求参数：{}", request);
        RaffleResponseDTO response = raffle.doRaffle(request);
        log.info("响应结果：{}", response);
    }


}

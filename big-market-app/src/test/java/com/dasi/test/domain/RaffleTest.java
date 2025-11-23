package com.dasi.test.domain;

import com.dasi.domain.strategy.model.dto.RaffleRequest;
import com.dasi.domain.strategy.model.dto.RaffleResponse;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.armory.IArmory;
import com.dasi.domain.strategy.service.raffle.IRaffle;
import com.dasi.domain.strategy.service.rule.chain.impl.RuleWeightChain;
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
    private IStrategyRepository strategyRepository;
    @Resource
    private IArmory armory;
    @Autowired
    private RuleWeightChain ruleWeightChain;

    @Before
    public void setUp() {
        log.info("success: {}", armory.assembleStrategy(100001L));
        log.info("success: {}", armory.assembleStrategy(100006L));

        ReflectionTestUtils.setField(ruleWeightChain, "userScore", 5000L);
    }

    @Test
    public void testRaffle() {
        RaffleRequest raffleRequest = RaffleRequest.builder()
                .userId("dasi")
                .strategyId(100006L)
                .build();
        RaffleResponse raffleResponse = raffle.doRaffle(raffleRequest);
        log.info("\nRaffleRequest = {}\nRaffleResponse = {}", raffleRequest, raffleResponse);
    }

}

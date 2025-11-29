package com.dasi.test.domain;

import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.dto.RaffleResult;
import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("all")
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityTest {

    @Resource
    private ISkuRecharge activityRecharge;

    @Resource
    private IActivityRaffle activityRaffle;

    @Resource
    private IActivityAssemble activityAssemble;

    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("big_market:activity:*");
    }

    @Test
    public void testRecharge() throws InterruptedException {
        Long skuId = 3001L;

        // 装配
        boolean success = activityAssemble.assembleRechargeSkuStockBySkuId(skuId);

        // 充值
        for (int i = 1; i <= 20; i++) {
            log.info("=================== 第 {} 次充值 ===================", i);
            try {
                RechargeContext rechargeContext = new RechargeContext();
                rechargeContext.setUserId("dasi");
                rechargeContext.setSkuId(skuId);
                rechargeContext.setBizId(RandomStringUtils.randomAlphanumeric(12));
                log.info("【充值请求】RechargeContext = {}", rechargeContext);
                RechargeResult rechargeResult = activityRecharge.doRecharge(rechargeContext);
                log.info("【充值结果】RechargeResult = {}", rechargeResult);
            } catch (Exception e) {
                log.warn("【错误栈】", e);
            }
        }
    }

    @Test
    public void testRaffle() throws InterruptedException {
        // 抽奖
        for (int i = 1; i <= 10; i++) {
            log.info("=================== 第 {} 次抽奖 ===================", i);
            try {
                RaffleContext raffleContext = new RaffleContext();
                raffleContext.setUserId("dasi");
                raffleContext.setActivityId(1001L);
                log.info("【抽奖请求】RaffleContext = {}", raffleContext);
                RaffleResult raffleResult = activityRaffle.doActivityRaffle(raffleContext);
                log.info("【抽奖结果】RaffleResult = {}", raffleResult);
            } catch (Exception e) {
                log.warn("【错误栈】", e);
            }
        }
    }

    @Test
    public void testActivity() throws InterruptedException {
        testRecharge();
        testRaffle();
        new CountDownLatch(1).await();
    }

}

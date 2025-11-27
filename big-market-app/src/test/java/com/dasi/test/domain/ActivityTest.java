package com.dasi.test.domain;

import com.dasi.domain.activity.model.dto.SkuRechargeContext;
import com.dasi.domain.activity.model.dto.SkuRechargeResult;
import com.dasi.domain.activity.service.recharge.IActivityRecharge;
import com.dasi.domain.activity.service.stock.IActivityStock;
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

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityTest {

    @Resource
    private IActivityRecharge activityOrder;

    @Resource
    private IActivityStock activityStock;

    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("big_market:activity:*");
    }

    @Test
    public void testActivity() throws InterruptedException {
        Long skuId = 3001L;

        // 装配
        boolean success = activityStock.assembleActivitySkuStock(skuId);

        // 下单
        for (int i = 1; i <= 20; i++) {
            log.info("=================== 第 {} 次下单 ===================", i);
            try {
                SkuRechargeContext skuRechargeContext = new SkuRechargeContext();
                skuRechargeContext.setUserId("dasi");
                skuRechargeContext.setSkuId(skuId);
                skuRechargeContext.setBizId(RandomStringUtils.randomAlphanumeric(12));
                SkuRechargeResult skuRechargeResult = activityOrder.skuRecharge(skuRechargeContext);
                log.info("【下单结果】skuRechargeResult = {}", skuRechargeResult);
            } catch (Exception e) {
                log.warn("【错误原因】info = {}", e.getMessage());
                log.warn("【错误栈】", e);
            }
        }

        new CountDownLatch(1).await();
    }
}

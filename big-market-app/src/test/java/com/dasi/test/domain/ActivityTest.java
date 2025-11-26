package com.dasi.test.domain;

import com.dasi.domain.activity.model.dto.SkuRecharge;
import com.dasi.domain.activity.service.order.IActivityOrder;
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
    private IActivityOrder activityOrder;

    @Resource
    private IActivityStock activityStock;

    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("big_market:activity:*");
    }

    @Test
    public void testOrder() throws InterruptedException {
        Long sku = 2001L;

        // 装配
        boolean success = activityStock.assembleActivitySkuStock(sku);

        // 下单
        for (int i = 1; i <= 20; i++) {
            log.info("=================== 第 {} 次下单 ===================", i);
            try {
                SkuRecharge skuRecharge = new SkuRecharge();
                skuRecharge.setUserId("dasi");
                skuRecharge.setSku(sku);
                skuRecharge.setBizId(RandomStringUtils.randomAlphanumeric(12));
                String orderId = activityOrder.createSkuRechargeOrder(skuRecharge);
                log.info("【下单结果】orderId = {}", orderId);
            } catch (Exception e) {
                log.warn("【错误原因】info = {}", e.getMessage());
            }
        }

        new CountDownLatch(1).await();
    }
}

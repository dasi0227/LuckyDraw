package com.dasi.test.domain;

import com.dasi.domain.activity.model.io.SkuRecharge;
import com.dasi.domain.activity.service.order.IOrder;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityTest {

    @Resource
    private IOrder order;

    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("big_market:activity:*");
    }

    @Test
    public void testOrder() {
        SkuRecharge skuRecharge = new SkuRecharge();
        skuRecharge.setUserId("dasi");
        skuRecharge.setSku(2001L);
        skuRecharge.setBizId("200402270002");
        String orderId = order.createSkuRechargeOrder(skuRecharge);
        log.info("【测试结果】orderId = {}", orderId);
    }
}

package com.dasi.test.domain;

import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivityShoppingCartEntity;
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
        ActivityShoppingCartEntity activityShoppingCartEntity = new ActivityShoppingCartEntity();
        activityShoppingCartEntity.setUserId("dasi");
        activityShoppingCartEntity.setSku(2001L);
        ActivityOrderEntity activityOrderEntity = order.createActivityOrder(activityShoppingCartEntity);
        log.info("【测试结果】activityOrderEntity = {}", activityOrderEntity);
    }
}

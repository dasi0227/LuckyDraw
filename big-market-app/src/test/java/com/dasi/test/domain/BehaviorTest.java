package com.dasi.test.domain;

import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
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
public class BehaviorTest {

    @Resource
    private IBehaviorReward behaviorReact;

    @Resource
    private IActivityAssemble activityAssemble;

    @Resource
    private IStrategyAssemble strategyAssemble;

    @Resource
    private IRedisService redisService;

    @Before
    public void setUp() {
        redisService.deleteByPattern("*");

        Long activityId = 1001L;
        boolean flag1 = activityAssemble.assembleRechargeSkuStockByActivityId(activityId);
        boolean flag2 = strategyAssemble.assembleStrategyByActivityId(activityId);
        if (flag1 && flag2) {
            log.info("测试装配成功");
        }
    }

    @Test
    public void testBehavior() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            BehaviorContext behaviorContext = BehaviorContext.builder()
                    .userId("dasi")
                    .activityId(10001L)
                    .behaviorType(BehaviorType.SIGN)
                    .build();
            BehaviorResult behaviorResult = behaviorReact.doBehaviorReward(behaviorContext);
            log.info("测试订单 IDs：{}", behaviorResult.getOrderIds());
        }

        new CountDownLatch(1).await();
    }

}

package com.dasi.test.domain;

import com.dasi.domain.activity.model.io.DistributeContext;
import com.dasi.domain.activity.model.io.DistributeResult;
import com.dasi.domain.activity.service.distribute.IAwardDistribute;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.domain.task.service.scan.ITaskScan;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("unused")
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardTest {

    @Resource
    private IAwardDistribute awardDistribute;

    @Resource
    private ITaskScan iTaskScan;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Test
    public void testAward() throws InterruptedException {

        DistributeContext distributeContext = DistributeContext.builder()
                .userId("dasi")
                .activityId(1001L)
                .awardId(2001L)
                .awardName("测试奖品2001")
                .orderId(uniqueIdGenerator.nextRaffleOrderId())
                .build();
        DistributeResult distributeResult = awardDistribute.doAwardDistribute(distributeContext);
        log.info("【中奖】{}", distributeResult);

        new CountDownLatch(1).await();
    }

}

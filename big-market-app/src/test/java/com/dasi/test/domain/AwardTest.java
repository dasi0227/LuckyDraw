package com.dasi.test.domain;

import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.task.service.scan.ITaskScan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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

    @Test
    public void testAward() throws InterruptedException {

        DistributeContext distributeContext = DistributeContext.builder()
                .userId("dasi")
                .activityId(1001L)
                .awardId(2001L)
                .awardName("测试奖品2001")
                .strategyId(1001L)
                .orderId(RandomStringUtils.randomNumeric(12))
                .build();
        DistributeResult distributeResult = awardDistribute.doAwardDistribute(distributeContext);
        log.info("【中奖】{}", distributeResult);

        new CountDownLatch(1).await();
    }

}

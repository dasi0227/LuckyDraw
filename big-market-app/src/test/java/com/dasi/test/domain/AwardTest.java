package com.dasi.test.domain;

import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.service.scan.ITaskScan;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

        for (int i = 0; i < 100; i++) {
            RaffleAwardEntity raffleAwardEntity = RaffleAwardEntity.builder()
                        .userId("dasi")
                        .activityId(1001L)
                        .strategyId(1001L)
                        .orderId(RandomStringUtils.randomNumeric(12))
                        .awardId(2001)
                        .awardName("测试奖品")
                        .awardTime(LocalDateTime.now())
                        .awardState(AwardState.CREATED.getCode())
                        .build();
//            awardDistribute.saveRaffleAward(raffleAwardEntity);
            Thread.sleep(500);
        }

        new CountDownLatch(1).await();
    }

}

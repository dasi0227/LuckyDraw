package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.AwardDispatchAggregate;
import com.dasi.domain.award.model.entity.ActivityAccountEntity;
import com.dasi.domain.award.model.entity.UserAwardEntity;
import com.dasi.domain.award.model.type.AwardSource;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.dispatch.IAwardDispatchHandler;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@AwardTypeConfig(awardType = AwardType.RANDOM_ACCOUNT_POINT)
@Component
public class RandomAccountPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(AwardDispatchAggregate awardDispatchAggregate) {

        // 解析值
        String awardValue = awardDispatchAggregate.getAwardEntity().getAwardValue();
        String[] pointRange = awardValue.split(Delimiter.COMMA);
        if (pointRange.length != 2) {
            throw new AppException("随机积分配置错误：awardValue={}" + awardValue);
        }
        int randomPoint = ThreadLocalRandom.current().nextInt(Integer.parseInt(pointRange[0]), Integer.parseInt(pointRange[1]) + 1);

        // 构造对象
        UserAwardEntity userAwardEntity = UserAwardEntity.builder()
                .orderId(awardDispatchAggregate.getOrderId())
                .userId(awardDispatchAggregate.getUserId())
                .activityId(awardDispatchAggregate.getActivityId())
                .awardId(awardDispatchAggregate.getAwardId())
                .awardSource(AwardSource.RAFFLE)
                .awardName(awardDispatchAggregate.getAwardEntity().getAwardName())
                .awardDesc(awardDispatchAggregate.getAwardEntity().getAwardDesc())
                .awardDeadline(null)
                .awardTime(LocalDateTime.now())
                .build();

        ActivityAccountEntity activityAccountEntity = awardDispatchAggregate.getActivityAccountEntity();
        activityAccountEntity.setAccountPoint(randomPoint);

        awardRepository.saveUserAward(activityAccountEntity, awardDispatchAggregate.getActivityAwardEntity(), userAwardEntity);
    }

}

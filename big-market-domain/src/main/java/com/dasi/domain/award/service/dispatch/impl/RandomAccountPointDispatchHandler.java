package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@AwardTypeConfig(awardType = AwardType.RANDOM_ACCOUNT_POINT)
@Component
public class RandomAccountPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(DispatchHandleAggregate dispatchHandleAggregate) {
        String awardValue = dispatchHandleAggregate.getAwardEntity().getAwardValue();
        String[] pointRange = awardValue.split(Delimiter.COMMA);
        if (pointRange.length != 2) {
            throw new AppException("随机积分配置错误：awardValue={}" + awardValue);
        }
        int randomPoint = ThreadLocalRandom.current().nextInt(Integer.parseInt(pointRange[0]), Integer.parseInt(pointRange[1]) + 1);
        dispatchHandleAggregate.setAccountPoint(randomPoint);
        awardRepository.increaseActivityAccountPoint(dispatchHandleAggregate);

        UserAwardEntity userAwardEntity = UserAwardEntity.builder()
                .orderId(dispatchHandleAggregate.getOrderId())
                .userId(dispatchHandleAggregate.getUserId())
                .awardId(dispatchHandleAggregate.getAwardId())
                .activityId(dispatchHandleAggregate.getActivityId())
                .awardSource(AwardSource.RAFFLE)
                .awardName(dispatchHandleAggregate.getAwardEntity().getAwardName())
                .awardDesc(dispatchHandleAggregate.getAwardEntity().getAwardDesc())
                .awardDeadline(null)
                .awardTime(dispatchHandleAggregate.getActivityAwardEntity().getAwardTime())
                .build();
        dispatchHandleAggregate.setUserAwardEntity(userAwardEntity);
        awardRepository.saveUserAward(dispatchHandleAggregate);
    }

}

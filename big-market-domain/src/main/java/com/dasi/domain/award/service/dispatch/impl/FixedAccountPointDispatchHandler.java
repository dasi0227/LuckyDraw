package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.entity.UserAwardEntity;
import com.dasi.domain.award.model.type.AwardSource;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.dispatch.IAwardDispatchHandler;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@AwardTypeConfig(awardType = AwardType.FIXED_ACCOUNT_POINT)
@Component
public class FixedAccountPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(DispatchHandleAggregate dispatchHandleAggregate) {
        String awardValue = dispatchHandleAggregate.getAwardEntity().getAwardValue();
        if (!StringUtils.isNumeric(awardValue)) {
            throw new AppException("固定积分配置错误：awardValue={}" + awardValue);
        }
        int fixedPoint = Integer.parseInt(awardValue);
        dispatchHandleAggregate.setAccountPoint(fixedPoint);
        awardRepository.increaseActivityAccountPoint(dispatchHandleAggregate);

        UserAwardEntity userAwardEntity = UserAwardEntity.builder()
                .orderId(dispatchHandleAggregate.getOrderId())
                .userId(dispatchHandleAggregate.getUserId())
                .activityId(dispatchHandleAggregate.getActivityId())
                .awardId(dispatchHandleAggregate.getAwardId())
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

package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.AwardDispatchAggregate;
import com.dasi.domain.award.model.entity.ActivityAccountEntity;
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
import java.time.LocalDateTime;

@Slf4j
@AwardTypeConfig(awardType = AwardType.FIXED_ACCOUNT_POINT)
@Component
public class FixedAccountPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(AwardDispatchAggregate awardDispatchAggregate) {

        // 解析值
        String awardValue = awardDispatchAggregate.getAwardEntity().getAwardValue();
        if (!StringUtils.isNumeric(awardValue)) {
            throw new AppException("固定积分配置错误：awardValue={}" + awardValue);
        }
        int fixedPoint = Integer.parseInt(awardValue);

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
        activityAccountEntity.setAccountPoint(fixedPoint);

        awardRepository.saveUserAward(activityAccountEntity, awardDispatchAggregate.getActivityAwardEntity(), userAwardEntity);
    }

}

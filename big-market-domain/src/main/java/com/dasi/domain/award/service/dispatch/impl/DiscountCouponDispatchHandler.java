package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.entity.UserAwardEntity;
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
@AwardTypeConfig(awardType = AwardType.DISCOUNT_COUPON)
@Component
public class DiscountCouponDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;


    @Override
    public void dispatchHandle(DispatchHandleAggregate dispatchHandleAggregate) {
        String awardValue = dispatchHandleAggregate.getAwardEntity().getAwardValue();
        if (!StringUtils.isNumeric(awardValue)) {
            throw new AppException("到期时间配置错误：awardValue={}" + awardValue);
        }
        long seconds = Long.parseLong(awardValue);
        LocalDateTime awardDeadline = LocalDateTime.now().plusSeconds(seconds);
        UserAwardEntity userAwardEntity = UserAwardEntity.builder()
                .orderId(dispatchHandleAggregate.getOrderId())
                .userId(dispatchHandleAggregate.getUserId())
                .awardId(dispatchHandleAggregate.getAwardId())
                .awardType(AwardType.DISCOUNT_COUPON)
                .awardName(dispatchHandleAggregate.getAwardEntity().getAwardName())
                .awardDesc(dispatchHandleAggregate.getAwardEntity().getAwardDesc())
                .awardDeadline(awardDeadline)
                .awardTime(dispatchHandleAggregate.getActivityAwardEntity().getAwardTime())
                .build();
        dispatchHandleAggregate.setUserAwardEntity(userAwardEntity);
        awardRepository.saveUserAward(dispatchHandleAggregate);
    }

}

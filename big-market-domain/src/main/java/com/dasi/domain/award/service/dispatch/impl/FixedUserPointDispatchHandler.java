package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.AwardDispatchHandleAggregate;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.io.DispatchContext;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.dispatch.IAwardDispatchHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@AwardTypeConfig(awardType = AwardType.FIXED_USER_POINT)
@Component
public class FixedUserPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(DispatchContext dispatchContext, AwardEntity awardEntity) {

        // 1. 获取配置信息
        String awardConfig = awardEntity.getAwardConfig();

        // 2. 转换为积分值
        Integer fixedPoint = Integer.parseInt(awardConfig);

        // 3. 增加账户积分
        AwardDispatchHandleAggregate awardDispatchHandleAggregate = AwardDispatchHandleAggregate.builder()
                .userId(dispatchContext.getUserId())
                .awardId(dispatchContext.getAwardId())
                .orderId(dispatchContext.getOrderId())
                .userPoint(fixedPoint)
                .awardEntity(awardEntity)
                .build();
        awardRepository.increaseUserAccountPoint(awardDispatchHandleAggregate);

    }

}

package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.AwardDispatchHandleAggregate;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.io.DispatchContext;
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
@AwardTypeConfig(awardType = AwardType.RANDOM_USER_POINT)
@Component
public class RandomUserPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(DispatchContext dispatchContext, AwardEntity awardEntity) {

        // 1. 获取配置信息
        String awardConfig = awardEntity.getAwardConfig();

        // 2. 生成随机值
        String[] pointRange = awardConfig.split(Delimiter.COMMA);
        if (pointRange.length != 2) {
            throw new AppException("随机积分配置错误：awardConfig={}" + awardConfig);
        }
        int randomPoint = ThreadLocalRandom.current().nextInt(Integer.parseInt(pointRange[0]), Integer.parseInt(pointRange[1]) + 1);

        // 3. 增加账户积分
        AwardDispatchHandleAggregate awardDispatchHandleAggregate = AwardDispatchHandleAggregate.builder()
                .userId(dispatchContext.getUserId())
                .awardId(dispatchContext.getAwardId())
                .orderId(dispatchContext.getOrderId())
                .userPoint(randomPoint)
                .awardEntity(awardEntity)
                .build();
        awardRepository.increaseUserAccountPoint(awardDispatchHandleAggregate);
    }

}

package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
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
    public void dispatchHandle(DispatchHandleAggregate dispatchHandleAggregate) {
        String awardValue = dispatchHandleAggregate.getAwardEntity().getAwardValue();
        String[] pointRange = awardValue.split(Delimiter.COMMA);
        if (pointRange.length != 2) {
            throw new AppException("随机积分配置错误：awardValue={}" + awardValue);
        }
        int randomPoint = ThreadLocalRandom.current().nextInt(Integer.parseInt(pointRange[0]), Integer.parseInt(pointRange[1]) + 1);
        dispatchHandleAggregate.setActivityPoint(randomPoint);
        awardRepository.increaseActivityAccountPoint(dispatchHandleAggregate);
    }

}

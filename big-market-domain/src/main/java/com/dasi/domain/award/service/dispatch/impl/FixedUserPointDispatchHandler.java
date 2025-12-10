package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.dispatch.IAwardDispatchHandler;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@AwardTypeConfig(awardType = AwardType.FIXED_USER_POINT)
@Component
public class FixedUserPointDispatchHandler implements IAwardDispatchHandler {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public void dispatchHandle(DispatchHandleAggregate dispatchHandleAggregate) {
        String awardValue = dispatchHandleAggregate.getAwardEntity().getAwardValue();
        if (!StringUtils.isNumeric(awardValue)) {
            throw new AppException("固定积分配置错误：awardValue={}" + awardValue);
        }
        int fixedPoint = Integer.parseInt(awardValue);
        dispatchHandleAggregate.setActivityPoint(fixedPoint);
        awardRepository.increaseActivityAccountPoint(dispatchHandleAggregate);
    }

}

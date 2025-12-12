package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.aggregate.AwardDispatchAggregate;
import com.dasi.domain.award.model.entity.ActivityAccountEntity;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.io.DispatchContext;
import com.dasi.domain.award.model.io.DispatchResult;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.dispatch.IAwardDispatch;
import com.dasi.domain.award.service.dispatch.IAwardDispatchHandler;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AwardDispatch implements IAwardDispatch {

    @Resource
    private IAwardRepository awardRepository;

    private final Map<String, IAwardDispatchHandler> awardDispatchHandlerMap = new ConcurrentHashMap<>();

    public AwardDispatch(List<IAwardDispatchHandler> dispatchHandlerList) {
        dispatchHandlerList.forEach(dispatchHandler -> {
            AwardTypeConfig awardTypeConfig = AnnotationUtils.findAnnotation(dispatchHandler.getClass(), AwardTypeConfig.class);
            if (awardTypeConfig != null) {
                awardDispatchHandlerMap.put(awardTypeConfig.awardType().name(), dispatchHandler);
            }
        });
    }

    @Override
    public DispatchResult doAwardDispatch(DispatchContext dispatchContext) {

        String userId = dispatchContext.getUserId();
        Long awardId = dispatchContext.getAwardId();
        String orderId = dispatchContext.getOrderId();
        Long activityId = dispatchContext.getActivityId();

        // 获取记录
        AwardEntity awardEntity = awardRepository.queryAwardByAwardId(awardId);
        ActivityAccountEntity activityAccountEntity = awardRepository.queryActivityAccount(userId, activityId);
        ActivityAwardEntity activityAwardEntity = awardRepository.queryActivityAwardByOrderId(userId, orderId);
        String awardType = awardEntity.getAwardType().name();
        String awardName = awardEntity.getAwardName();

        // 处理幂等
        if (activityAwardEntity.getAwardState().equals(AwardState.COMPLETED)) {
            return DispatchResult.builder()
                    .awardType(awardType)
                    .awardName(awardName)
                    .build();
        }

        // 分发奖品到账户
        IAwardDispatchHandler awardDispatchHandler = awardDispatchHandlerMap.get(awardType);
        if (awardDispatchHandler == null) {
            throw new AppException("当前奖品类型没有配置获奖逻辑：awardType={}" + awardType);
        } else {
            AwardDispatchAggregate awardDispatchAggregate = AwardDispatchAggregate.builder()
                        .userId(userId)
                        .awardId(awardId)
                        .orderId(orderId)
                        .activityId(activityId)
                        .awardEntity(awardEntity)
                        .activityAwardEntity(activityAwardEntity)
                        .activityAccountEntity(activityAccountEntity)
                        .build();
            awardDispatchHandler.dispatchHandle(awardDispatchAggregate);
            return DispatchResult.builder()
                    .awardType(awardType)
                    .awardName(awardName)
                    .build();
        }
    }
}

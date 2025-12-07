package com.dasi.domain.award.service.dispatch.impl;

import com.dasi.domain.award.annotation.AwardTypeConfig;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.domain.award.model.io.DispatchContext;
import com.dasi.domain.award.model.io.DispatchResult;
import com.dasi.domain.award.model.type.AwardType;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.dispatch.IAwardDispatch;
import com.dasi.domain.award.service.dispatch.IAwardDispatchHandler;
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

        // 创建账户
        awardRepository.createUserAccountIfAbsent(userId);

        // 获取奖品类型
        AwardEntity awardEntity = awardRepository.queryAwardByAwardId(awardId);
        AwardType awardType = awardEntity.getAwardType();

        // 分发奖品到账户
        IAwardDispatchHandler awardDispatchHandler = awardDispatchHandlerMap.get(awardType.name());
        if (awardDispatchHandler == null) {
            log.error("【获奖】当前奖品类型没有配置投递逻辑：awardType={}", awardType);
            return null;
        } else {
            awardDispatchHandler.dispatchHandle(dispatchContext, awardEntity);
            return DispatchResult.builder()
                    .awardType(awardEntity.getAwardType().name())
                    .awardName(awardEntity.getAwardName())
                    .build();
        }
    }
}

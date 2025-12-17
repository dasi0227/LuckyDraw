package com.dasi.domain.point.service.dispatch.impl;

import com.dasi.domain.point.annotation.TradeTypeConfig;
import com.dasi.domain.point.model.aggregate.PointDispatchAggregate;
import com.dasi.domain.point.model.entity.ActivityAccountEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.io.DispatchContext;
import com.dasi.domain.point.model.io.DispatchResult;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.domain.point.service.dispatch.IPointDispatch;
import com.dasi.domain.point.service.dispatch.IPointDispatchHandler;
import com.dasi.types.exception.AppException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PointDispatch implements IPointDispatch {

    @Resource
    private IPointRepository pointRepository;

    private final Map<String, IPointDispatchHandler> pointDispatchHandlerMap = new ConcurrentHashMap<>();

    public PointDispatch(List<IPointDispatchHandler> dispatchHandlerList) {
        dispatchHandlerList.forEach(dispatchHandler -> {
            TradeTypeConfig tradeTypeConfig = AnnotationUtils.findAnnotation(dispatchHandler.getClass(), TradeTypeConfig.class);
            if (tradeTypeConfig != null) {
                pointDispatchHandlerMap.put(tradeTypeConfig.tradeType().name(), dispatchHandler);
            }
        });
    }

    @Override
    public DispatchResult doPointDispatch(DispatchContext dispatchContext) {

        String userId = dispatchContext.getUserId();
        Long tradeId = dispatchContext.getTradeId();
        String orderId = dispatchContext.getOrderId();
        Long activityId = dispatchContext.getActivityId();

        // 获取记录
        TradeEntity tradeEntity = pointRepository.queryTradeByTradeId(tradeId);
        TradeOrderEntity tradeOrderEntity = pointRepository.queryTradeOrderByOrderId(userId, orderId);
        ActivityAccountEntity activityAccountEntity = pointRepository.queryActivityAccount(userId, activityId);
        String tradeType = tradeEntity.getTradeType().name();
        String tradeName = tradeEntity.getTradeName();

        // 处理幂等
        if (tradeOrderEntity.getTradeState().equals(TradeState.USED)) {
            return DispatchResult.builder()
                    .tradeType(tradeType)
                    .tradeName(tradeName)
                    .build();
        }

        // 分发交易结果到账户
        IPointDispatchHandler pointDispatchHandler = pointDispatchHandlerMap.get(tradeType);
        if (pointDispatchHandler == null) {
            throw new AppException("当前交易类型没有配置交易逻辑：tradeType={}" + tradeType);
        } else {
            PointDispatchAggregate pointDispatchAggregate = PointDispatchAggregate.builder()
                    .userId(userId)
                    .tradeId(tradeId)
                    .orderId(orderId)
                    .activityId(activityId)
                    .tradeEntity(tradeEntity)
                    .tradeOrderEntity(tradeOrderEntity)
                    .activityAccountEntity(activityAccountEntity)
                    .build();
            pointDispatchHandler.dispatchHandle(pointDispatchAggregate);
            return DispatchResult.builder()
                    .tradeType(tradeType)
                    .tradeName(tradeName)
                    .build();
        }
    }

}

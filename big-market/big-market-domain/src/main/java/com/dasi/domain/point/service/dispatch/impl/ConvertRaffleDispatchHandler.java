package com.dasi.domain.point.service.dispatch.impl;

import com.dasi.domain.common.IRedisLock;
import com.dasi.domain.point.annotation.TradeTypeConfig;
import com.dasi.domain.point.model.aggregate.PointDispatchAggregate;
import com.dasi.domain.point.model.type.TradeType;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.domain.point.service.dispatch.IPointDispatchHandler;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.BusinessException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@TradeTypeConfig(tradeType = TradeType.CONVERT_RAFFLE)
@Component
public class ConvertRaffleDispatchHandler implements IPointDispatchHandler {

    @Resource
    private IPointRepository pointRepository;

    @Resource
    private IRedisLock redisLock;

    @Override
    public void dispatchHandle(PointDispatchAggregate pointDispatchAggregate) {

        String lockKey = RedisKey.LOCK_RAFFLE_KEY + pointDispatchAggregate.getActivityId() + Delimiter.COLON + pointDispatchAggregate.getUserId();
        boolean isLock = false;

        try {
            isLock = redisLock.tryLock(lockKey);
            if (!isLock) {
                throw new BusinessException("当前系统繁忙，请稍后再试");
            }
            pointRepository.saveConvertRaffle(pointDispatchAggregate.getActivityAccountEntity(), pointDispatchAggregate.getTradeEntity(), pointDispatchAggregate.getTradeOrderEntity());
        } finally {
            if (isLock) {
                redisLock.unlock(lockKey);
            }
        }

    }
}

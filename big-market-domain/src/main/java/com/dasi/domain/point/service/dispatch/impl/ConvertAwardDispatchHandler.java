package com.dasi.domain.point.service.dispatch.impl;

import com.dasi.domain.point.annotation.TradeTypeConfig;
import com.dasi.domain.point.model.aggregate.PointDispatchAggregate;
import com.dasi.domain.point.model.type.TradeType;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.domain.point.service.dispatch.IPointDispatchHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@TradeTypeConfig(tradeType = TradeType.CONVERT_AWARD)
@Component
public class ConvertAwardDispatchHandler implements IPointDispatchHandler {

    @Resource
    private IPointRepository pointRepository;

    @Override
    public void dispatchHandle(PointDispatchAggregate pointDispatchAggregate) {
        pointRepository.saveConvertAward(pointDispatchAggregate.getActivityAccountEntity(), pointDispatchAggregate.getTradeEntity(), pointDispatchAggregate.getTradeOrderEntity());
    }
}

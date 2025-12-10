package com.dasi.domain.trade.repository;

import com.dasi.domain.trade.model.entity.TaskEntity;
import com.dasi.domain.trade.model.entity.TradeEntity;
import com.dasi.domain.trade.model.entity.TradeOrderEntity;

import java.util.List;

public interface ITradeRepository {

    TradeEntity queryTradeByTradeId(Long tradeId);

    void createActivityAccountIfAbsent(String userId, Long activityId);

    void savePointRechargeTradeOrder(TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity);

    void savePointConvertTradeOrder(TaskEntity taskEntity, TradeOrderEntity tradeOrderEntity);

    void updateTradeOrderState(TradeOrderEntity tradeOrderEntity);

    void doConvertAward(String userId, String orderId, TradeEntity tradeEntity);

    void doConvertRaffle(String userId, TradeEntity tradeEntity);

    Integer queryActivityAccountPoint(String userId, Long activityId);

    List<TradeEntity> queryConvertListByActivityId(Long activityId);
}

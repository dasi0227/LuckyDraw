package com.dasi.domain.point.repository;

import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;

public interface IPointRepository {

    TradeEntity queryTradeByTradeId(Long tradeId);

    void createUserAccountIfAbsent(String userId);

    void savePointRechargeTradeOrder(TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity);

    void savePointConvertTradeOrder(TaskEntity taskEntity, TradeOrderEntity tradeOrderEntity);

    void updateTradeOrderState(TradeOrderEntity tradeOrderEntity);

    void doConvertAward(String userId, String orderId, TradeEntity tradeEntity);

    void doConvertRaffle(String userId, TradeEntity tradeEntity);

    Integer queryUserPointByUserId(String userId);
}

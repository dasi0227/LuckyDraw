package com.dasi.domain.point.repository;

import com.dasi.domain.point.model.entity.ActivityAccountEntity;
import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;

import java.util.List;

public interface IPointRepository {

    TradeEntity queryTradeByTradeId(Long tradeId);

    TradeOrderEntity queryTradeOrderByOrderId(String userId, String orderId);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    List<TradeEntity> queryActivityConvertList(Long activityId);

    void savePointTradeOrder(ActivityAccountEntity activityAccountEntity, TaskEntity taskEntity, TradeOrderEntity tradeOrderEntity);

    void saveConvertAward(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity);

    void saveConvertRaffle(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity);

    void savePointReward(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity);
}

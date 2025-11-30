package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.entity.StrategyAwardStockEntity;

import java.time.LocalDateTime;

public interface IStrategyStock {

    long subtractStrategyAwardCount(Long strategyId, Long awardId, LocalDateTime activityEndTime);

    StrategyAwardStockEntity getQueueValue();

    void updateStrategyAwardStock(StrategyAwardStockEntity stockUpdateRequest);

}

package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.io.StrategyAwardStock;

import java.time.LocalDateTime;

public interface IStrategyStock {

    long subtractStrategyAwardCount(Long strategyId, Long awardId, LocalDateTime activityEndTime);

    StrategyAwardStock getQueueValue();

    void updateStrategyAwardStock(StrategyAwardStock stockUpdateRequest);

}

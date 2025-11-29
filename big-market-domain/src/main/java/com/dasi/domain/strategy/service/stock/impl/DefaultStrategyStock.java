package com.dasi.domain.strategy.service.stock.impl;

import com.dasi.domain.strategy.model.entity.StrategyAwardStockEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.stock.IStrategyStock;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultStrategyStock implements IStrategyStock {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public long subStrategyAwardCount(Long strategyId, Integer awardId) {
        String cacheKey = RedisKey.STRATEGY_AWARD_STOCK_SURPLUS_KEY + strategyId + Delimiter.UNDERSCORE + awardId;
        return strategyRepository.subStrategyAwardStock(cacheKey);
    }

    @Override
    public StrategyAwardStockEntity getQueueValue() {
        return strategyRepository.getQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(StrategyAwardStockEntity strategyAwardStockEntity) {
        strategyRepository.updateStrategyAwardStock(strategyAwardStockEntity.getStrategyId(), strategyAwardStockEntity.getAwardId());
    }

}

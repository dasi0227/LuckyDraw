package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.dto.StockUpdateRequest;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.common.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultStock implements IStock{

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public long subStrategyAwardCount(Long strategyId, Integer awardId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_STOCK_KEY + strategyId + Constants.UNDERSCORE + awardId;
        return strategyRepository.subStrategyAwardCount(cacheKey);
    }

    @Override
    public StockUpdateRequest getQueueValue() {
        return strategyRepository.getQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(StockUpdateRequest stockUpdateRequest) {
        strategyRepository.updateStrategyAwardStock(stockUpdateRequest.getStrategyId(), stockUpdateRequest.getAwardId());
    }

}

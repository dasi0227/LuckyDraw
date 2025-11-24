package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.message.StockUpdateMessage;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Character;
import com.dasi.types.constant.RedisKey;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultStock implements IStock{

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public long subStrategyAwardCount(Long strategyId, Integer awardId) {
        String cacheKey = RedisKey.STRATEGY_AWARD_STOCK_KEY + strategyId + Character.UNDERSCORE + awardId;
        return strategyRepository.subStrategyAwardCount(cacheKey);
    }

    @Override
    public StockUpdateMessage getQueueValue() {
        return strategyRepository.getQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(StockUpdateMessage stockUpdateMessage) {
        strategyRepository.updateStrategyAwardStock(stockUpdateMessage.getStrategyId(), stockUpdateMessage.getAwardId());
    }

}

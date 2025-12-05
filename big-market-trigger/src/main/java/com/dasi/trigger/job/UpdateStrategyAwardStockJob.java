package com.dasi.trigger.job;

import com.dasi.domain.strategy.model.entity.StrategyAwardStockEntity;
import com.dasi.domain.strategy.service.stock.IStrategyStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateStrategyAwardStockJob {

    @Resource
    private IStrategyStock strategyStock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void updateAwardStock() {
        StrategyAwardStockEntity awardStock = strategyStock.getQueueValue();
        if (awardStock == null) {
            log.debug("【库存】无待更新");
            return;
        }

        Long strategyId = awardStock.getStrategyId();
        Long awardId = awardStock.getAwardId();

        try {
            strategyStock.updateStrategyAwardStock(awardStock);
            log.info("【库存】更新策略奖品库存成功：strategyId={}, awardId={}", strategyId, awardId);
        } catch (Exception e) {
            log.error("【库存】更新策略奖品库存失败：strategyId={}, awardId={}, error={}", strategyId, awardId, e.getMessage());
        }
    }

}

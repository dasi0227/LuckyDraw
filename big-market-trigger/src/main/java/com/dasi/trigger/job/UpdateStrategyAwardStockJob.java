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
        try {
            StrategyAwardStockEntity strategyAwardStockEntity = strategyStock.getQueueValue();
            if (strategyAwardStockEntity != null) {
                strategyStock.updateStrategyAwardStock(strategyAwardStockEntity);
                log.info("【定时任务 - updateAwardStock】更新策略奖品库存成功：strategyId = {}, awardId = {}", strategyAwardStockEntity.getStrategyId(), strategyAwardStockEntity.getAwardId());
            } else {
                log.debug("【定时任务 - updateAwardStock】暂时没有策略奖品库存待更新");
            }
        } catch (Exception e) {
            log.error("【定时任务 - updateAwardStock】更新策略奖品库存失败：error = {}", e.getMessage());
        }
    }

}

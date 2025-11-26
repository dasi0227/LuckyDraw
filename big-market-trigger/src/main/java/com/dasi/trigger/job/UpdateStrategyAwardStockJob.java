package com.dasi.trigger.job;

import com.dasi.domain.strategy.model.dto.StrategyAwardStock;
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
            StrategyAwardStock strategyAwardStock = strategyStock.getQueueValue();
            if (strategyAwardStock != null) {
                strategyStock.updateStrategyAwardStock(strategyAwardStock);
                log.info("【定时任务】更新奖品库存成功：{}", strategyAwardStock);
            } else {
                log.info("【定时任务】暂时没有奖品库存待更新");
            }
        } catch (Exception e) {
            log.error("【定时任务】更新奖品库存失败：{}", e.getMessage());
        }
    }

}

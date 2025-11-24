package com.dasi.trigger.job;

import com.dasi.domain.strategy.model.message.StockUpdateMessage;
import com.dasi.domain.strategy.service.stock.IStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateAwardStockJob {

    @Resource
    private IStock stock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void updateAwardStock() {
        try {
            StockUpdateMessage stockUpdateMessage = stock.getQueueValue();
            if (stockUpdateMessage != null) {
                stock.updateStrategyAwardStock(stockUpdateMessage);
                log.info("【定时任务】更新奖品库存：{}", stockUpdateMessage);
            }
            log.info("【定时任务】暂时没有奖品库存待更新");
        } catch (Exception e) {
            log.error("【定时任务】失败：{}", e.getMessage());
        }
    }

}

package com.dasi.trigger.job;

import com.dasi.domain.activity.model.dto.SkuStock;
import com.dasi.domain.activity.service.stock.IActivityStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateRechargeSkuStockJob {

    @Resource
    private IActivityStock activityStock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void updateRechargeSkuStockJob() {
        try {
            SkuStock skuStock = activityStock.getQueueValue();
            if (skuStock != null) {
                activityStock.updateRechargeSkuStock(skuStock.getSkuId());
                log.info("【定时任务】更新 SKU 库存成功：{}", skuStock);
            } else {
                log.info("【定时任务】暂时没有 SKU 库存待更新");
            }
        } catch (Exception e) {
            log.error("【定时任务】更新 SKU 库存失败：{}", e.getMessage());
        }
    }

}

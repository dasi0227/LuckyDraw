package com.dasi.trigger.job;

import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.service.stock.IActivityStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateActivitySkuStockJob {

    @Resource
    private IActivityStock activityStock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void updateActivitySkuStockJob() {
        try {
            ActivitySkuStock activitySkuStock = activityStock.getQueueValue();
            if (activitySkuStock != null) {
                activityStock.updateActivitySkuStock(activitySkuStock.getSkuId());
                log.info("【定时任务】更新 SKU 库存成功：{}", activitySkuStock);
            } else {
                log.info("【定时任务】暂时没有 SKU 库存待更新");
            }
        } catch (Exception e) {
            log.error("【定时任务】更新 SKU 库存失败：{}", e.getMessage());
        }
    }

}

package com.dasi.trigger.job;

import com.dasi.domain.activity.model.queue.ActivitySkuStock;
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
    public void updateRechargeSkuStock() {
        ActivitySkuStock activitySkuStock = activityStock.getQueueValue();
        if (activitySkuStock == null) {
            log.debug("【库存】无待更新");
            return;
        }

        Long skuId = activitySkuStock.getSkuId();
        Long activityId = activitySkuStock.getActivityId();

        try {
            activityStock.updateRechargeSkuStock(skuId);
            log.info("【库存】更新活动 SKU 库存成功：skuId={}, activityId={}", skuId, activityId);
        } catch (Exception e) {
            log.error("【库存】更新活动 SKU 库存失败：skuId={}, activityId={}, error={}", skuId, activityId, e.getMessage());
        }
    }

}

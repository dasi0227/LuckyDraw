package com.dasi.domain.activity.repository;

import com.dasi.domain.activity.model.dto.SkuStock;
import com.dasi.domain.activity.model.dto.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.*;

import java.time.LocalDateTime;

public interface IActivityRepository {

    RechargeSkuEntity queryRechargeSkuBySkuId(Long skuId);

    ActivityEntity queryActivityByActivityId(Long activityId);

    RechargeQuotaEntity queryRechargeQuotaByQuotaId(Long quotaId);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day);

    RaffleOrderEntity queryUnusedRaffleOrder(String userId, Long activityId);

    void cacheRechargeSkuStockSurplus(Long skuId, Integer stockSurplus);

    Long subtractRechargeSkuStockSurplus(Long skuId, LocalDateTime endTime);

    void sendRechargeSkuStockConsumeToMQ(SkuStock skuStock);

    SkuStock getQueueValue();

    void clearQueueValue();

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

    void saveRaffleOrder(RaffleOrderAggregate raffleOrderAggregate);

    void saveRechargeOrder(RechargeOrderEntity rechargeOrderEntity);
}

package com.dasi.domain.activity.model.dto;

import com.dasi.domain.activity.model.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionChainCheck {

    private String userId;

    private Long activityId;

    private ActivityEntity activityEntity;

    private RechargeSkuEntity rechargeSkuEntity;

    private RechargeQuotaEntity rechargeQuotaEntity;

    private RaffleOrderAggregate raffleOrderAggregate;

}

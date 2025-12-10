package com.dasi.domain.trade.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRechargeContext {

    private String userId;

    private String bizId;

    private Long tradeId;

    private Long activityId;

}

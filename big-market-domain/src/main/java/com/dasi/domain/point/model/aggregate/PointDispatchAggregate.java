package com.dasi.domain.point.model.aggregate;

import com.dasi.domain.point.model.entity.ActivityAccountEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointDispatchAggregate {

    private String userId;

    private Long activityId;

    private Long tradeId;

    private String orderId;

    private TradeEntity tradeEntity;

    private TradeOrderEntity tradeOrderEntity;

    private ActivityAccountEntity activityAccountEntity;

}

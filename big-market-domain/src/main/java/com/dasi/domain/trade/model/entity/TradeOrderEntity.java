package com.dasi.domain.trade.model.entity;

import com.dasi.domain.trade.model.type.TradeState;
import com.dasi.domain.trade.model.type.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderEntity {

    private String orderId;

    private String bizId;

    private String userId;

    private Long tradeId;

    private Long activityId;

    private TradeType tradeType;

    private TradeState tradeState;

    private LocalDateTime tradeTime;

}
